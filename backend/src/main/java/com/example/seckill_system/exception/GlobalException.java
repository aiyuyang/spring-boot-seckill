package com.example.seckill_system.exception;

import com.example.seckill_system.vo.RespBeanEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Global Business Exception
 * 
 * <p>This exception is used throughout the application to represent business logic errors
 * that should be returned to the client in a standardized format. It extends
 * {@link RuntimeException} to allow unchecked exception propagation.</p>
 * 
 * <p><b>Design Pattern</b>:</p>
 * <p>This follows the "Exception as Control Flow" pattern, where business logic errors
 * are represented as exceptions that are caught by {@link GlobalExceptionHandler}
 * and converted to standardized {@link com.example.seckill_system.vo.RespBean} responses.</p>
 * 
 * <p><b>Usage Example</b>:</p>
 * <pre>{@code
 * // In service layer
 * if (stock <= 0) {
 *     throw new GlobalException(RespBeanEnum.EMPTY_STOCK);
 * }
 * 
 * // Exception is caught by GlobalExceptionHandler and converted to:
 * // { "code": 500500, "message": "库存不足", "obj": null }
 * }</pre>
 * 
 * <p><b>Benefits</b>:</p>
 * <ul>
 *   <li>Clean separation of business logic and error handling</li>
 *   <li>Type-safe error codes via {@link RespBeanEnum}</li>
 *   <li>Consistent error response format</li>
 *   <li>No need for verbose if-else error handling in controllers</li>
 * </ul>
 * 
 * @author Ai Yuyang
 * @see RespBeanEnum
 * @see com.example.seckill_system.exception.GlobalExceptionHandler
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalException extends RuntimeException {
    
    /**
     * The error enum containing the error code and message.
     * This is used by {@link GlobalExceptionHandler} to construct the response.
     */
    private RespBeanEnum respBeanEnum;
}
