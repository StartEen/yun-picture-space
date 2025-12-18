package com.cloud.picture.space.backend.common;


import com.cloud.picture.space.backend.exception.ErrorCode;
import lombok.Data;


import java.io.Serializable;

/**
 * @Author: LT
 * @CreateTime: 2025-12-17  17:17
 * @Description: TODO 响应包装类
 *
 * 一般后端接口每个都要返回调用码、数据、调用信息等，
 * 前端可以根据这些信息进行相应处理
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;
    private T data;
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

}
