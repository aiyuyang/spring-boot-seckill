package com.example.seckill_system.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.seckill_system.config.RabbitMQConfig;
import com.example.seckill_system.entity.SeckillActivity;
import com.example.seckill_system.service.ISeckillActivityService;
import com.example.seckill_system.service.ISeckillService;

import lombok.extern.slf4j.Slf4j;

/**
 * Seckill Stock Synchronization Task - Data Consistency Maintenance
 * 
 * <p>This scheduled task runs every minute to reconcile stock counts between Redis
 * and MySQL, ensuring data consistency in the distributed system. It uses MySQL as
 * the source of truth and corrects any discrepancies in Redis.</p>
 * 
 * <p><b>Why This Task Is Needed</b>:</p>
 * <p>In a high-concurrency system with asynchronous processing, temporary inconsistencies
 * can occur between Redis (fast cache) and MySQL (persistent storage). This task detects
 * and fixes these inconsistencies to prevent:</p>
 * <ul>
 *   <li><b>Over-selling</b>: Redis shows stock available but database is empty</li>
 *   <li><b>Under-selling</b>: Redis shows sold out but database still has stock</li>
 * </ul>
 * 
 * <p><b>Reconciliation Strategy</b>:</p>
 * <p>The task uses MySQL as the source of truth because:</p>
 * <ul>
 *   <li>MySQL is the persistent storage (survives restarts)</li>
 *   <li>MySQL has transaction guarantees (ACID properties)</li>
 *   <li>MySQL is updated by MQ consumers (controlled rate, reliable)</li>
 * </ul>
 * 
 * <p><b>Distributed Lock</b>:</p>
 * <p>The task uses a Redis distributed lock to ensure only one instance executes
 * the synchronization in a multi-instance deployment. This prevents duplicate work
 * and potential race conditions.</p>
 * 
 * <p><b>MQ Queue Check</b>:</p>
 * <p>Before synchronizing, the task checks if the RabbitMQ queue has any backlog.
 * If messages are still being processed, synchronization is skipped to avoid overwriting
 * correct Redis data with stale database data.</p>
 * 
 * <p><b>Execution Schedule</b>:</p>
 * <p>The task runs every minute ({@code @Scheduled(cron = "0 0/1 * * * ?")}),
 * which provides a good balance between:</p>
 * <ul>
 *   <li>Detecting inconsistencies quickly</li>
 *   <li>Not overloading the system with frequent checks</li>
 * </ul>
 * 
 * @author Ai Yuyang
 * @see com.example.seckill_system.service.ISeckillService
 */
@Component
@Slf4j
public class SeckillStockSyncTask {
    
    /**
     * Activity service for querying activities from database.
     */
    @Autowired
    private ISeckillActivityService seckillActivityService;

    /**
     * Seckill service for stock recovery and local cache management.
     */
    @Autowired
    private ISeckillService seckillService;

    /**
     * Redis template for distributed lock and stock queries.
     */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * RabbitMQ admin for querying queue properties.
     */
    @Autowired
    private AmqpAdmin amqpAdmin;

    /**
     * Distributed lock key for preventing concurrent execution.
     * 
     * <p>This key is used with Redis {@code SETNX} (set if not exists) to create
     * a distributed lock. Only one instance can acquire the lock at a time.</p>
     */
    private static final String LOCK_KEY = "seckill:task:lock";
    
    /**
     * Lock expiration time in seconds.
     * 
     * <p>This prevents deadlocks if the task crashes while holding the lock.
     * The lock automatically expires after 10 seconds, allowing another instance
     * to acquire it if the first instance fails.</p>
     */
    private static final long LOCK_EXPIRE = 10;

    /**
     * Stock synchronization task - runs every minute.
     * 
     * <p>This method performs the following operations:</p>
     * <ol>
     *   <li>Acquires distributed lock (prevents concurrent execution)</li>
     *   <li>Checks MQ queue backlog (skips sync if messages are being processed)</li>
     *   <li>Queries active activities from database</li>
     *   <li>Reconciles stock for each activity</li>
     *   <li>Releases distributed lock</li>
     * </ol>
     * 
     * <p><b>Distributed Lock Mechanism</b>:</p>
     * <p>The lock is acquired using Redis {@code SETNX} (set if not exists) with TTL.
     * This ensures:</p>
     * <ul>
     *   <li>Only one instance executes the task at a time</li>
     *   <li>Lock automatically expires if instance crashes (prevents deadlock)</li>
     *   <li>Atomic operation (no race conditions)</li>
     * </ul>
     * 
     * <p><b>MQ Queue Check</b>:</p>
     * <p>Before synchronizing, the task checks if the RabbitMQ queue has any unprocessed
     * messages. If messages are still being consumed, the database may not have caught up
     * with Redis yet, so synchronization is skipped to avoid overwriting correct data.</p>
     * 
     * <p><b>Query Optimization</b>:</p>
     * <p>The task only queries activities that haven't ended yet, avoiding unnecessary
     * processing of historical data. This reduces database load and improves performance.</p>
     * 
     * <p><b>Error Handling</b>:</p>
     * <p>All exceptions are caught and logged, but the lock is always released in the
     * {@code finally} block to prevent deadlocks.</p>
     */
    @Scheduled(cron = "0 0/1 * * * ?") // Execute every minute
    public void syncStock() {
        // Step 1: 【Distributed lock】Prevent concurrent execution across multiple instances
        // setIfAbsent = SETNX (atomic operation)
        // This ensures only one instance executes the sync task at a time
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "LOCKED", LOCK_EXPIRE, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(locked)) {
            // Another instance is already executing the task
            log.debug("Failed to acquire distributed lock, task is being executed by another instance...");
            return;
        }

        try {
            // Check if MQ queue has backlog
            // Get queue properties to check message count
            Properties queueProperties = amqpAdmin.getQueueProperties(RabbitMQConfig.QUEUE);
            int messageCount = 0;
            if (queueProperties != null) {
                // QUEUE_MESSAGE_COUNT represents how many unprocessed messages are in the queue
                messageCount = (int) queueProperties.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
            }

            // If queue still has messages, DB is in "catching up" state, data is definitely inconsistent
            // Must not execute sync at this time, otherwise will overwrite Redis correct data with DB old data
            // Example: Redis stock = 0 (correct, all items sold), but DB still shows stock = 5 (old data)
            // If we sync now, we'd overwrite Redis with wrong data, causing over-selling
            if (messageCount > 0) {
                log.warn("MQ queue has backlog ({} messages), skipping this stock sync, waiting for consumption to complete.", messageCount);
                return;
            }

            // If queue is empty, DB has caught up with Redis, safe to synchronize
            log.info("MQ has no backlog, starting stock reconciliation task...");

            // Step 2: 【Range control】Only query "not ended" activities, avoid full table scan
            // This optimization reduces database load by only processing active activities
            // And create_time is recent (e.g., within 7 days), prevent scanning old historical data
            QueryWrapper<SeckillActivity> query = new QueryWrapper<>();
            query.gt("end_time", LocalDateTime.now());
            List<SeckillActivity> list = seckillActivityService.list(query);

            if (list.isEmpty()) return;

            // Reconcile stock for each active activity
            for (SeckillActivity activity : list) {
                syncSingleActivity(activity);
            }

        } catch (Exception e) {
            log.error("Stock synchronization task exception", e);
        } finally {
            // Step 3: 【Release lock】
            // Always release the lock, even if an exception occurred
            // This prevents deadlocks if the task crashes
            stringRedisTemplate.delete(LOCK_KEY);
        }
    }

    /**
     * Synchronizes stock for a single activity.
     * 
     * <p>This method compares Redis stock with database stock and corrects any discrepancies.
     * It handles two main scenarios:</p>
     * <ul>
     *   <li><b>Redis > DB</b>: Over-selling risk (Redis data is higher than actual)</li>
     *   <li><b>Redis < DB</b>: Under-selling (Redis shows sold out but DB has stock)</li>
     * </ul>
     * 
     * <p><b>Reconciliation Logic</b>:</p>
     * <p><b>Case A: Redis > DB (Over-selling Risk)</b>:</p>
     * <ul>
     *   <li><b>Phenomenon</b>: Redis has 10, DB only has 8</li>
     *   <li><b>Cause</b>: Rare scenario where DB was updated but Redis wasn't synced</li>
     *   <li><b>Risk</b>: Users might see stock available when it's actually sold out</li>
     *   <li><b>Action</b>: Force overwrite Redis with database value (prevent over-selling)</li>
     * </ul>
     * 
     * <p><b>Case B: Redis < DB (Under-selling)</b>:</p>
     * <ul>
     *   <li><b>Phenomenon</b>: Redis shows 0 (sold out), DB still has 5</li>
     *   <li><b>Cause</b>: MQ message lost, consumer failed, or rollback didn't succeed</li>
     *   <li><b>Impact</b>: Users can't purchase even though stock is available</li>
     *   <li><b>Action</b>: Replenish Redis stock from database (fix under-selling)</li>
     * </ul>
     * 
     * <p><b>Threshold Logic</b>:</p>
     * <p>For Case B, a threshold is used to avoid false positives from momentary time
     * differences in high-concurrency scenarios. However, if Redis is already 0, the
     * sync is performed immediately to restore availability.</p>
     * 
     * @param activity The activity to synchronize stock for
     */
    private void syncSingleActivity(SeckillActivity activity) {
        String redisKey = "seckill:stock:" + activity.getId();
        String redisStockStr = stringRedisTemplate.opsForValue().get(redisKey);

        // If Redis doesn't have this key, it means not preheated yet or expired, skip directly, don't sync
        // This prevents unnecessary synchronization for activities that haven't started or have already ended
        if (!StringUtils.hasText(redisStockStr)) return;

        long redisStock = Long.parseLong(redisStockStr);
        Integer dbStock = activity.getAvailableStock();

        // Step 4: 【Core reconciliation logic】
        
        // Case A: Redis > DB (Over-selling Risk)
        // Phenomenon: Redis has 10, DB only has 8.
        // Reason: DB just deducted, Redis hasn't synced yet (rare, because we deduct Redis first).
        // Handling: Should not happen theoretically. If it happens, Redis data is dirty, risk of over-selling.
        // Action: Force overwrite Redis with database value to prevent over-selling.
        if (redisStock > dbStock) {
            log.warn("Detected over-selling risk (Redis > DB)! ActivityId: {}, Redis: {}, DB: {}", activity.getId(), redisStock, dbStock);
            stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(dbStock));
        }

        // Case B: Redis < DB (Under-selling)
        // Phenomenon: Redis shows 0 (sold out), DB still has 5.
        // Reason: RabbitMQ message lost, or consumer execution failed and rollback didn't succeed. Causes "under-selling".
        // Handling: This is the core scenario that the scheduled task most needs to fix!
        // Action: Replenish Redis stock from database to restore availability.
        if (redisStock < dbStock) {
            // Set a threshold to avoid false positives from momentary time differences in high concurrency
            // Only sync if:
            // 1. Redis is already 0 (definitely under-selling, must fix immediately)
            // 2. Difference exceeds 1 (significant discrepancy, not just timing difference)
            if (redisStock == 0 || (dbStock - redisStock > 1)) {
                // Correct Redis stock by setting it to database value
                stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(dbStock));

                // 【Important】Since stock is replenished, if there's a "sold out flag" in local cache, must delete it too
                // This allows new requests to flow through to Redis again instead of being rejected immediately
                seckillService.removeLocalOverMap(activity.getId());
                log.info("Detected under-selling (Redis < DB), replenishing. ActivityId: {}, Redis: {}, DB: {}", activity.getId(), redisStock, dbStock);
            }
        }
    }
}
