package com.cloud.picture.space.backend.common;

import com.cloud.picture.space.backend.exception.ErrorCode;

/**
 * @Author: LT
 * @CreateTime: 2025-12-17  17:18
 * @Description: TODO 响应结果工具类
 *
 *
 * 简化响应结果的创建，
 * 提供成功调用和失败调用的方法
 *
 */

public class ResultUtils {

    /**
     * 成功
     *
     * @param data  数据
     * @param <T>  数据类型
     * @return BaseResponse 响应
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @return BaseResponse 响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     *
     * @param code 错误码
     * @param message 错误信息
     * @return BaseResponse 响应
     */
    public static BaseResponse<?> error(int code, String message){
        return new BaseResponse<>(code, null, message);
    }


    /**
     * 失败
     *
     * @param errorCode 错误码
     * @param message 错误信息
     * @return BaseResponse 响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message){
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }

}
