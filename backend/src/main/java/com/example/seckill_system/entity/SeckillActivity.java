package com.example.seckill_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * Seckill Activity Entity
 * 
 * <p>This entity represents a seckill (flash sale) activity in the system.
 * Each activity defines a product that will be sold at a discounted price during
 * a specific time window.</p>
 * 
 * <p><b>Core Fields</b>:</p>
 * <ul>
 *   <li><b>Stock Management</b>: {@code initialStock} (total) and {@code availableStock} (remaining)</li>
 *   <li><b>Pricing</b>: {@code originalPrice} (regular) and {@code seckillPrice} (discounted)</li>
 *   <li><b>Time Window</b>: {@code startTime} and {@code endTime} define when the activity is active</li>
 * </ul>
 * 
 * <p><b>Database Mapping</b>:</p>
 * <p>This entity maps to the {@code seckill_activity} table in MySQL. The table uses
 * InnoDB engine with row-level locking to prevent concurrent update conflicts.</p>
 * 
 * <p><b>Stock Consistency</b>:</p>
 * <p>The system maintains stock consistency through multiple layers:</p>
 * <ol>
 *   <li><b>Redis Cache</b>: Pre-reduced stock for fast access (Lua script atomic operation)</li>
 *   <li><b>Database</b>: Final source of truth (updated asynchronously via RabbitMQ)</li>
 *   <li><b>Local Cache</b>: JVM memory flag to quickly reject requests when sold out</li>
 * </ol>
 * 
 * <p><b>Usage Example</b>:</p>
 * <pre>{@code
 * SeckillActivity activity = new SeckillActivity();
 * activity.setName("iPhone 15 Pro");
 * activity.setProductId(1001L);
 * activity.setOriginalPrice(new BigDecimal("9999.00"));
 * activity.setSeckillPrice(new BigDecimal("4999.00"));
 * activity.setInitialStock(100);
 * activity.setAvailableStock(100);
 * activity.setStartTime(LocalDateTime.now().plusHours(1));
 * activity.setEndTime(LocalDateTime.now().plusDays(1));
 * }</pre>
 * 
 * @author Ai Yuyang
 * @see com.example.seckill_system.service.ISeckillActivityService
 */
@Data
@TableName("seckill_activity")
public class SeckillActivity {
    
    /**
     * Primary key, auto-incremented by database.
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * Activity name (e.g., "iPhone 15 Pro Seckill").
     * Displayed to users on the frontend.
     */
    private String name;

    /**
     * Reference to the product being sold in this activity.
     * Foreign key to the product catalog (not implemented in this system).
     */
    private Long productId;

    /**
     * Activity start time. Users cannot place orders before this time.
     * The system checks this time on both client (countdown) and server (validation).
     */
    private LocalDateTime startTime;

    /**
     * Activity end time. Users cannot place orders after this time.
     * After this time, the activity is considered finished.
     */
    private LocalDateTime endTime;

    /**
     * Original price of the product (before discount).
     * Used for display purposes to show the discount amount.
     */
    private BigDecimal originalPrice;

    /**
     * Seckill price (discounted price during the activity).
     * This is the actual price users pay when placing orders.
     */
    private BigDecimal seckillPrice;

    /**
     * Initial stock quantity when the activity was created.
     * This value never changes and is used for display (e.g., "100/100 sold").
     */
    private Integer initialStock;
    
    /**
     * Available stock quantity (remaining items).
     * This value decreases as orders are placed and is the source of truth
     * for stock validation in the database layer.
     * 
     * <p><b>Important</b>: This field is updated asynchronously via RabbitMQ
     * after Redis stock reduction succeeds. There may be a brief delay between
     * Redis stock reduction and database stock update.</p>
     */
    private Integer availableStock;

    /**
     * Timestamp when this activity was created in the system.
     * Used for auditing and activity management.
     */
    private LocalDateTime createTime;
}
