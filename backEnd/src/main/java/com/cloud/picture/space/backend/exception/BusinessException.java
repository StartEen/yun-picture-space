package com.cloud.picture.space.backend.exception;
import lombok.Getter;

/**
 * @Author: LT
 * @CreateTime: 2025-12-17  15:41
 * @Description: TODO 业务异常类
 *
 * 继承自RuntimeException
 * 用于抛出业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    /* 错误码  */
    private final int code;


    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

}
