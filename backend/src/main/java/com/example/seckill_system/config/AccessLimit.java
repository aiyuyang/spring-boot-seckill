package com.example.seckill_system.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Access Limit Annotation - Rate Limiting
 * 
 * <p>This annotation is used to mark controller methods that should be rate-limited.
 * It works in conjunction with {@link AccessLimitInterceptor} to prevent abuse
 * and protect the system from excessive requests.</p>
 * 
 * <p><b>Rate Limiting Strategy</b>:</p>
 * <p>The rate limiting is implemented using Redis with a sliding window algorithm.
 * The limit is enforced per user (identified by userId parameter) within the specified
 * time window.</p>
 * 
 * <p><b>How It Works</b>:</p>
 * <ol>
 *   <li>Interceptor checks if method has this annotation</li>
 *   <li>Extracts userId from request parameters</li>
 *   <li>Executes Redis Lua script to increment counter atomically</li>
 *   <li>If count exceeds limit, request is rejected with 500 error</li>
 *   <li>If within limit, request proceeds normally</li>
 * </ol>
 * 
 * <p><b>Usage Example</b>:</p>
 * <pre>{@code
 * @AccessLimit(second = 5, maxCount = 5)
 * @PostMapping("/seckill/doSeckill/{activityId}/{token}")
 * public RespBean doSeckill(@PathVariable Long activityId, @PathVariable String token) {
 *     // This endpoint allows maximum 5 requests per 5 seconds per user
 * }
 * }</pre>
 * 
 * <p><b>Configuration Parameters</b>:</p>
 * <ul>
 *   <li><b>second</b>: Time window in seconds (e.g., 5 = 5-second window)</li>
 *   <li><b>maxCount</b>: Maximum number of requests allowed in the time window</li>
 *   <li><b>needLogin</b>: Whether authentication is required (default: true)</li>
 * </ul>
 * 
 * <p><b>Redis Key Format</b>:</p>
 * <p>The rate limit counter is stored in Redis with the key format:</p>
 * <pre>{@code
 * seckill:limit:{userId}
 * }</pre>
 * 
 * <p><b>Benefits</b>:</p>
 * <ul>
 *   <li>Prevents brute force attacks</li>
 *   <li>Protects against API abuse</li>
 *   <li>Ensures fair resource distribution</li>
 *   <li>Reduces server load from malicious requests</li>
 * </ul>
 * 
 * @author Ai Yuyang
 * @see AccessLimitInterceptor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimit {
    
    /**
     * Time window in seconds for rate limiting.
     * 
     * <p>Example: If set to 5, the limit applies to requests within any 5-second window.</p>
     * 
     * @return The time window in seconds
     */
    int second();
    
    /**
     * Maximum number of requests allowed within the time window.
     * 
     * <p>Example: If set to 5 with second=5, user can make at most 5 requests in any 5-second period.</p>
     * 
     * @return The maximum request count
     */
    int maxCount();
    
    /**
     * Whether authentication is required for this endpoint.
     * 
     * <p>If true, the interceptor will check for user authentication before applying rate limiting.
     * If false, rate limiting is applied to all requests (useful for public endpoints).</p>
     * 
     * @return true if login is required, false otherwise (default: true)
     */
    boolean needLogin() default true;
}
