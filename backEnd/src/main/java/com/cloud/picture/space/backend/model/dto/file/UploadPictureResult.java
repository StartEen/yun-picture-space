package com.cloud.picture.space.backend.model.dto.file;


import lombok.Data;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-24  10:07
 * @Description: TODO
 */
@Data
public class UploadPictureResult {
    /**
     * 图片url
     */
    private String url;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private int picWidth;

    /**
     * 图片高度
     */
    private int picHeight;

    /**
     * 图片宽高比例
     */
    private double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 缩略图
     */
    private String thumbnailUrl;

    /**
     * 原图url
     */
    private String originalUrl;

    /**
     * 图片主色调
     */
    private String picColor;


}
