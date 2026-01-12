package com.cloud.picture.space.backend.model.dto.picture;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-12  17:31
 * @Description: TODO 图片主色调搜索请求体
 */
@Data
public class SearchPictureByColorRequest implements Serializable {


    /**
     * 图片主色调
     */
    private String picColor;

    /**
     * 空间id
     */
    private Long spaceId;


    private static final long serialVersionUID = 1L;
}
