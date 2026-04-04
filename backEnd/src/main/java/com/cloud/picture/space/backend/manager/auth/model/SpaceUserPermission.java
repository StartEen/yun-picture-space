package com.cloud.picture.space.backend.manager.auth.model;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-20  13:13
 * @Description: TODO 空间成员权限
 */

@Data
public class SpaceUserPermission implements Serializable {

    /**
     * 权限键
     */
    private String key;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限描述
     */
    private String description;

    private static final long serialVersionUID = 1L;
}
