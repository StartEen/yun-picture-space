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
public class SpaceSizeAnalyzeResponse implements Serializable {

    /**
     * 图片大小范围
     */
    private String sizeRange;

    /**
     * 图片数量
     */
    private Long count;

    private static final long serialVersionUID = 1L;
}
