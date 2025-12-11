package com.example.seckill_system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified Response Bean for RESTful API
 * 
 * <p>This class provides a standardized response format for all API endpoints in the system.
 * It follows a consistent structure that includes status code, message, and optional data object.</p>
 * 
 * <p><b>Response Structure</b>:</p>
 * <pre>{@code
 * {
 *   "code": 200,           // Status code: 200=success, others=failure
 *   "message": "SUCCESS", // Human-readable message
 *   "obj": {}             // Optional data object (can be any type)
 * }
 * }</pre>
 * 
 * <p><b>Usage Example</b>:</p>
 * <pre>{@code
 * // Success response with data
 * return RespBean.success(user);
 * 
 * // Success response without data
 * return RespBean.success();
 * 
 * // Error response
 * return RespBean.error(RespBeanEnum.EMPTY_STOCK);
 * }</pre>
 * 
 * <p><b>Benefits</b>:</p>
 * <ul>
 *   <li>Consistent API response format across all endpoints</li>
 *   <li>Easy error handling on frontend</li>
 *   <li>Type-safe error codes via {@link RespBeanEnum}</li>
 * </ul>
 * 
 * @author Ai Yuyang
 * @see RespBeanEnum
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespBean {
    
    /**
     * Status code indicating the result of the operation.
     * <ul>
     *   <li>200: Success</li>
     *   <li>500: Server error</li>
     *   <li>5002xx: Login module errors</li>
     *   <li>5005xx: Seckill module errors</li>
     * </ul>
     */
    private long code;
    
    /**
     * Human-readable message describing the result.
     * For errors, this typically contains the error reason.
     */
    private String message;
    
    /**
     * Optional data object containing the response payload.
     * Can be any type: entity, list, map, or primitive value.
     * Null for error responses or success responses without data.
     */
    private Object obj;

    /**
     * Creates a success response without data.
     * 
     * <p>Use this when the operation succeeds but no data needs to be returned,
     * such as delete operations or status updates.</p>
     * 
     * @return RespBean with code=200, message="SUCCESS", obj=null
     */
    public static RespBean success() {
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(), null);
    }
    
    /**
     * Creates a success response with data.
     * 
     * <p>Use this when the operation succeeds and data needs to be returned,
     * such as query operations or create operations that return the created entity.</p>
     * 
     * @param obj The data object to return (can be any type)
     * @return RespBean with code=200, message="SUCCESS", obj=provided data
     */
    public static RespBean success(Object obj) {
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(), obj);
    }

    /**
     * Creates an error response using the specified error enum.
     * 
     * <p>Use this for business logic errors that are defined in {@link RespBeanEnum},
     * such as "insufficient stock" or "duplicate order".</p>
     * 
     * @param respBeanEnum The error enum containing code and message
     * @return RespBean with the error code and message, obj=null
     */
    public static RespBean error(RespBeanEnum respBeanEnum) {
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMessage(), null);
    }
    
    /**
     * Creates an error response with additional error details.
     * 
     * <p>Use this when you need to provide additional context about the error,
     * such as validation error details or exception stack traces.</p>
     * 
     * @param respBeanEnum The error enum containing code and message
     * @param obj Additional error details (e.g., validation errors, exception message)
     * @return RespBean with the error code, message, and additional details
     */
    public static RespBean error(RespBeanEnum respBeanEnum, Object obj) {
        return new RespBean(respBeanEnum.getCode(), respBeanEnum.getMessage(), obj);
    }
}
