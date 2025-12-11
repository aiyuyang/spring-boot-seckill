package com.example.seckill_system.exception;

import com.example.seckill_system.vo.RespBean;
import com.example.seckill_system.vo.RespBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global Exception Handler - Centralized Exception Processing
 * 
 * <p>This class provides centralized exception handling for the entire application.
 * It uses Spring's {@code @RestControllerAdvice} annotation to intercept all exceptions
 * thrown by controller methods and convert them into standardized {@link RespBean} responses.</p>
 * 
 * <p><b>Exception Handling Strategy</b>:</p>
 * <p>The handler processes exceptions in order of specificity:</p>
 * <ol>
 *   <li><b>Business Exceptions</b>: {@link GlobalException} - Converted to RespBean with error code</li>
 *   <li><b>Validation Exceptions</b>: {@link BindException} - Parameter validation errors</li>
 *   <li><b>System Exceptions</b>: {@link Exception} - Catch-all for unexpected errors</li>
 * </ol>
 * 
 * <p><b>Benefits</b>:</p>
 * <ul>
 *   <li><b>Consistent Response Format</b>: All errors return the same RespBean structure</li>
 *   <li><b>Clean Controllers</b>: Controllers don't need try-catch blocks</li>
 *   <li><b>Centralized Logging</b>: All exceptions can be logged in one place</li>
 *   <li><b>User-Friendly Messages</b>: Business errors return user-friendly messages</li>
 * </ul>
 * 
 * <p><b>Exception Flow</b>:</p>
 * <pre>{@code
 * Controller → Service throws GlobalException
 *           ↓
 * GlobalExceptionHandler catches it
 *           ↓
 * Converts to RespBean with error code
 *           ↓
 * Returns JSON response to client
 * }</pre>
 * 
 * @author Ai Yuyang
 * @see GlobalException
 * @see RespBean
 * @see RespBeanEnum
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles business logic exceptions thrown by service layer.
     * 
     * <p>This method catches {@link GlobalException} instances that are thrown
     * throughout the application to represent business logic errors (e.g., "insufficient stock",
     * "duplicate order"). These exceptions contain a {@link RespBeanEnum} that defines
     * the error code and message.</p>
     * 
     * <p><b>Usage Example</b>:</p>
     * <pre>{@code
     * // In service layer
     * if (stock <= 0) {
     *     throw new GlobalException(RespBeanEnum.EMPTY_STOCK);
     * }
     * 
     * // This handler converts it to:
     * // { "code": 500500, "message": "库存不足", "obj": null }
     * }</pre>
     * 
     * @param e The GlobalException containing the error enum
     * @return RespBean with the error code and message from the exception
     */
    @ExceptionHandler(GlobalException.class)
    public RespBean handleGlobalException(GlobalException e) {
        return RespBean.error(e.getRespBeanEnum());
    }

    /**
     * Handles parameter validation exceptions from Spring's validation framework.
     * 
     * <p>This method catches {@link BindException} that occurs when method parameters
     * fail validation annotations such as {@code @NotNull}, {@code @NotBlank}, {@code @Min}, etc.</p>
     * 
     * <p><b>Validation Flow</b>:</p>
     * <ol>
     *   <li>Controller method receives request with invalid parameters</li>
     *   <li>Spring validation framework detects violation</li>
     *   <li>BindException is thrown with validation error details</li>
     *   <li>This handler extracts the first error message and returns it</li>
     * </ol>
     * 
     * <p><b>Example</b>:</p>
     * <pre>{@code
     * @PostMapping("/seckill")
     * public RespBean doSeckill(@RequestParam @NotNull Long activityId) {
     *     // If activityId is null, BindException is thrown
     *     // This handler returns: { "code": 500212, "message": "参数校验异常：activityId不能为空" }
     * }
     * }</pre>
     * 
     * @param e The BindException containing validation error details
     * @return RespBean with BIND_ERROR code and the first validation error message
     */
    @ExceptionHandler(BindException.class)
    public RespBean handleBindException(BindException e) {
        RespBean respBean = RespBean.error(RespBeanEnum.BIND_ERROR);
        respBean.setMessage("参数校验异常：" + e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return respBean;
    }

    /**
     * Handles all other unexpected exceptions (catch-all handler).
     * 
     * <p>This method catches any exception that is not handled by the more specific
     * handlers above. This includes:</p>
     * <ul>
     *   <li>Runtime exceptions (NullPointerException, IllegalArgumentException, etc.)</li>
     *   <li>Database exceptions (SQLException, DataAccessException, etc.)</li>
     *   <li>Network exceptions (connection timeouts, etc.)</li>
     *   <li>Any other unexpected errors</li>
     * </ul>
     * 
     * <p><b>Error Handling</b>:</p>
     * <ul>
     *   <li>Prints stack trace to console for debugging (in development)</li>
     *   <li>Returns generic error response to client (doesn't expose internal details)</li>
     *   <li>In production, should log to file/system instead of printing to console</li>
     * </ul>
     * 
     * <p><b>Security Note</b>:</p>
     * <p>The exception message is returned to the client, which may expose internal
     * system details. In production, consider sanitizing the message or returning
     * a generic error message instead.</p>
     * 
     * @param e The unexpected exception
     * @return RespBean with ERROR code and exception message
     */
    @ExceptionHandler(Exception.class)
    public RespBean handleException(Exception e) {
        e.printStackTrace(); // Print stack trace for debugging
        return RespBean.error(RespBeanEnum.ERROR, e.getMessage());
    }
}
