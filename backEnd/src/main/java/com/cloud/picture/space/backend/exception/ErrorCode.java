package com.cloud.picture.space.backend.exception;

import lombok.Getter;

/**
 * @Author: LT
 * @CreateTime: 2025-12-17  15:26
 * @Description: TODO 错误码枚举类
 */
@Getter
public enum ErrorCode {

    SUCCESS(200, "成功"),

    PARAMS_ERROR(40000, "参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    NOT_FOUND_ERROR(40400, "未找到"),

    SYSTEM_ERROR(50000, "服务器内部系统异常"),
    OPERATION_ERROR(50001, "操作失败"),
    ;

    /**
     * 状态码
     **/
    private final int code;


    /**
     * 信息
     **/
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


}
