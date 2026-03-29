package com.cloud.picture.space.backend.api.aliYun.model.GeneratePictureUsePrompt;


import cn.hutool.core.annotation.Alias;
import com.cloud.picture.space.backend.api.aliYun.model.EditPicture.CreateEditPictureTaskResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-03-29  10:57
 * @Description: TODO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePictureUsePromptTaskResponse implements Serializable {

    /**
     * 请求唯一标识
     * 用于请求明细溯源和问题排查
     */
    @Alias("request_id")
    private String requestId;

    /**
     * 任务输出信息
     */
    private Output output;

    /**
     * 资源使用量统计
     */
    private Usage usage;

    /**
     * 错误码
     * 请求失败时返回，成功时不返回
     */
    private String code;

    /**
     * 错误详细信息
     * 请求失败时返回，成功时不返回
     */
    private String message;


    /**
     * 任务输出信息类
     */
    @Data
    public static class Output implements Serializable {

        /**
         * 模型生成的内容列表
         * 可以包含一个或多个 choices 对象
         */
        private List<Choice> choices;

    }

    /**
     * 选择项类
     */
    @Data
    public static class Choice implements Serializable {

        /**
         * 自然停止时输出为 stop
         */
        @Alias("finish_reason")
        private String finishReason;

        /**
         * 输出的消息
         */
        private Message message;
    }

    /**
     * 消息类
     */
    @Data
    public static class Message implements Serializable {

        /**
         * 消息的角色，固定为 assistant
         */
        private String role;

        /**
         * 消息内容列表
         */
        private List<Content> content;
    }

    /**
     * 内容项类
     */
    @Data
    public static class Content implements Serializable {

        /**
         * 模型生成图片的 URL 地址
         * 有效期 24 小时，请及时下载并保存图像
         */
        private String image;
    }


    /**
     * 资源使用量统计类
     */
    @Data
    public static class Usage implements Serializable {

        /**
         * 模型生成图片的数量
         */
        @Alias("image_count")
        private Integer imageCount;

        /**
         * 模型生成图片的宽度，单位像素
         */
        private Integer width;

        /**
         * 模型生成图片的高度，单位像素
         */
        private Integer height;
    }


}
