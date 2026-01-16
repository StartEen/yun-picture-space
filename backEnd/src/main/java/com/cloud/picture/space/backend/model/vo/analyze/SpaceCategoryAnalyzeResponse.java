package com.cloud.picture.space.backend.model.vo.analyze;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-14  16:09
 * @Description: TODO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceCategoryAnalyzeResponse implements Serializable {

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片数量
     */
    private Long count;

    /**
     * 分类图片总大小
     */
    private Long totalSize;


    private static final long serialVersionUID = 1L;
}
