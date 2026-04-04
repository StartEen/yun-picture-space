package com.cloud.picture.space.backend.manager.auth;


import com.cloud.picture.space.backend.manager.auth.model.SpaceUserPermission;
import com.cloud.picture.space.backend.manager.auth.model.SpaceUserRole;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-20  13:11
 * @Description: TODO 用于接收用户权限配置信息的值
 */
@Data
public class SpaceUserAuthConfig implements Serializable {

    /**
     * 权限列表
     */
    private List<SpaceUserPermission> permissions;

    /**
     * 角色列表
     */
    private List<SpaceUserRole> roles;

    private static final long serialVersionUID = 1L;
}
