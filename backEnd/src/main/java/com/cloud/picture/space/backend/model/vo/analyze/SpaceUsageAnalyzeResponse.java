package com.cloud.picture.space.backend.model.vo.analyze;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-14  16:11
 * @Description: TODO 空间使用情况请求相应视图类
 */
@Data
public class SpaceUsageAnalyzeResponse implements Serializable {

    /**
     * 空间已使用情况
     */
    private Long usedSize;

    /**
     * 空间总大小
     */
    private Long maxSize;


    /**
     * 空间使用比例
     */
    private Double sizeUsageRatio;

    /**
     * 当前图片数量
     */
    private Long usedCount;

    /**
     * 最大图片数量
     */
    private Long maxCount;

    /**
     * 图片数量占用比例
     */
    private Double countUsageRatio;


    private static final long serialVersionUID = 1L;
}
