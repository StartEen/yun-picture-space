package com.cloud.picture.space.backend.model.dto.user;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: LT
 * @CreateTime: 2025-12-18  14:19
 * @Description: TODO 用户注册请求体
 * <p>
 * 封装用户注册请求体，用于接受用户注册的参数
 */
@Data
public class UserRegisterRequest implements Serializable {

    /**
     * 用户账号
     **/
    private String userAccount;

    /**
     * 用户密码
     **/
    private String userPassword;

    /**
     * 确认密码
     **/
    private String checkPassword;

    private static final long serialVersionUID = 1L;

}
