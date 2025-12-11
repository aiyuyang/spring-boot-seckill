package com.example.seckill_system.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Response Bean Enumeration - Standardized Error Codes
 * 
 * <p>This enum defines all possible response codes and messages used throughout
 * the application. It provides a centralized, type-safe way to manage error codes
 * and ensures consistency across all API endpoints.</p>
 * 
 * <p><b>Error Code Structure</b>:</p>
 * <p>Error codes follow a hierarchical structure:</p>
 * <ul>
 *   <li><b>200</b>: Success</li>
 *   <li><b>500</b>: General server error</li>
 *   <li><b>5002xx</b>: Login/Authentication module errors</li>
 *   <li><b>5005xx</b>: Seckill module errors</li>
 * </ul>
 * 
 * <p><b>Benefits</b>:</p>
 * <ul>
 *   <li><b>Type Safety</b>: Compile-time checking prevents typos in error codes</li>
 *   <li><b>Centralized Management</b>: All error codes defined in one place</li>
 *   <li><b>Consistency</b>: Same error always returns same code and message</li>
 *   <li><b>Maintainability</b>: Easy to update error messages across the system</li>
 * </ul>
 * 
 * <p><b>Usage Example</b>:</p>
 * <pre>{@code
 * // In service layer
 * if (stock <= 0) {
 *     throw new GlobalException(RespBeanEnum.EMPTY_STOCK);
 * }
 * 
 * // In controller
 * return RespBean.error(RespBeanEnum.LOGIN_ERROR);
 * }</pre>
 * 
 * <p><b>Message Language</b>:</p>
 * <p>Error messages are in Chinese as they are displayed directly to end users
 * (who are Chinese-speaking in this system). The messages are user-friendly and
 * provide clear guidance on what went wrong and how to fix it.</p>
 * 
 * @author Ai Yuyang
 * @see RespBean
 * @see com.example.seckill_system.exception.GlobalException
 */
@Getter
@ToString
@AllArgsConstructor
public enum RespBeanEnum {
    
    /**
     * Operation successful.
     * Used when a request completes successfully without errors.
     */
    SUCCESS(200, "SUCCESS"),
    
    /**
     * General server error.
     * Used for unexpected exceptions that don't fit into specific error categories.
     * This is a catch-all error code for system-level failures.
     */
    ERROR(500, "服务端异常"), // Server exception (Chinese message for frontend)

    // ========== Login/Authentication Module (5002xx) ==========
    
    /**
     * Login failed due to incorrect username or password.
     * Used when user provides invalid credentials during login attempt.
     */
    LOGIN_ERROR(500210, "用户名或密码不正确"), // Username or password incorrect
    
    /**
     * Mobile number format is invalid.
     * Used when user provides a mobile number that doesn't match the expected format.
     */
    MOBILE_ERROR(500211, "手机号码格式不正确"), // Mobile number format incorrect

    /**
     * Parameter validation failed.
     * Used when request parameters fail validation annotations (e.g., @NotNull, @NotBlank).
     * Typically caught by {@link com.example.seckill_system.exception.GlobalExceptionHandler#handleBindException}.
     */
    BIND_ERROR(500212, "参数校验异常"), // Parameter validation exception
    
    // ========== Seckill Module (5005xx) ==========
    
    /**
     * Stock is insufficient.
     * Used when a seckill request cannot be fulfilled because all items have been sold out.
     * This is the most common error in seckill scenarios.
     */
    EMPTY_STOCK(500500, "库存不足"), // Stock insufficient
    
    /**
     * Duplicate order detected.
     * Used when a user attempts to purchase the same item multiple times in the same activity.
     * This is enforced by the unique index {@code uk_activity_user} in the database.
     */
    REPEAT_ERROR(500501, "该商品每人限购一件"), // Each user can only purchase one item
    
    /**
     * Request is illegal or invalid.
     * Used when:
     * <ul>
     *   <li>Seckill token is missing or invalid</li>
     *   <li>Request parameters are malformed</li>
     *   <li>Request doesn't meet security requirements</li>
     * </ul>
     */
    REQUEST_ILLEGAL(500502, "请求非法，请重新尝试"), // Illegal request, please try again
    
    /**
     * Captcha verification failed.
     * Used when the user provides an incorrect captcha answer.
     * The captcha is required to prevent automated bot attacks.
     */
    ERROR_CAPTCHA(500503, "验证码错误，请重新输入"); // Captcha error, please re-enter

    /**
     * Numeric error code.
     * Used in API responses and for programmatic error handling.
     * Lower numbers (200) indicate success, higher numbers (500+) indicate errors.
     */
    private final Integer code;
    
    /**
     * Human-readable error message.
     * Displayed directly to users on the frontend.
     * Messages are in Chinese as the system targets Chinese-speaking users.
     */
    private final String message;
}
