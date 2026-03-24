package com.cloud.picture.space.backend.api.aliYun.model.EditPicture;


import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-03-19  13:54
 * @Description: AI P 图请求体 - qwen-image-2.0 模型
 */
@Data
public class CreateEditPictureTaskRequest implements Serializable {

    /**
     * 模型名称
     * 固定值：qwen-image-2.0
     */
    private String model = "qwen-image-2.0";

    /**
     * 输入参数对象
     */
    private Input input;

    /**
     * 控制图像生成的附加参数
     */
    private Parameters parameters;

    /**
     * 输入参数类
     */
    @Data
    public static class Input implements Serializable {

        /**
         * 请求内容数组
         * 仅支持单轮对话，有且只有一个对象
         */
        private List<Message> messages;
    }

    /**
     * 消息类
     */
    @Data
    public static class Message implements Serializable {

        /**
         * 消息发送者角色
         * 必须设置为 "user"
         */
        private String role = "user";

        /**
         * 消息内容数组
         * 包含 1-3 张图像和单个编辑指令
         */
        private List<ContentItem> content;
    }

    /**
     * 内容项类
     */
    @Data
    public static class ContentItem implements Serializable {

        /**
         * 图像 URL 或 Base64 数据
         * 格式：{"image": "..."}
         */
        private String image;

        /**
         * 编辑指令文本
         * 格式：{"text": "..."}
         */
        private String text;
    }

    /**
     * 图像生成参数类
     */
    @Data
    public static class Parameters implements Serializable {

        /**
         * 输出图像的数量
         * 默认值：1
         * qwen-image-2.0 系列可选择输出 1-6 张图片
         */
        private Integer n = 1;

        /**
         * 反向提示词
         * 用来描述不希望在画面中看到的内容
         * 支持中英文，长度上限 500 个字符
         */
        @Alias("negative_prompt")
        private String negativePrompt = "";

        /**
         * 是否开启提示词扩展
         * 默认值：true
         */
        @Alias("prompt_extend")
        private Boolean promptExtend = true;

        /**
         * 是否添加水印
         * 默认值：false
         */
        private Boolean watermark = false;

        /**
         * 输出图像的分辨率
         * 格式：宽*高，例如 "1024*1536"
         * 图像总像素需在 512*512 至 2048*2048 之间
         * 默认总像素数接近 1024*1024
         */
        private String size = "1080*1920";
    }
}
