package com.cloud.picture.space.backend.api.volcano.model.generatePrompt;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-03-22  18:16
 * @Description: 提示词扩写任务响应类（基于 Chat 接口）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePromptTaskResponse implements Serializable {

    /**
     * 本次请求的唯一标识
     */
    private String id;

    /**
     * 本次请求实际使用的模型名称和版本
     */
    private String model;

    /**
     * 响应对象类型
     */
    private String object;

    /**
     * 创建时间戳
     */
    @Alias("created")
    private Long created;

    /**
     * 模型生成结果列表
     */
    private List<Choice> choices;

    /**
     * 使用量统计
     */
    private Usage usage;

    /**
     * 错误信息（请求失败时返回）
     */
    private ErrorDetail error;

    /**
     * 服务等级
     */
    @Alias("service_tier")
    private String serviceTier;

    /**
     * 选择项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice implements Serializable {
        private Integer index;
        private Message message;
        private String finishReason;
        @Alias("logprobs")
        private Object logProbs;
    }

    /**
     * 消息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message implements Serializable {
        private String role;
        private String content;
        @Alias("reasoning_content")
        private String reasoningContent;
    }

    /**
     * 使用量
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage implements Serializable {
        @Alias("prompt_tokens")
        private Integer promptTokens;
        @Alias("completion_tokens")
        private Integer completionTokens;
        @Alias("total_tokens")
        private Integer totalTokens;
        @Alias("prompt_tokens_details")
        private PromptTokensDetails promptTokensDetails;
        @Alias("completion_tokens_details")
        private CompletionTokensDetails completionTokensDetails;
    }

    /**
     * 提示词 token 详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptTokensDetails implements Serializable {
        @Alias("cached_tokens")
        private Integer cachedTokens;
    }

    /**
     * 完成 token 详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletionTokensDetails implements Serializable {
        @Alias("reasoning_tokens")
        private Integer reasoningTokens;
    }

    /**
     * 错误详情对象
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail implements Serializable {
        private String code;
        private String message;
        private String param;
        private String type;
    }

    private static final long serialVersionUID = 1L;
}
