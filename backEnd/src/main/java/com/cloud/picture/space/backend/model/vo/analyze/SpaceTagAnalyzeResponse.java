package com.cloud.picture.space.backend.model.vo.analyze;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-14  16:10
 * @Description: TODO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceTagAnalyzeResponse implements Serializable {

    /**
     * 标签名称
     */
    private String tag;

    /**
     * 使用数量
     */
    private Long count;

    private static final long serialVersionUID = 1L;
}
