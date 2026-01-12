package com.cloud.picture.space.backend.model.dto.picture;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-12  15:13
 * @Description: TODO 以图搜图请求体
 */
@Data
public class SearchPictureByPictureRequest implements Serializable {
    /**
     * 图片id
     */
    private Long pictureId;

    private static final long serialVersionUID = 1L;
}
