package com.cloud.picture.space.backend.model.dto.user;


import com.cloud.picture.space.backend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-18  17:28
 * @Description: TODO 【管理员使用】用户查询请求体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     **/
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;


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
