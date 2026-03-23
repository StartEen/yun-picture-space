package com.cloud.picture.space.backend.api.volcano.model.generatePictureUseWords;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-03-22  11:06
 * @Description: TODO 文字图片生成响应类
 * <p>
 * 火山方舟图片生成 API 响应体 (非流式)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePictureTaskResponse implements Serializable {

    /**
     * 本次请求使用的模型 ID （模型名称-版本）
     */
    private String model;

    /**
     * 本次请求创建时间的 Unix 时间戳（秒）
     */
    private Long created;

    /**
     * 输出图像的信息数组。
     * 注意：即使只生成单图，也是放在数组里的。
     */
    private List<ImageData> data;


    @Data
    public static class ImageData {
        /**
         * 图片的 url 信息，当 response_format 指定为 url 时返回。
         * 注意：生成后 24 小时内失效！
         */
        private String url;

        /**
         * 图片的 base64 信息，当 response_format 指定为 b64_json 时返回。
         */
        private String b64_json;

        /**
         * 图像的宽高像素值，格式 <宽像素>x<高像素>，如 "2048x2048"
         */
        private String size;

        /**
         * 某张图片生成失败时的错误信息。
         * 若成功，此字段为 null。
         */
        private ErrorDetail error;
    }

    /**
     * 本次请求，配置的模型调用工具 (如开启了 web_search)
     */
    private List<Tool> tools;


    @Data
    public static class Tool {
        /**
         * 配置的调用工具类型，例如 "web_search"
         */
        private String type;
    }

    /**
     * 本次请求的用量信息 (用于计费核算)
     */
    private Usage usage;


    @Data
    public static class Usage {
        /**
         * 模型成功生成的图片张数
         */
        private Integer generated_images;

        /**
         * 模型生成的图片花费的 token 数量
         */
        private Integer output_tokens;

        /**
         * 本次请求消耗的总 token 数量
         */
        private Integer total_tokens;

        /**
         * 使用工具的用量信息
         */
        private ToolUsage tool_usage;
    }

    @Data
    public static class ToolUsage {
        /**
         * 调用联网搜索工具次数，仅开启联网搜索时返回
         */
        private Integer web_search;
    }

    /**
     * 本次请求，如发生全局错误，对应的错误信息
     */
    private ErrorDetail error;

    @Data
    public static class ErrorDetail {
        /**
         * 错误码
         */
        private String code;

        /**
         * 错误提示信息
         */
        private String message;
    }
}