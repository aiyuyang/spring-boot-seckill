package com.example.seckill_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.seckill_system.entity.SeckillActivity;
import com.example.seckill_system.service.ISeckillActivityService;
import com.example.seckill_system.service.ISeckillService;
import com.example.seckill_system.vo.RespBean;
import com.example.seckill_system.vo.RespBeanEnum;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seckill Activity Controller - Activity Management
 * 
 * <p>This controller handles all activity-related operations including creation,
 * querying, listing, and preheating (cache loading).</p>
 * 
 * <p><b>Core Operations</b>:</p>
 * <ul>
 *   <li><b>Activity CRUD</b>: Create, read, update, delete activities</li>
 *   <li><b>Activity Listing</b>: Query all activities with sorting</li>
 *   <li><b>Activity Preheating</b>: Load activity data and stock into Redis cache</li>
 *   <li><b>Server Time</b>: Provide server time for client synchronization</li>
 * </ul>
 * 
 * <p><b>Activity Lifecycle</b>:</p>
 * <ol>
 *   <li><b>Creation</b>: Admin creates activity with product, price, stock, and time window</li>
 *   <li><b>Preheating</b>: Before seckill starts, activity data is loaded into Redis</li>
 *   <li><b>Active</b>: During seckill period, users can place orders</li>
 *   <li><b>Ended</b>: After end time, no more orders can be placed</li>
 * </ol>
 * 
 * <p><b>Preheating Importance</b>:</p>
 * <p>The {@code publishActivity} endpoint is critical for performance. It loads
 * activity data and stock into Redis before the seckill starts, ensuring fast
 * access during high-concurrency periods. Without preheating, the first requests
 * would need to query the database, causing significant latency.</p>
 * 
 * @author Ai Yuyang
 * @see ISeckillActivityService
 * @see ISeckillService
 */
@RestController
@RequestMapping("/activity")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Activity Management Module")
public class SeckillActivityController {

    /**
     * Activity service for database operations.
     */
    @Autowired
    private ISeckillActivityService seckillActivityService;
    
    /**
     * Seckill service for cache preheating operations.
     */
    @Autowired
    private ISeckillService seckillService;

    /**
     * Creates a new seckill activity.
     * 
     * <p>This endpoint is typically used by administrators to create new seckill activities.
     * The activity defines the product, pricing, stock, and time window for the seckill.</p>
     * 
     * <p><b>Initialization</b>:</p>
     * <p>When creating an activity, the {@code availableStock} is automatically set to
     * equal {@code initialStock}, ensuring both values start at the same level.</p>
     * 
     * @param activity The activity entity containing all activity details
     * @return RespBean indicating success or failure
     */
    @Operation(summary = "Create Activity")
    @PostMapping
    public RespBean createActivity(@RequestBody SeckillActivity activity) {
        // Initialize available stock to match initial stock
        activity.setAvailableStock(activity.getInitialStock());
        boolean success = seckillActivityService.save(activity);
        return success ? RespBean.success() : RespBean.error(RespBeanEnum.ERROR);
    }

    /**
     * Retrieves detailed information about a specific activity.
     * 
     * <p>This endpoint is used by the frontend to display activity details including
     * product name, prices, stock levels, and time windows. The response includes
     * all information needed for the activity detail page.</p>
     * 
     * <p><b>Usage</b>:</p>
     * <p>Called when user navigates to an activity detail page. The frontend uses
     * this data to display product information and initialize the countdown timer.</p>
     * 
     * @param activityId The ID of the activity to retrieve
     * @return RespBean containing the activity entity, or error if not found
     */
    @Operation(summary = "Get Activity Details")
    @GetMapping("/{activityId}")
    public RespBean getActivityDetail(@PathVariable Long activityId) {
        SeckillActivity activity = seckillActivityService.getById(activityId);
        if (activity == null) {
            return RespBean.error(RespBeanEnum.ERROR, "Activity not found");
        }
        return RespBean.success(activity);
    }

    /**
     * Retrieves a list of all seckill activities.
     * 
     * <p>This endpoint returns all activities sorted by start time in descending order
     * (most recent first). The frontend uses this to display the activity list page.</p>
     * 
     * <p><b>Sorting</b>:</p>
     * <p>Activities are sorted by {@code start_time} in descending order, so upcoming
     * activities appear first in the list.</p>
     * 
     * <p><b>Performance</b>:</p>
     * <p>For systems with many activities, consider adding pagination or filtering
     * to limit the number of records returned.</p>
     * 
     * @return RespBean containing a list of all activities
     */
    @Operation(summary = "Get Activity List")
    @GetMapping("/list")
    public RespBean getActivityList() {
        QueryWrapper<SeckillActivity> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("start_time");
        List<SeckillActivity> list = seckillActivityService.list(wrapper);
        return RespBean.success(list);
    }

    /**
     * Publishes and preheats a seckill activity.
     * 
     * <p>This is a critical endpoint that loads activity data and stock into Redis
     * cache before the seckill starts. It should be called by administrators shortly
     * before the activity's start time to ensure optimal performance.</p>
     * 
     * <p><b>What Gets Cached</b>:</p>
     * <ul>
     *   <li>Activity object as JSON (for fast retrieval by MQ consumer)</li>
     *   <li>Stock count as string (for Lua script operations)</li>
     * </ul>
     * 
     * <p><b>Cache TTL</b>:</p>
     * <p>The cache TTL is set to activity end time + 5 minutes buffer, ensuring
     * the cache remains valid throughout the entire seckill period.</p>
     * 
     * <p><b>Cleanup</b>:</p>
     * <p>This method also cleans up any previous data (success users set, local cache flags)
     * to ensure a fresh start for the activity.</p>
     * 
     * <p><b>Error Handling</b>:</p>
     * <p>If the activity doesn't exist or has no stock, the service throws
     * {@link com.example.seckill_system.exception.GlobalException} which is caught
     * by {@link com.example.seckill_system.exception.GlobalExceptionHandler}.</p>
     * 
     * @param activityId The ID of the activity to publish and preheat
     * @return RespBean indicating success
     */
    @Operation(summary = "Publish/Preheat Activity")
    @PostMapping("/publish/{activityId}")
    public RespBean publishActivity(@PathVariable Long activityId) {
        // Service will throw GlobalException if activity doesn't exist or has no stock
        // No need for if-else here - exception handling is centralized in GlobalExceptionHandler
        seckillService.prepareSeckill(activityId);
        return RespBean.success("Activity published successfully, cache preheated");
    }

    /**
     * Returns the current server time for client synchronization.
     * 
     * <p>This endpoint is used by the frontend to synchronize client time with server time,
     * ensuring accurate countdown timers. The response includes both ISO string format
     * and Unix timestamp (milliseconds) for flexibility.</p>
     * 
     * <p><b>Time Synchronization</b>:</p>
     * <p>The frontend calculates the time offset between client and server, then uses
     * this offset to adjust all time calculations. This ensures countdown timers are
     * accurate even if the user's system clock is incorrect.</p>
     * 
     * <p><b>Response Format</b>:</p>
     * <pre>{@code
     * {
     *   "code": 200,
     *   "message": "SUCCESS",
     *   "obj": {
     *     "serverTime": "2025-12-10T13:56:13",
     *     "timestamp": 1733831773000
     *   }
     * }
     * }</pre>
     * 
     * <p><b>Performance</b>:</p>
     * <p>This endpoint is lightweight (~1ms response time) and can be called frequently
     * without impacting system performance.</p>
     * 
     * @return RespBean containing server time in both ISO string and timestamp formats
     */
    @Operation(summary = "Get Server Time (for time synchronization)")
    @GetMapping("/serverTime")
    public RespBean getServerTime() {
        Map<String, Object> result = new HashMap<>();
        result.put("serverTime", java.time.LocalDateTime.now().toString());
        result.put("timestamp", System.currentTimeMillis());
        return RespBean.success(result);
    }
    
    // ... update and delete methods should also return RespBean ...
}
