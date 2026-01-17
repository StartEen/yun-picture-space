package com.cloud.picture.space.backend.model.dto.analyze;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-17  10:15
 * @Description: TODO 用户上传行为分析请求体
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceUserAnalyzeRequest extends SpaceUsageAnalyzeRequest {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 时间维度：1.day（日） 2.week(周) 3.month(月) 4.year(年)
     */
    private String timeDimension;
}
