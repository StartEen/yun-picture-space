package com.cloud.picture.space.backend.model.dto.picture;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-24  14:06
 * @Description: TODO 图片批量修改请求
 */
@Data
public class PictureEditByBatchRequest implements Serializable {
    /**
     * 图片id列表
     */
    private List<Long> pictureIdList;


    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片标签
     */
    private List<String> tags;


    /**
     * 命名规则
     */
    private String nameRule;


    private static final long serialVersionUID = 1L;
}
