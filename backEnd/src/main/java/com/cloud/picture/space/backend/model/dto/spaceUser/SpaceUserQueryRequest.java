package com.cloud.picture.space.backend.model.dto.spaceUser;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-19  09:45
 * @Description: TODO 查询空间成员请求
 */
@Data
public class SpaceUserQueryRequest implements Serializable {


    /**
     * id
     */
    private Long id;


    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 用户id
     */
    private Long userId;


    /**
     * 空间角色：viewer-浏览者，editor-编辑者，admin-管理员
     */
    private String spaceRole;

    private static final long serialVersionUID = -1L;
}
