package com.example.seckill_system.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration - Interceptors and CORS
 * 
 * <p>This configuration class customizes Spring MVC behavior by registering
 * interceptors and configuring CORS (Cross-Origin Resource Sharing) settings.</p>
 * 
 * <p><b>Interceptors Registered</b>:</p>
 * <ol>
 *   <li><b>Authentication Interceptor</b>: Validates JWT tokens for protected endpoints</li>
 *   <li><b>Rate Limit Interceptor</b>: Enforces rate limiting on annotated endpoints</li>
 * </ol>
 * 
 * <p><b>Interceptor Order</b>:</p>
 * <p>Interceptors are executed in registration order:</p>
 * <ol>
 *   <li>Authentication Interceptor (validates token first)</li>
 *   <li>Rate Limit Interceptor (applies rate limiting)</li>
 * </ol>
 * 
 * <p><b>Whitelist Endpoints</b>:</p>
 * <p>Certain endpoints are excluded from authentication to allow public access:</p>
 * <ul>
 *   <li>{@code /auth/**}: Login endpoint (no authentication required)</li>
 *   <li>{@code /swagger-ui/**}: Swagger documentation (development only)</li>
 *   <li>{@code /error}: Spring Boot error pages</li>
 *   <li>{@code /favicon.ico}: Browser icon requests</li>
 * </ul>
 * 
 * <p><b>CORS Configuration</b>:</p>
 * <p>The CORS configuration allows cross-origin requests from any origin for development.
 * In production, this should be restricted to specific frontend domains for security.</p>
 * 
 * @author Ai Yuyang
 * @see AuthInterceptor
 * @see AccessLimitInterceptor
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * Rate limit interceptor for enforcing request rate limits.
     */
    @Autowired
    private AccessLimitInterceptor accessLimitInterceptor;

    /**
     * Authentication interceptor for JWT token validation.
     */
    @Autowired
    private AuthInterceptor authInterceptor;

    /**
     * Registers interceptors for request processing.
     * 
     * <p>This method configures the interceptor chain that processes all HTTP requests.
     * Interceptors are executed in registration order before the request reaches the controller.</p>
     * 
     * <p><b>Interceptor Chain</b>:</p>
     * <ol>
     *   <li><b>Authentication Interceptor</b>:
     *       <ul>
     *         <li>Validates JWT token from Authorization header</li>
     *         <li>Extracts user ID and stores in ThreadLocal</li>
     *         <li>Rejects requests without valid tokens</li>
     *       </ul>
     *   </li>
     *   <li><b>Rate Limit Interceptor</b>:
     *       <ul>
     *         <li>Checks if endpoint has {@link AccessLimit} annotation</li>
     *         <li>Enforces rate limits using Redis Lua scripts</li>
     *         <li>Rejects requests that exceed the limit</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <p><b>Whitelist Strategy</b>:</p>
     * <p>Endpoints in the whitelist bypass authentication but may still be subject to
     * rate limiting (if annotated). This allows public endpoints like login and Swagger
     * documentation to be accessible without authentication.</p>
     * 
     * @param registry The interceptor registry to add interceptors to
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Step 1: Define whitelist (endpoints that don't require authentication)
        // These endpoints can be accessed without a JWT token
        List<String> excludePaths = new ArrayList<>();
        excludePaths.add("/auth/**");     // Login endpoint (must be public)
        excludePaths.add("/seckill/list");   // Public activity list page (if exists)
        excludePaths.add("/favicon.ico");    // Browser icon requests
        excludePaths.add("/error");          // Spring Boot default error page

        // Swagger whitelist (development/documentation endpoints)
        excludePaths.add("/swagger-ui/**");      // Swagger UI pages
        excludePaths.add("/v3/api-docs/**");     // API JSON data (OpenAPI specification)
        excludePaths.add("/swagger-resources/**"); // Swagger static resources
        excludePaths.add("/webjars/**");         // WebJars static resources (CSS, JS)

        // Step 2: Register authentication interceptor
        // This interceptor validates JWT tokens and extracts user ID
        // It applies to all paths except those in the whitelist
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")  // Apply to all paths
                .excludePathPatterns(excludePaths);  // Except whitelisted paths

        // Step 3: Register rate limit interceptor
        // This interceptor enforces rate limits on endpoints annotated with @AccessLimit
        // It applies to all paths (no exclusions, as rate limiting is opt-in via annotation)
        registry.addInterceptor(accessLimitInterceptor)
                .addPathPatterns("/**");
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings.
     * 
     * <p>This method enables cross-origin requests from the frontend application,
     * which typically runs on a different port (e.g., localhost:5173) than the backend
     * (e.g., localhost:8081).</p>
     * 
     * <p><b>Current Configuration</b>:</p>
     * <ul>
     *   <li><b>Allowed Origins</b>: All origins ({@code *}) - suitable for development</li>
     *   <li><b>Allowed Methods</b>: GET, POST, PUT, DELETE, OPTIONS</li>
     *   <li><b>Allowed Headers</b>: All headers ({@code *})</li>
     *   <li><b>Credentials</b>: Disabled (required when using {@code *} for origins)</li>
     *   <li><b>Max Age</b>: 3600 seconds (1 hour) - how long preflight responses are cached</li>
     * </ul>
     * 
     * <p><b>Production Considerations</b>:</p>
     * <p>In production, this configuration should be more restrictive:</p>
     * <ul>
     *   <li>Specify exact frontend domains instead of {@code *}</li>
     *   <li>Enable credentials if cookies/sessions are used</li>
     *   <li>Restrict allowed headers to only those needed</li>
     *   <li>Consider using environment-specific configurations</li>
     * </ul>
     * 
     * <p><b>CORS Preflight</b>:</p>
     * <p>Browsers send OPTIONS requests (preflight) before certain cross-origin requests.
     * The {@link AuthInterceptor} allows these requests to pass through without authentication,
     * enabling the CORS preflight to complete successfully.</p>
     * 
     * <p><b>Important Note</b>:</p>
     * <p>When using {@code allowedOriginPatterns("*")}, you cannot use {@code allowCredentials(true)}
     * at the same time due to browser security restrictions. If credentials are needed in production,
     * specify exact domain names instead of using wildcards.</p>
     * 
     * @param registry The CORS registry to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Allow all origins (development only)
                // Production should specify: Arrays.asList("https://yourdomain.com", "https://www.yourdomain.com")
                .allowedOriginPatterns("*")
                
                // Allow standard HTTP methods
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                
                // Allow all headers (development only)
                // Production should restrict to: "Authorization", "Content-Type", etc.
                .allowedHeaders("*")
                
                // Credentials disabled (required when using "*" for origins)
                // For production with credentials, specify exact domains and set to true
                .allowCredentials(false)
                
                // Cache preflight responses for 1 hour
                // Reduces number of preflight requests from browsers
                .maxAge(3600);
    }
}
