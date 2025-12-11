package com.example.seckill_system.config;

import java.io.PrintWriter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Access Limit Interceptor - Rate Limiting Implementation
 * 
 * <p>This interceptor implements rate limiting functionality using Redis and Lua scripts.
 * It intercepts HTTP requests and checks if they exceed the rate limit defined by
 * the {@link AccessLimit} annotation on controller methods.</p>
 * 
 * <p><b>Rate Limiting Algorithm</b>:</p>
 * <p>The interceptor uses a sliding window algorithm implemented in a Redis Lua script.
 * For each user and endpoint, it maintains a counter in Redis with a TTL equal to the
 * time window. If the counter exceeds the limit, the request is rejected.</p>
 * 
 * <p><b>How It Works</b>:</p>
 * <ol>
 *   <li>Request arrives at controller method</li>
 *   <li>Interceptor checks if method has {@link AccessLimit} annotation</li>
 *   <li>If annotated, extracts userId from request parameters</li>
 *   <li>Executes Redis Lua script to atomically increment counter</li>
 *   <li>If count > limit, rejects request with 500 error</li>
 *   <li>If count <= limit, allows request to proceed</li>
 * </ol>
 * 
 * <p><b>Redis Lua Script</b>:</p>
 * <p>The rate limiting is implemented using {@code scripts/rate_limit.lua}, which:</p>
 * <ul>
 *   <li>Atomically increments the counter</li>
 *   <li>Sets TTL on first increment (ensures counter expires after time window)</li>
 *   <li>Returns 0 if limit exceeded, 1 if within limit</li>
 * </ul>
 * 
 * <p><b>Configuration</b>:</p>
 * <p>Rate limits are configured per endpoint using the {@link AccessLimit} annotation:</p>
 * <pre>{@code
 * @AccessLimit(second = 5, maxCount = 5)
 * @PostMapping("/seckill/doSeckill/{activityId}/{token}")
 * public RespBean doSeckill(...) {
 *     // Allows 5 requests per 5 seconds per user
 * }
 * }</pre>
 * 
 * <p><b>Benefits</b>:</p>
 * <ul>
 *   <li>Prevents API abuse and brute force attacks</li>
 *   <li>Protects system resources from excessive load</li>
 *   <li>Ensures fair resource distribution among users</li>
 *   <li>Atomic operations prevent race conditions</li>
 * </ul>
 * 
 * <p><b>Current Status</b>:</p>
 * <p>The interceptor is registered but the rate limiting logic is not fully implemented
 * (marked with TODO). The Lua script is loaded, but the actual rate limit check needs
 * to be completed.</p>
 * 
 * @author Ai Yuyang
 * @see AccessLimit
 * @see WebConfig
 */
@Component
public class AccessLimitInterceptor implements HandlerInterceptor {
    
    /**
     * Redis template for executing rate limit operations.
     * Used to execute the Lua script that atomically increments counters.
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Redis Lua script for atomic rate limit counter operations.
     * 
     * <p>The script performs:</p>
     * <ol>
     *   <li>Increment counter atomically</li>
     *   <li>Set TTL on first increment (sliding window)</li>
     *   <li>Return 0 if limit exceeded, 1 if within limit</li>
     * </ol>
     */
    private DefaultRedisScript<Long> rateLimitScript;

    /**
     * Initializes the Redis Lua script after bean construction.
     * 
     * <p>Loads the rate limit Lua script from {@code scripts/rate_limit.lua}
     * and configures it for execution. The script is loaded once at startup
     * and reused for all rate limit checks.</p>
     */
    @PostConstruct
    public void init() {
        rateLimitScript = new DefaultRedisScript<>();
        rateLimitScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/rate_limit.lua")));
        rateLimitScript.setResultType(Long.class);
    }

    /**
     * Intercepts HTTP requests before they reach the controller method.
     * 
     * <p>This method is called by Spring MVC for every request. It checks if the
     * target method has the {@link AccessLimit} annotation and applies rate limiting
     * if necessary.</p>
     * 
     * <p><b>Current Implementation</b>:</p>
     * <p>The rate limiting logic is not fully implemented (marked with TODO).
     * The method currently extracts the annotation and parameters but doesn't
     * execute the rate limit check.</p>
     * 
     * <p><b>Expected Implementation</b>:</p>
     * <pre>{@code
     * // Extract userId from request
     * String userId = request.getParameter("userId");
     * String limitKey = "seckill:limit:" + userId;
     * 
     * // Execute Lua script
     * Long result = stringRedisTemplate.execute(
     *     rateLimitScript,
     *     List.of(limitKey),
     *     String.valueOf(maxCount),
     *     String.valueOf(seconds)
     * );
     * 
     * // If limit exceeded (result == 0), reject request
     * if (result == 0) {
     *     render(response, "请求过于频繁，请稍后再试");
     *     return false;
     * }
     * }</pre>
     * 
     * @param request HTTP request object
     * @param response HTTP response object
     * @param handler The target handler (controller method)
     * @return true to allow request, false to reject
     * @throws Exception if an error occurs during processing
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);

            // If method doesn't have @AccessLimit annotation, allow request
            if (accessLimit == null) {
                return true;
            }

            int seconds = accessLimit.second();
            int maxCount = accessLimit.maxCount();

            String userId = request.getParameter("userId");
            // TODO: Implement rate limiting logic
            // 1. Construct Redis key: "seckill:limit:" + userId
            // 2. Execute Lua script with maxCount and seconds parameters
            // 3. If result == 0 (limit exceeded), call render() and return false
            // 4. If result == 1 (within limit), return true
        }
        
        return true;
    }

    /**
     * Renders an error response when rate limit is exceeded.
     * 
     * <p>This method writes a JSON error response directly to the HTTP response
     * stream, bypassing the normal controller response flow. This ensures the
     * rate limit error is returned immediately without processing the request further.</p>
     * 
     * <p><b>Response Format</b>:</p>
     * <pre>{@code
     * {
     *   "code": 500,
     *   "msg": "请求过于频繁，请稍后再试"
     * }
     * }</pre>
     * 
     * <p><b>Future Improvement</b>:</p>
     * <p>Instead of hardcoding the JSON format, this should use the {@link RespBean}
     * class to ensure consistent response format across the application.</p>
     * 
     * @param response HTTP response object to write to
     * @param msg Error message to include in the response
     * @throws Exception if writing to response fails
     */
    private void render(HttpServletResponse response, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        // TODO: Hardcoded JSON format here, later should use RespBean object for consistency
        out.write("{\"code\": 500, \"msg\": \"" + msg + "\"}");
        out.flush();
        out.close();
    }
}
