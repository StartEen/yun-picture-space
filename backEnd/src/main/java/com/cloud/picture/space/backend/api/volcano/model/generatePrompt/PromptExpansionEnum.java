package com.cloud.picture.space.backend.api.volcano.model.generatePrompt;

import lombok.Getter;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-03-23  11:14
 * @Description: TODO
 */
@Getter
public enum PromptExpansionEnum {


    EXPAND_PROMPT_USE_WORDS_TO_IMAGE("文生图", "你是一个专业的 AI 绘画提示词工程师，精通 Midjourney 和 Stable Diffusion。" +
            "请将用户的简单中文描述，扩写为高质量、细节丰富的英文提示词。" +
            "【扩写规则】:1. 结构包含：主体描述, 环境背景, 光影氛围, 艺术风格, 渲染参数（如 2k, highly detailed, unreal engine 5）。" +
            "2. 直接输出英文提示词，单词或短语之间用英文逗号分隔。" +
            "3. 绝对不要输出任何多余的解释、翻译、或者“好的”等寒暄语。只输出最终的英文内容！" +
            "用户原始描述：%s"),

    EXPAND_PROMPT_USE_IMAGE_TO_IMAGE("图生图", "你是一个专业的 AI 绘画提示词工程师，精通 Stable Diffusion 和 Midjourney 的“以图生图（Image-to-Image）”工作流。\n" +
            "用户会提供一张底图，并输入一段“修改指令”或“风格转换要求”（例如：“改成赛博朋克风格”、“让画面下雪”、“转成水彩画”）。\n" +
            "请根据用户的简短指令，联想并扩写为高质量的英文提示词（Prompt），用于引导模型在保留原图构图的基础上完成重绘。\n\n" +
            "【扩写规则】\n" +
            "1. 结构必须包含：[目标核心特征/变化], [目标艺术风格/材质], [光影与渲染氛围], [色彩倾向], [高画质提升参数（如 2k, masterpiece, highly detailed, sharp focus）]。\n" +
            "2. 侧重于描述“转换后的最终视觉效果”，充分强化风格和质感标签。\n" +
            "3. 直接输出英文提示词，单词或短语之间用英文逗号分隔。\n" +
            "4. 绝对不要输出任何多余的解释、翻译、或者“好的”等寒暄语。只输出最终的英文内容！\n\n" +
            "用户修改指令：%s");


    private final String text;
    private final String value;


    PromptExpansionEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static PromptExpansionEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (PromptExpansionEnum typeEnum : PromptExpansionEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }


}
