package com.cloud.picture.space.backend.api.aliYun.model.GeneratePictureUsePrompt;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-03-29  10:58
 * @Description: TODO AI 文生图请求体 - qwen-image-2.0 模型
 */
@Data
public class createGeneratePictureUsePromptRequest implements Serializable {

    /**
     * 文字描述
     */
    private String text;

    private static final long serialVersionUID = 1L;



}
