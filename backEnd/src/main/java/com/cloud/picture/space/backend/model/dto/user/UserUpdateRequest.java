package com.cloud.picture.space.backend.model.dto.user;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-18  17:28
 * @Description: TODO 【管理员使用】修改用户请求体
 */
@Data
public class UserUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

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
