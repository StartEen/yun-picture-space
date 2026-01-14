package com.cloud.picture.space.backend.model.dto.analyze;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-14  16:23
 * @Description: TODO 公共图片分析请求
 */
@Data
public class SpaceAnalyzeRequest implements Serializable {

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 是否查询公共图库
     */
    private Boolean queryPublic;

    /**
     * 全空间分析
     */
    private Boolean queryAll;


    private static final long serialVersionUID = 1L;
}
