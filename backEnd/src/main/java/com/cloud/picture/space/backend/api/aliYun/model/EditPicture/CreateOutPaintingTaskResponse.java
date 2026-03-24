package com.cloud.picture.space.backend.api.aliYun.model.EditPicture;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-03-19  14:03
 * @Description: 扩图任务响应类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOutPaintingTaskResponse implements Serializable {

    /**
     * 任务输出信息
     */
    private Output output;


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

    }

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
     * 请求唯一标识
     * 用于请求明细溯源和问题排查
     */
    private String requestId;


}
