package com.cloud.picture.space.backend.api.aliYun.model.GeneratePictureUsePicture;


import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-03-23  16:07
 * @Description: 通义千问图像编辑响应类 - qwen-image-2.0-pro 模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGeneratePictureByPictureTaskResponse implements Serializable {

    /**
     * 请求唯一标识
     */
    @Alias("request_id")
    private String requestId;

    /**
     * 输出结果对象
     */
    private Output output;

    /**
     * 用量信息
     */
    private Usage usage;

    /**
     * 错误码（成功时为 null）
     */
    private String code;

    /**
     * 错误信息（成功时为 null）
     */
    private String message;

    /**
     * HTTP 状态码
     */
    @Alias("status_code")
    private Integer statusCode;

    /**
     * 输出结果类
     */
    @Data
    public static class Output implements Serializable {

        /**
         * 任务 ID
         */
        @Alias("task_id")
        private String taskId;

        /**
         * 任务状态
         * PENDING：等待中
         * RUNNING：运行中
         * SUCCEEDED：成功
         * FAILED：失败
         */
        @Alias("task_status")
        private String taskStatus;

        /**
         * 生成的内容数组（包含图片 URL）
         */
        private List<Content> content;

        /**
         * 提交时间
         */
        @Alias("submit_time")
        private String submitTime;

        /**
         * 计划时间
         */
        @Alias("scheduled_time")
        private String scheduledTime;

        /**
         * 结束时间
         */
        @Alias("end_time")
        private String endTime;

        /**
         * 任务指标
         */
        @Alias("task_metrics")
        private TaskMetrics taskMetrics;
    }

    /**
     * 生成内容类（单张图片信息）
     */
    @Data
    public static class Content implements Serializable {

        /**
         * 图片 URL
         */
        private String image;
    }

    /**
     * 任务指标类
     */
    @Data
    public static class TaskMetrics implements Serializable {

        /**
         * 总任务数
         */
        private Integer total;

        /**
         * 成功任务数
         */
        private Integer succeeded;

        /**
         * 失败任务数
         */
        private Integer failed;
    }

    /**
     * 用量信息类
     */
    @Data
    public static class Usage implements Serializable {

        /**
         * 生成的图片数量
         */
        @Alias("image_count")
        private Integer imageCount;
    }
}
