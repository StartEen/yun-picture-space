package com.cloud.picture.space.backend.api.volcano.generatePromptModel;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-03-22  18:16
 * @Description: TODO
 */
@Data
public class GeneratePromptTaskRequest implements Serializable {

    // doubao-seed-2-0-mini-260215
    private String model ="doubao-seed-2-0-mini-260215";

    /**
     * 对话消息列表（数组）
     */
    private List<Message> messages;

    /**
     * 消息对象
     */
    @Data
    public static class Message implements Serializable {

        /**
         * 消息角色
         */
        private String role = "user";

        /**
         * 消息内容（字符串或数组）
         */
        private Object content;
    }

    @Data
    public static class Content implements Serializable {

        private String text;

        private String type = "input_text";

    }

}
