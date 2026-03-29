package com.cloud.picture.space.backend.api.aliYun.model.GeneratePictureUsePicture;


import lombok.Data;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-03-23  16:08
 * @Description: TODO 创建图生图任务请求类
 */
@Data
public class CreatePictureGeneratePictureRequest implements Serializable {

    /**
     * 文字描述（提示词）
     */
    private String text;


    private static final long serialVersionUID = -1L;
}
