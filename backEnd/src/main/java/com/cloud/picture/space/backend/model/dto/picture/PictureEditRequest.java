package com.cloud.picture.space.backend.model.dto.picture;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-24  14:06
 * @Description: TODO 图片修改请求
 */
@Data
public class PictureEditRequest implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片简介
     */
    private String introduction;

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片标签
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}
