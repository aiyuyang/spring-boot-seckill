package com.example.seckill_system.config;

/**
 * User Context Utility - Thread-Local Storage for Current User
 * 
 * <p>This class provides thread-local storage for the current authenticated user ID.
 * It is used to pass user information from the interceptor layer to the service layer
 * without explicitly passing it through method parameters.</p>
 * 
 * <p><b>Thread Safety</b>:</p>
 * <p>This class uses {@link ThreadLocal} to ensure thread safety. Each thread has its
 * own isolated copy of the user ID, preventing data leakage between concurrent requests.</p>
 * 
 * <p><b>Lifecycle</b>:</p>
 * <ol>
 *   <li><b>Set</b>: {@link AuthInterceptor} extracts user ID from JWT token and calls {@link #setUser(Long)}</li>
 *   <li><b>Get</b>: Service layer calls {@link #getUser()} to retrieve current user ID</li>
 *   <li><b>Remove</b>: {@link AuthInterceptor#afterCompletion} calls {@link #removeUser()} to clean up</li>
 * </ol>
 * 
 * <p><b>Usage Example</b>:</p>
 * <pre>{@code
 * // In controller (automatically set by AuthInterceptor)
 * public RespBean doSeckill(@PathVariable Long activityId) {
 *     Long userId = UserContext.getUser(); // Get from ThreadLocal
 *     return seckillService.doSeckill(userId, activityId);
 * }
 * }</pre>
 * 
 * <p><b>Important Notes</b>:</p>
 * <ul>
 *   <li>Always call {@link #removeUser()} in the interceptor's {@code afterCompletion}
 *       method to prevent memory leaks</li>
 *   <li>This pattern is only safe in synchronous request handling. For async operations,
 *       user context must be explicitly passed or stored differently</li>
 *   <li>In a distributed system, consider using request headers or session storage instead</li>
 * </ul>
 * 
 * @author Ai Yuyang
 * @see AuthInterceptor
 */
public class UserContext {
    
    /**
     * Thread-local storage for the current user ID.
     * Each thread has its own isolated copy, ensuring thread safety.
     */
    private static final ThreadLocal<Long> userHolder = new ThreadLocal<>();

    /**
     * Sets the current user ID for this thread.
     * 
     * <p>This is typically called by {@link AuthInterceptor} after successfully
     * validating the JWT token.</p>
     * 
     * @param userId The authenticated user's ID
     */
    public static void setUser(Long userId) {
        userHolder.set(userId);
    }

    /**
     * Gets the current user ID for this thread.
     * 
     * <p>This is typically called by controllers or services that need to know
     * which user is making the request.</p>
     * 
     * @return The current user's ID, or null if not set
     */
    public static Long getUser() {
        return userHolder.get();
    }

    /**
     * Removes the current user ID from this thread.
     * 
     * <p><b>Critical</b>: This must be called in the interceptor's {@code afterCompletion}
     * method to prevent memory leaks. ThreadLocal values are not automatically garbage
     * collected when the thread is reused (e.g., in a thread pool).</p>
     */
    public static void removeUser() {
        userHolder.remove();
    }
}
