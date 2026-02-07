package com.cloud.picture.space.backend.exception;


import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.cloud.picture.space.backend.common.BaseResponse;
import com.cloud.picture.space.backend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * @Author: LT
 * @CreateTime: 2025-12-17  17:42
 * @Description: TODO 全局异常捕获类
 *
 * 为防止意料之外的异常，
 * 利用AOP切面对全局业务和RuntimeException进行监听捕获
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 统一处理业务异常
     *
     * @param e 业务异常
     * @return 统一响应
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e){
        log.error("BusinessException",e);
        return ResultUtils.error(e.getCode(),e.getMessage());
    }

    /**
     * 统一处理运行时异常
     *
     * @param e 运行时异常
     * @return 统一响应
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e){
        log.error("RuntimeException",e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"系统错误");
    }

    /**
     * 统一处理未登录异常
     *
     * @param e 未登录异常
     * @return 统一响应
     */
    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> notLoginException(NotLoginException e){
        log.error("NotLoginException",e);
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR,e.getMessage());
    }

    /**
     * 统一处理未授权异常
     *
     * @param e 未授权异常
     * @return 统一响应
     */
    @ExceptionHandler(NotPermissionException.class)
    public BaseResponse<?> notPermissionExceptionHandler(NotPermissionException e){
        log.error("NotPermissionException",e);
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR,e.getMessage());
    }


}
