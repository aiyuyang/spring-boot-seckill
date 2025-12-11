package com.example.seckill_system.common;

/**
 * Seckill Status Enumeration
 * 
 * <p>This enum defines the possible statuses of a seckill operation result.
 * It is used to communicate the outcome of a seckill request to the frontend,
 * allowing the UI to display appropriate messages and handle different scenarios.</p>
 * 
 * <p><b>Status Flow</b>:</p>
 * <ol>
 *   <li><b>QUEUEING</b>: Request accepted, waiting for order creation</li>
 *   <li><b>SUCCESS</b>: Order created successfully</li>
 *   <li><b>SOLD_OUT</b>: Stock exhausted, seckill failed</li>
 *   <li><b>REPEATED_ORDER</b>: User already purchased, duplicate request</li>
 *   <li><b>NOT_START</b>: Activity hasn't started yet</li>
 *   <li><b>FINISHED</b>: Activity has ended</li>
 *   <li><b>ERROR</b>: System error occurred</li>
 * </ol>
 * 
 * <p><b>Usage</b>:</p>
 * <p>This enum is primarily used in the order polling mechanism. When a user
 * polls for order results, the response includes a status code that maps to
 * one of these enum values, allowing the frontend to display appropriate UI.</p>
 * 
 * @author Ai Yuyang
 * @see com.example.seckill_system.controller.OrderController#getOrderResult(Long)
 */
public enum SeckillStatusEnum {
    
    /**
     * Seckill successful, order is being processed.
     * The order has been created in the database and is ready for fulfillment.
     */
    SUCCESS(0, "秒杀成功，订单正在处理中"),
    
    /**
     * Seckill request is queued and waiting for processing.
     * The request has been accepted (Redis stock reduced), but the order
     * hasn't been created in the database yet. User should continue polling.
     */
    QUEUEING(1, "秒杀请求正在排队处理中"),
    
    /**
     * Seckill failed due to insufficient stock.
     * All available items have been sold out before this request was processed.
     */
    SOLD_OUT(2, "秒杀失败，库存不足"),
    
    /**
     * Seckill failed due to duplicate order.
     * The user has already successfully purchased an item in this activity.
     * This is enforced by the unique index {@code uk_activity_user}.
     */
    REPEATED_ORDER(3, "秒杀失败，请勿重复下单"),
    
    /**
     * Seckill failed because the activity hasn't started yet.
     * The current time is before the activity's {@code startTime}.
     */
    NOT_START(4, "秒杀失败，活动未开始"),
    
    /**
     * Seckill failed because the activity has ended.
     * The current time is after the activity's {@code endTime}.
     */
    FINISHED(5, "秒杀失败，活动已结束"),
    
    /**
     * Seckill failed due to system error.
     * An unexpected exception occurred during processing (e.g., database connection lost).
     */
    ERROR(6, "秒杀失败，系统异常");

    /**
     * Numeric status code.
     * Used in API responses and database storage.
     */
    private final int code;
    
    /**
     * Human-readable status message.
     * Displayed to users on the frontend.
     */
    private final String msg;

    /**
     * Constructs a new SeckillStatusEnum with the given code and message.
     * 
     * @param code The numeric status code
     * @param msg The human-readable message
     */
    SeckillStatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * Gets the numeric status code.
     * 
     * @return The status code
     */
    public int getCode() { 
        return code; 
    }
    
    /**
     * Gets the human-readable status message.
     * 
     * @return The status message
     */
    public String getMsg() { 
        return msg; 
    }
}
