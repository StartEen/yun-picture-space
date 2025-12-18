package com.cloud.picture.space.backend.model.dto.user;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-18  17:28
 * @Description: TODO 【管理员使用】创建用户请求体
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String userAvatar;
    /**
     * 个人简介
     */
    private String userProfile;

    /**
     * 角色：user/admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;

}
