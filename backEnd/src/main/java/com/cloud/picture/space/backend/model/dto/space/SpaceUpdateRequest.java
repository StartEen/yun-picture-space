package com.cloud.picture.space.backend.model.dto.space;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-01  15:39
 * @Description: TODO 空间更新请求
 */
@Data
public class SpaceUpdateRequest implements Serializable {
    /**
     * 空间id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别 ： 0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间中图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间中图片的最大数量
     */
    private Long maxCount;


    private static final long serialVersionUID = 1L;
}
