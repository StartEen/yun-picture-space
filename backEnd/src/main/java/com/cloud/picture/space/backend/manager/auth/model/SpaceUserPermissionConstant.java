package com.cloud.picture.space.backend.manager.auth.model;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-20  13:23
 * @Description: TODO 空间成员权限常量
 */
public interface SpaceUserPermissionConstant {

    /**
     * 空间用户管理权限
     */
    String SPACE_USER_MANAGE = "spaceUser:manage";


    /**
     * 图片查看权限
     */
    String PICTURE_VIEW = "picture:view";

    /**
     * 图片上传权限
     */
    String PICTURE_UPLOAD = "picture:upload";

    /**
     * 图片删除权限
     */
    String PICTURE_DELETE = "picture:delete";

    /**
     * 图片编辑权限
     */
    String PICTURE_EDIT = "picture:edit";

}
