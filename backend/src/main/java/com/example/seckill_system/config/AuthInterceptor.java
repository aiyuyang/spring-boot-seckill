package com.example.seckill_system.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.seckill_system.utils.JwtUtil;

/**
 * Authentication Interceptor - JWT Token Validation
 * 
 * <p>This interceptor validates JWT tokens for all protected endpoints in the system.
 * It extracts the token from the HTTP Authorization header, validates it, and stores
 * the user ID in {@link UserContext} for use by controllers and services.</p>
 * 
 * <p><b>Authentication Flow</b>:</p>
 * <ol>
 *   <li>Request arrives with Authorization header</li>
 *   <li>Interceptor extracts token from header</li>
 *   <li>Token is parsed to extract user ID</li>
 *   <li>If valid, user ID is stored in ThreadLocal ({@link UserContext})</li>
 *   <li>Request proceeds to controller</li>
 *   <li>After completion, ThreadLocal is cleaned up</li>
 * </ol>
 * 
 * <p><b>Token Format</b>:</p>
 * <p>The interceptor expects tokens in the standard Bearer token format:</p>
 * <pre>{@code
 * Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
 * }</pre>
 * <p>However, it also accepts tokens without the "Bearer " prefix for compatibility
 * with different frontend libraries.</p>
 * 
 * <p><b>CORS Preflight Handling</b>:</p>
 * <p>The interceptor allows OPTIONS requests (CORS preflight) to pass through without
 * authentication. This is necessary for browsers to perform CORS preflight checks
 * before sending actual requests.</p>
 * 
 * <p><b>Error Handling</b>:</p>
 * <p>If authentication fails (no token, invalid token, or expired token), the interceptor:</p>
 * <ul>
 *   <li>Returns HTTP 401 (Unauthorized) status</li>
 *   <li>Returns JSON error response: {@code {"code": 401, "msg": "Invalid token, please login again"}}</li>
 *   <li>Prevents the request from reaching the controller</li>
 * </ul>
 * 
 * <p><b>ThreadLocal Cleanup</b>:</p>
 * <p>The {@code afterCompletion} method ensures that the ThreadLocal user context
 * is cleaned up after request processing, preventing memory leaks in thread pools.</p>
 * 
 * <p><b>Security Considerations</b>:</p>
 * <ul>
 *   <li>Token validation includes signature verification (prevents tampering)</li>
 *   <li>Token expiration is checked (prevents use of old tokens)</li>
 *   <li>Invalid tokens are rejected immediately (no database lookup)</li>
 *   <li>User ID is stored in ThreadLocal (not accessible to other threads)</li>
 * </ul>
 * 
 * @author Ai Yuyang
 * @see JwtUtil
 * @see UserContext
 * @see WebConfig
 */
@Component
public class AuthInterceptor implements HandlerInterceptor{
    
    /**
     * JWT utility for token parsing and validation.
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Intercepts HTTP requests before they reach the controller method.
     * 
     * <p>This method performs the following operations:</p>
     * <ol>
     *   <li>Allows OPTIONS requests (CORS preflight) to pass through</li>
     *   <li>Extracts JWT token from Authorization header</li>
     *   <li>Parses token to extract user ID</li>
     *   <li>Stores user ID in ThreadLocal for use by controllers/services</li>
     *   <li>Rejects request if token is missing or invalid</li>
     * </ol>
     * 
     * <p><b>Token Extraction</b>:</p>
     * <p>The method handles both formats:</p>
     * <ul>
     *   <li>{@code Authorization: Bearer <token>} (standard format)</li>
     *   <li>{@code Authorization: <token>} (compatibility format)</li>
     * </ul>
     * 
     * <p><b>ThreadLocal Storage</b>:</p>
     * <p>After successful token validation, the user ID is stored in {@link UserContext}.
     * This allows controllers and services to access the current user ID without
     * explicitly passing it through method parameters.</p>
     * 
     * @param request HTTP request object
     * @param response HTTP response object
     * @param handler The target handler (controller method)
     * @return true to allow request, false to reject
     * @throws Exception if an error occurs during processing
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow OPTIONS requests (CORS preflight requests)
        // Browsers send OPTIONS requests before actual requests to check CORS policy
        // These requests don't include authentication tokens, so we allow them through
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Step 1: Extract token from Authorization header
        // Standard format: Authorization: Bearer <token>
        String authHeader = request.getHeader("Authorization");

        // If no Authorization header, reject request
        if (StringUtils.isEmpty(authHeader)) {
            renderAuthError(response);
            return false;
        }

        // Handle Bearer prefix (some frontend libraries include it, some don't, handle for compatibility)
        // Remove "Bearer " prefix if present, otherwise use the header value as-is
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        // Step 2: Parse token and extract user ID
        // This validates the token signature and expiration
        // Returns null if token is invalid, expired, or tampered with
        Long userId = jwtUtil.getUserIdFromToken(token);

        // If token is invalid, reject request
        if (userId == null) {
            renderAuthError(response);
            return false;
        }

        // Step 3: Store user ID in ThreadLocal for use by controllers and services
        // This allows methods to access the current user ID via UserContext.getUser()
        // without explicitly passing it as a parameter
        UserContext.setUser(userId);

        return true;
    }

    /**
     * Cleans up ThreadLocal storage after request processing completes.
     * 
     * <p><b>Why This Is Critical</b>:</p>
     * <p>ThreadLocal values are not automatically garbage collected when a thread
     * is reused (e.g., in a thread pool). If we don't explicitly remove the value,
     * it will remain in memory and may be accessed by subsequent requests on the
     * same thread, causing data leakage and security issues.</p>
     * 
     * <p><b>When This Is Called</b>:</p>
     * <p>This method is called by Spring MVC after the request has been fully
     * processed, regardless of whether an exception occurred. This ensures cleanup
     * happens even if the controller throws an exception.</p>
     * 
     * @param request HTTP request object
     * @param response HTTP response object
     * @param handler The target handler (controller method)
     * @param ex Exception that occurred during processing (null if no exception)
     * @throws Exception if cleanup fails
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Remove user ID from ThreadLocal to prevent memory leak
        // This is critical in thread pool scenarios where threads are reused
        UserContext.removeUser();
    }

    /**
     * Renders an authentication error response.
     * 
     * <p>This method is called when authentication fails (no token or invalid token).
     * It writes a JSON error response directly to the HTTP response stream,
     * bypassing the normal controller response flow.</p>
     * 
     * <p><b>Response Format</b>:</p>
     * <pre>{@code
     * {
     *   "code": 401,
     *   "msg": "Invalid token, please login again"
     * }
     * }</pre>
     * 
     * <p><b>HTTP Status</b>:</p>
     * <p>The response uses HTTP 401 (Unauthorized) status code, which is the
     * standard status for authentication failures.</p>
     * 
     * @param response HTTP response object to write to
     * @throws Exception if writing to response fails
     */
    private void renderAuthError(HttpServletResponse response) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\": 401, \"msg\": \"Invalid token, please login again\"}");
    }
}
