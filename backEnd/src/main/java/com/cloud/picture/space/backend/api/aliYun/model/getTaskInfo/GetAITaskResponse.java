package com.cloud.picture.space.backend.api.aliYun.model.getTaskInfo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-03-19  14:11
 * @Description: 查询 AI 扩图任务响应体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAITaskResponse implements Serializable {

    /**
     * 请求唯一标识
     * 用于请求明细溯源和问题排查
     */
    private String requestId;


    /**
     * 任务输出信息
     */
    private Output output;


    // /**
    //  * 资源使用量统计
    //  */
    // private Usage usage;

    /**
     * 任务输出信息
     */
    @Data
    public static class Output implements Serializable {

        /**
         * 任务 ID
         * 查询有效期 24 小时
         */
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
        private String taskStatus;

        /**
         * 任务提交时间
         * 格式：yyyy-MM-dd HH:mm:ss.SSS
         */
        private String submitTime;

        /**
         * 任务调度开始时间
         * 格式：yyyy-MM-dd HH:mm:ss.SSS
         */
        private String scheduledTime;

        /**
         * 任务结束时间
         * 格式：yyyy-MM-dd HH:mm:ss.SSS
         */
        private String endTime;

        /**
         * 生成的图像 URL
         * 有效期为 24 小时
         */
        private String outputImageUrl;


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
         * 任务指标信息
         */
        private TaskMetrics taskMetrics;

    }

    /**
     * 任务的统计信息
     */
    @Data
    private static class TaskMetrics implements Serializable {
        /**
         * 总任务数量
         */
        private Integer total;

        /**
         * 成功任务数量
         */
        private Integer succeeded;

        /**
         * 失败任务数量
         */
        private Integer failed;

    }

}
