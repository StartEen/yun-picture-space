package com.cloud.picture.space.backend.model.dto.analyze;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-17  11:48
 * @Description: TODO 空间排行分析
 */

@Data
public class SpaceRankAnalyzeRequest implements Serializable {

    /**
     * 排名前N的空间
     */
    private Integer topN;

    private static final long serialVersionUID = 1L;
}
