package com.cloud.picture.space.backend.api.aliYun.api;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.cloud.picture.space.backend.api.aliYun.model.EditPicture.CreateEditPictureTaskRequest;
import com.cloud.picture.space.backend.api.aliYun.model.EditPicture.CreateEditPictureTaskResponse;
import com.cloud.picture.space.backend.api.aliYun.model.GeneratePictureUsePicture.CreateGeneratePictureByPictureTaskRequest;
import com.cloud.picture.space.backend.api.aliYun.model.GeneratePictureUsePicture.CreateGeneratePictureByPictureTaskResponse;
import com.cloud.picture.space.backend.api.aliYun.model.GeneratePictureUsePicture.CreatePictureGeneratePictureRequest;
import com.cloud.picture.space.backend.api.aliYun.model.getTaskInfo.GetAITaskResponse;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-03-19  14:22
 * @Description: TODO 通过Http请求工具来调用阿里云API
 */
@Slf4j
@Component
public class AliYunAPI {

    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    // 创建任务地址
    @Value("${aliYunAi.visitURL}")
    private String CREATE_OUT_PAINTING_TASK;

    // 查询任务状态
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";


    /**
     * 创建P图任务
     *
     * @param createEditPictureTaskRequest 创建P图任务参数
     * @return CreateOutPaintingTaskResponse 创建P图任务结果
     */
    public CreateEditPictureTaskResponse createAliAIPictureTask(CreateEditPictureTaskRequest createEditPictureTaskRequest) {
        if (createEditPictureTaskRequest == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "P图参数为空");
        }

        String imageUrl = createEditPictureTaskRequest.getInput().getMessages().get(0).getContent().get(0).getImage();

        // 前置检查
        if (!ImageUrlValidator.validateImageAccessibility(imageUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片地址不可访问，请确保已开启公共读权限");
        }

        // 发送请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK).header(Header.AUTHORIZATION, "Bearer " + apiKey)
                // 开启异步处理
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue()).body(JSONUtil.toJsonStr(createEditPictureTaskRequest));
        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求失败,异常信息：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求失败,AI P图任务失败");
            }
            log.info("AI P 图接口原始响应：{}", httpResponse.body());
            CreateEditPictureTaskResponse response = JSONUtil.toBean(httpResponse.body(), CreateEditPictureTaskResponse.class);
            String errorCode = response.getCode();
            if (StrUtil.isNotBlank(errorCode)) {
                String errorMessage = response.getMessage();
                log.error("AI P图接口响应异常,异常信息：{}", errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI P图接口响应异常");
            }
            return response;
        }
    }


    /**
     * 查询任务状态
     *
     * @param taskId 任务ID
     * @return GetOutPaintingTaskResponse 查询任务结果
     */
    public GetAITaskResponse getOutPaintingTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务ID为空");
        }
        try (HttpResponse httpResponse = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL, taskId))
                .header(Header.AUTHORIZATION, "Bearer " + apiKey).execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求失败,异常信息：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求失败,查询任务失败");
            }
            return JSONUtil.toBean(httpResponse.body(), GetAITaskResponse.class);
        }
    }


    /**
     * 创建图生图任务
     */
    public CreateGeneratePictureByPictureTaskResponse createPictureGeneratePictureTask(CreateGeneratePictureByPictureTaskRequest createGeneratePictureByPictureTaskRequest) {
        if (createGeneratePictureByPictureTaskRequest == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图生图参数为空");
        }

        // 发送请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK).header(Header.AUTHORIZATION, "Bearer " + apiKey)
                // 开启异步处理
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue()).body(JSONUtil.toJsonStr(createGeneratePictureByPictureTaskRequest));
        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()){
                log.error("请求失败,异常信息：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求失败,图生图任务失败");
            }
            CreateGeneratePictureByPictureTaskResponse response = JSONUtil.toBean(httpResponse.body(), CreateGeneratePictureByPictureTaskResponse.class);
            String errorCode = response.getCode();
            if (StrUtil.isNotBlank(errorCode)) {
                String errorMessage = response.getMessage();
                log.error("图生图接口响应异常,异常信息：{}", errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "图生图接口响应异常");
            }
            return response;
        }
    }


}

