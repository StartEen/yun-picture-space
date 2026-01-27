package com.cloud.picture.space.backend.manager.auth.model;

import com.cloud.picture.space.backend.model.entity.Picture;
import com.cloud.picture.space.backend.model.entity.Space;
import com.cloud.picture.space.backend.model.entity.SpaceUser;
import lombok.Data;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-27  14:32
 * @Description: TODO 标识用户在特定空间内的授权上下文，不可关联的图片、空间和用户信息
 */

@Data
public class SpaceUserAuthContext {

    /**
     * 临时参数，不同请求对应的ID不同
     */
    private Long id;

    /**
     * 图片ID
     */
    private Long pictureId;

    /**
     * 空间ID
     */
    private Long spaceId;

    /**
     * 空间用户ID
     */
    private Long spaceUserId;

    /**
     * 图片信息
     */
    private Picture picture;

    /**
     * 空间信息
     */
    private Space space;

    /**
     * 空间用户信息
     */
    private SpaceUser spaceUser;
}
