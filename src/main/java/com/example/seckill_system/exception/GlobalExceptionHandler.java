package com.example.seckill_system.exception;

import com.example.seckill_system.vo.RespBean;
import com.example.seckill_system.vo.RespBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理我们手动抛出的业务异常
    @ExceptionHandler(GlobalException.class)
    public RespBean handleGlobalException(GlobalException e) {
        return RespBean.error(e.getRespBeanEnum());
    }

    // 处理参数校验异常 (比如 @NotNull)
    @ExceptionHandler(BindException.class)
    public RespBean handleBindException(BindException e) {
        RespBean respBean = RespBean.error(RespBeanEnum.BIND_ERROR);
        respBean.setMessage("参数校验异常：" + e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return respBean;
    }

    // 处理其他未知异常
    @ExceptionHandler(Exception.class)
    public RespBean handleException(Exception e) {
        e.printStackTrace(); // 打印堆栈方便排错
        return RespBean.error(RespBeanEnum.ERROR, e.getMessage());
    }
}