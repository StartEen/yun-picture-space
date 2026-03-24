package com.cloud.picture.space.backend.api.aliYun.model.GeneratePictureUsePicture;


import cn.hutool.core.annotation.Alias;
import com.cloud.picture.space.backend.api.aliYun.model.EditPicture.CreateEditPictureTaskResponse;
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
     * 用于请求明细溯源和问题排查
     */
    @Alias("request_id")
    private String requestId;

    /**
     * 任务输出信息
     */
    private CreateEditPictureTaskResponse.Output output;

    /**
     * 资源使用量统计
     */
    private CreateEditPictureTaskResponse.Usage usage;

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
     * HTTP 状态码
     */
    @Alias("status_code")
    private Integer statusCode;

    /**
     * 任务输出信息类
     */
    @Data
    public static class Output implements Serializable {

        /**
         * 任务 ID
         * 查询有效期 24 小时
         */
        @Alias("task_id")
        private String taskId;

        /**
         * 任务状态
         * 枚举值：
         * - PENDING: 任务排队中
         * - RUNNING: 任务处理中
         * - SUCCEEDED: 任务执行成功
         * - FAILED: 任务执行失败
         * - CANCELED: 任务已取消
         * - UNKNOWN: 任务不存在或状态未知
         */
        @Alias("task_status")
        private String taskStatus;

        /**
         * 任务结果列表
         * 包含生成的图像 URL 或错误信息
         */
        private List<CreateEditPictureTaskResponse.ResultItem> results;

        /**
         * 任务指标统计
         */
        @Alias("task_metrics")
        private CreateEditPictureTaskResponse.TaskMetrics taskMetrics;

        /**
         * 任务提交时间
         * 格式：YYYY-MM-DD HH:mm:ss.SSS
         */
        @Alias("submit_time")
        private String submitTime;

        /**
         * 任务执行时间
         * 格式：YYYY-MM-DD HH:mm:ss.SSS
         */
        @Alias("scheduled_time")
        private String scheduledTime;

        /**
         * 任务完成时间
         * 格式：YYYY-MM-DD HH:mm:ss.SSS
         */
        @Alias("end_time")
        private String endTime;
    }

    /**
     * 任务结果项类
     */
    @Data
    public static class ResultItem implements Serializable {

        /**
         * 生成图像的 URL
         * 有效期 24 小时，需及时下载保存
         */
        private String url;

        /**
         * 错误码
         * 图像生成失败时返回
         */
        private String code;

        /**
         * 错误详细信息
         * 图像生成失败时返回
         */
        private String message;
    }

    /**
     * 任务指标统计类
     */
    @Data
    public static class TaskMetrics implements Serializable {

        /**
         * 总任务数
         */
        private Integer total;

        /**
         * 成功的任务数
         */
        private Integer succeeded;

        /**
         * 失败的任务数
         */
        private Integer failed;
    }

    /**
     * 资源使用量统计类
     */
    @Data
    public static class Usage implements Serializable {

        /**
         * 生成的图像数量
         */
        @Alias("image_count")
        private Integer imageCount;
    }
}
