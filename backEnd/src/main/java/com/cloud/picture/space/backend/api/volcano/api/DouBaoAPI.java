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
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    @Value("${volcano.visitPromptURL}")
    private String CREATE_GENERATE_PROMPT_TASK;


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


}
