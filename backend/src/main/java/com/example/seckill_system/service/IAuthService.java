package com.example.seckill_system.service;

import com.example.seckill_system.vo.RespBean;

/**
 * Authentication Service Interface
 * 
 * <p>This interface defines the contract for user authentication operations,
 * including login, registration, and logout functionality.</p>
 * 
 * <p><b>Current Implementation</b>:</p>
 * <p>The current implementation ({@link com.example.seckill_system.service.impl.AuthServiceImpl})
 * uses a simplified authentication model for demonstration purposes:</p>
 * <ul>
 *   <li><b>Login</b>: Accepts any user ID (simulated authentication)</li>
 *   <li><b>Registration</b>: Placeholder (not fully implemented)</li>
 *   <li><b>Logout</b>: Returns success message (JWT is stateless, frontend handles token deletion)</li>
 * </ul>
 * 
 * <p><b>JWT Token Generation</b>:</p>
 * <p>Upon successful login, the service generates a JWT token that contains the user ID.
 * This token is used for subsequent API requests to identify the authenticated user.</p>
 * 
 * <p><b>Future Enhancements</b>:</p>
 * <p>In a production system, this service would:</p>
 * <ul>
 *   <li>Validate username and password against database</li>
 *   <li>Hash passwords using BCrypt or similar</li>
 *   <li>Implement proper user registration with validation</li>
 *   <li>Support token refresh mechanism</li>
 *   <li>Implement token blacklist for logout</li>
 * </ul>
 * 
 * @author Ai Yuyang
 * @see com.example.seckill_system.service.impl.AuthServiceImpl
 * @see com.example.seckill_system.utils.JwtUtil
 */
public interface IAuthService {
    
    /**
     * Authenticates a user and generates a JWT token.
     * 
     * <p><b>Current Implementation</b>:</p>
     * <p>This is a simplified implementation that accepts any user ID without
     * password validation. In production, this would:</p>
     * <ul>
     *   <li>Query user from database by username</li>
     *   <li>Compare password hash (BCrypt)</li>
     *   <li>Generate JWT token only if credentials are valid</li>
     * </ul>
     * 
     * @param userId The user ID (in production, this would be username)
     * @param password The password (currently not validated)
     * @return RespBean containing JWT token on success
     */
    RespBean login(Long userId, String password);
    
    /**
     * Registers a new user in the system.
     * 
     * <p><b>Current Implementation</b>:</p>
     * <p>This is a placeholder that returns success without actually creating a user.
     * In production, this would:</p>
     * <ul>
     *   <li>Validate user input (username format, password strength)</li>
     *   <li>Check if username already exists</li>
     *   <li>Hash password using BCrypt</li>
     *   <li>Insert user record into database</li>
     * </ul>
     * 
     * @param userId The user ID to register
     * @param password The password (should be hashed before storage)
     * @return RespBean indicating registration success
     */
    RespBean register(Long userId, String password);
    
    /**
     * Logs out the current user.
     * 
     * <p><b>JWT Stateless Nature</b>:</p>
     * <p>Since JWT tokens are stateless, the server cannot directly invalidate a token.
     * The current implementation simply returns a success message, and the frontend
     * is responsible for deleting the token from storage.</p>
     * 
     * <p><b>Production Alternatives</b>:</p>
     * <ul>
     *   <li><b>Token Blacklist</b>: Store invalidated tokens in Redis with TTL equal to token expiration</li>
     *   <li><b>Token Version</b>: Maintain a version number in database, increment on logout, check in interceptor</li>
     *   <li><b>Short Token TTL</b>: Use short-lived tokens (15 minutes) with refresh tokens for longer sessions</li>
     * </ul>
     * 
     * @return RespBean indicating logout success
     */
    RespBean logout();
}
