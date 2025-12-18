package com.cloud.picture.space.backend.model.dto.user;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: LT
 * @CreateTime: 2025-12-18  15:35
 * @Description: TODO 用户登录请求类
 * <p>
 * 接收用户登录请求参数
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户账号
     */
    private String userAccount;


    /**
     * 用户密码
     */
    private String userPassword;

}
