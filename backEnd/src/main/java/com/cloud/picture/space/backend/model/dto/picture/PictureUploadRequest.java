package com.cloud.picture.space.backend.model.dto.picture;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-24  09:32
 * @Description: TODO 图片上传请求体
 */
@Data
public class PictureUploadRequest implements Serializable {
    /**
     * 图片id(用于修改)
     */
    private Long id;


    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 图片名称
     */
    private String picName;

    private static final long serialVersionUID = -1L;
}
