package com.cloud.picture.space.backend.api.volcano.model;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-03-22  11:08
 * @Description: TODO
 */
@Data
public class CreateGeneratePictureRequest implements Serializable {

    /**
     * 图像生成提示词
     * 描述希望生成的图像内容，支持中文描述
     */
    private String prompt;

    private static final long serialVersionUID = 1L;
}
