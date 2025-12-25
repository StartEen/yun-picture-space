package com.cloud.picture.space.backend.model.dto.picture;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-25  10:30
 * @Description: TODO
 */
@Data
public class PictureReviewRequest implements Serializable {

    /**
     * 图片id
     */
    private Long id;

    /**
     * 审核状态：0-待审核；1-通过；2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewReason;

    private static final long serialVersionUID = 1L;
}
