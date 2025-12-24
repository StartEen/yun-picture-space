package com.cloud.picture.space.backend.model.vo;


import lombok.Data;

import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-24  18:01
 * @Description: TODO 图片标签分类列表视图
 */

@Data
public class PictureTagCategory {

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 分类列表
     */
    private List<String> categoryList;
}
