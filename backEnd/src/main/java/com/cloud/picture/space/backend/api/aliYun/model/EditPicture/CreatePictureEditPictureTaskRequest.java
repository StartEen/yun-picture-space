package com.cloud.picture.space.backend.api.aliYun.model.EditPicture;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-03-20  11:27
 * @Description: TODO AI P图请求体
 */
@Data
public class CreatePictureEditPictureTaskRequest implements Serializable {

    /**
     * 图片ID
     */
    private Long pictureId;

    /**
     * 文字描述
     */
    private String text;

    private static final long serialVersionUID = 1L;
}
