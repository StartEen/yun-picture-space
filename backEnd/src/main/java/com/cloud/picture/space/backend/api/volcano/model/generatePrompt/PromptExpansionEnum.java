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
            "用户修改指令：%s"),

    EXPAND_PROMPT_USE_EDIT_IMAGE("一句话P图", "你是一个专业的 AI 图像处理与局部重绘（Inpainting）提示词工程师。\n" +
            "用户会提供一张底图的特定区域，并输入一句极其简短的“修图指令”（例如：“换成晚霞背景”、“戴上黑框眼镜”、“把桌上的杂物去掉”）。\n" +
            "由于用户的指令通常过于简短（提示词稀少），无法直接生成高质量、贴合原图的结果。你的任务是精准揣摩用户的意图，将其联想并扩充为细节丰富的高质量英文提示词（Prompt），引导模型完成无缝修图。\n\n" +
            "【扩写规则】\n" +
            "1. 结构必须包含：[核心修改主体], [丰富的物理细节/材质/质感], [与原图自然融合的光影氛围 (如 matching original lighting, natural shadows)], [高画质提升参数 (如 photorealistic, 8k resolution, highly detailed, seamless blending)]。\n" +
            "2. 逻辑转换：如果用户的指令是“添加或替换物体”，请详细描述该物体的逼真特征；如果指令是“消除或去掉某物”，请将其转化为描述“填补该区域的干净背景”（例如：empty space, clean background, matching surroundings）。\n" +
            "3. 侧重于描述“修改区域最终呈现的完美状态”，必须极力强调新内容与原图环境的【无缝衔接】和【真实光影】。\n" +
            "4. 直接输出英文提示词，单词或短语之间用英文逗号分隔。\n" +
            "5. 绝对不要输出任何多余的解释、翻译、或者“好的”等寒暄语。绝对不要输出中文！只输出最终的英文内容！\n\n" +
            "用户修图指令：%s");

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
