package com.cloud.picture.space.backend.model.vo.analyze;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-14  16:11
 * @Description: TODO 用户上传行为分析响应体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceUserAnalyzeResponse implements Serializable {

    /**
     * 时间区间
     */
    private String period;

    /**
     * 上传数量
     */
    private Long count;

    private static final long serialVersionUID = 1L;
}
