package com.example.seckill_system.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.seckill_system.utils.JwtUtil;
import com.example.seckill_system.vo.RespBean;

/**
 * Authentication Controller - User Login
 * 
 * <p>This controller handles user authentication operations. The current implementation
 * uses a simplified authentication model for demonstration purposes, accepting any
 * numeric user ID without password validation.</p>
 * 
 * <p><b>Current Implementation</b>:</p>
 * <p>The login endpoint accepts any numeric user ID and immediately generates a JWT token.
 * This is a simplified model suitable for demonstration and testing, but not for production use.</p>
 * 
 * <p><b>Production Considerations</b>:</p>
 * <p>In a production system, this controller should:</p>
 * <ul>
 *   <li>Validate username and password against database</li>
 *   <li>Use password hashing (BCrypt) for security</li>
 *   <li>Implement rate limiting to prevent brute force attacks</li>
 *   <li>Support token refresh mechanism</li>
 *   <li>Log login attempts for security auditing</li>
 * </ul>
 * 
 * <p><b>JWT Token</b>:</p>
 * <p>Upon successful login, the system generates a JWT token containing the user ID.
 * This token is used for all subsequent API requests to identify the authenticated user.
 * The token has a configurable expiration time (default: 24 hours).</p>
 * 
 * <p><b>Request Format</b>:</p>
 * <pre>{@code
 * POST /auth/login?userId=1001
 * }</pre>
 * 
 * <p><b>Response Format</b>:</p>
 * <pre>{@code
 * {
 *   "code": 200,
 *   "message": "SUCCESS",
 *   "obj": {
 *     "token": "eyJhbGciOiJIUzUxMiJ9..."
 *   }
 * }
 * }</pre>
 * 
 * @author Ai Yuyang
 * @see JwtUtil
 * @see com.example.seckill_system.config.AuthInterceptor
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    /**
     * JWT utility for token generation.
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Simulated login endpoint.
     * 
     * <p>This endpoint accepts any numeric user ID and generates a JWT token immediately.
     * It does not perform password validation or database lookup.</p>
     * 
     * <p><b>Simplified Authentication</b>:</p>
     * <p>For demonstration purposes, this endpoint:</p>
     * <ul>
     *   <li>Accepts any non-null user ID</li>
     *   <li>Generates JWT token immediately</li>
     *   <li>Does not validate credentials</li>
     *   <li>Does not check if user exists in database</li>
     * </ul>
     * 
     * <p><b>Production Implementation Should</b>:</p>
     * <ol>
     *   <li>Query user from database by username</li>
     *   <li>Compare provided password with stored hash (BCrypt)</li>
     *   <li>Check if user account is active/enabled</li>
     *   <li>Generate token only if all validations pass</li>
     *   <li>Log login attempt (success or failure) for security auditing</li>
     * </ol>
     * 
     * <p><b>Token Structure</b>:</p>
     * <p>The generated token contains:</p>
     * <pre>{@code
     * {
     *   "userId": 1001,
     *   "exp": 1733918400  // Expiration timestamp
     * }
     * }</pre>
     * 
     * @param userId The user ID (in production, this would be username)
     * @return RespBean containing JWT token on success
     */
    @PostMapping("/login")
    public RespBean login(@RequestParam Long userId) {
        // Simplified validation: accept any non-null userId
        // In production: validate username/password against database
        
        // Generate JWT token containing user ID
        // Token expiration is configured in application.yml (default: 24 hours)
        String token = jwtUtil.generateToken(userId);
        
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return RespBean.success(result);
    }
}
