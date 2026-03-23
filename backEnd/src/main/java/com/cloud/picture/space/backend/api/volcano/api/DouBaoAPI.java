package com.cloud.picture.space.backend.api.volcano.api;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.cloud.picture.space.backend.api.volcano.model.generatePicture.GeneratePictureTaskRequest;
import com.cloud.picture.space.backend.api.volcano.model.generatePicture.GeneratePictureTaskResponse;
import com.cloud.picture.space.backend.api.volcano.model.generatePrompt.GeneratePromptTaskRequest;
import com.cloud.picture.space.backend.api.volcano.model.generatePrompt.GeneratePromptTaskResponse;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-03-21  10:38
 * @Description: TODO
 */

@Slf4j
@Component
public class DouBaoAPI {

    @Value("${volcano.apiKey}")
    private String apiKey;

    // 创建文生图任务地址
    @Value("${volcano.visitURL}")
    private String CREATE_GENERATE_PICTURE_TASK;


    /**
     * 创建文生图任务
     *
     * @param generatePictureTaskRequest 文生图参数
     * @return GeneratePictureTaskResponse 文生图响应类
     * @Description: TODO 创建文生图任务
     */
    public GeneratePictureTaskResponse createGeneratePictureTask
    (GeneratePictureTaskRequest generatePictureTaskRequest) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(generatePictureTaskRequest),
                ErrorCode.OPERATION_ERROR, "文生图参数为空");

        // 发送请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_GENERATE_PICTURE_TASK)
                .header(Header.CONNECTION, ContentType.JSON.getValue())
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .body(JSONUtil.toJsonStr(generatePictureTaskRequest));
        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求失败,异常信息：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求失败,AI文生图任务失败");
            }
            GeneratePictureTaskResponse response = JSONUtil.toBean(httpResponse.body(), GeneratePictureTaskResponse.class);
            if (ObjectUtil.isNotEmpty(response.getError())) {
                String errorCode = response.getError().getCode();
                if (StrUtil.isNotBlank(errorCode)) {
                    String errorMessage = response.getError().getMessage();
                    log.error("AI 文生图接口响应异常,异常信息：{}", errorMessage);
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 文生图接口响应异常");
                }
            }
            return response;
        }
    }


    // 创建文生图提示词扩充任务地址
    @Value("${volcano.visitPromptURL}")
    private String CREATE_GENERATE_PROMPT_TASK;

    /**
     * 调用豆包模型扩写文生图提示词
     *
     * @param userRawInput 用户输入的简单中文描述（例如："一只戴墨镜的猫"）
     * @return 扩写后的全英文专业提示词
     */
    public String generateExpandedPrompt(String userRawInput) {

        // 1. 严格的 Prompt 模板（约束大模型只输出英文逗号分隔的标签）
        String promptTemplate = "你是一个专业的 AI 绘画提示词工程师，精通 Midjourney 和 Stable Diffusion。" +
                "请将用户的简单中文描述，扩写为高质量、细节丰富的英文提示词。" +
                "【扩写规则】" +
                "1. 结构包含：主体描述, 环境背景, 光影氛围, 艺术风格, 渲染参数（如 2k, highly detailed, unreal engine 5）。" +
                "2. 直接输出英文提示词，单词或短语之间用英文逗号分隔。" +
                "3. 绝对不要输出任何多余的解释、翻译、或者“好的”等寒暄语。只输出最终的英文内容！" +
                "用户原始描述：%s";

        String finalPrompt = String.format(promptTemplate, userRawInput);


        // 2. 组装请求对象
        GeneratePromptTaskRequest request = new GeneratePromptTaskRequest();

        GeneratePromptTaskRequest.Content content = new GeneratePromptTaskRequest.Content();
        content.setText(finalPrompt);
        content.setType("input_text");

        // 创建消息
        GeneratePromptTaskRequest.Message message = new GeneratePromptTaskRequest.Message();
        message.setRole("user");
        message.setContent(finalPrompt);  // 直接传字符串

        // 设置消息列表
        request.setMessages(java.util.Arrays.asList(message));


        // 3. 将请求对象序列化为 JSON 字符串
        String jsonBody = JSONUtil.toJsonStr(request);

        // 4. 使用 Hutool 发送 HTTP POST 请求
        try (HttpResponse response = HttpRequest.post(CREATE_GENERATE_PROMPT_TASK)
                .header(Header.CONNECTION, ContentType.JSON.getValue())
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .body(jsonBody)
                .timeout(20000) // 设置超时时间：20秒（大模型响应通常较慢）
                .execute()) {

            // 5. 检查网络请求是否成功 (HTTP 状态码 200)
            if (!response.isOk()) {
                String errorBody = response.body();
                log.error("HTTP 请求失败，状态码：{}, 响应内容：{}", response.getStatus(), errorBody);
                throw new BusinessException(ErrorCode.OPERATION_ERROR,
                        "HTTP 请求失败，状态码：" + response.getStatus() + ", 错误信息：" + errorBody);

            }

            // 6. 将响应 JSON 反序列化为你定义的 Response 实体类
            String responseBody = response.body();
            GeneratePromptTaskResponse taskResponse = JSONUtil.toBean(responseBody, GeneratePromptTaskResponse.class);

            // 7. 业务异常校验
            if (taskResponse.getError() != null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "API 业务调用失败：" + taskResponse.getError().getMessage());
            }


            // 8. 安全提取并返回最终的文本结果
            if (taskResponse.getChoices() != null && !taskResponse.getChoices().isEmpty()) {
                String resultText = taskResponse.getChoices().get(0).getMessage().getContent();
                return resultText != null ? resultText.trim() : "";
            }
            return "未获取到有效的扩写结果";
        } catch (Exception e) {
            // 捕获网络异常或 JSON 解析异常
            System.err.println("调用大模型 API 时发生异常: " + e.getMessage());
            throw e;
        }
    }

}
