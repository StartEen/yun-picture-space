package com.cloud.picture.space.backend.api.aliYunAi.api;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.cloud.picture.space.backend.api.aliYunAi.model.CreateOutPaintingTaskRequest;
import com.cloud.picture.space.backend.api.aliYunAi.model.CreateOutPaintingTaskResponse;
import com.cloud.picture.space.backend.api.aliYunAi.model.GetOutPaintingTaskResponse;
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
     * 创建任务
     *
     * @param createOutPaintingTaskRequest 创建任务参数
     * @return CreateOutPaintingTaskResponse 创建任务结果
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        if (createOutPaintingTaskRequest == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "扩图参数为空");
        }
        // 发送请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK).header(Header.AUTHORIZATION, "Bearer " + apiKey)
                // 开启异步处理
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue()).body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));
        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求失败,异常信息：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求失败,AI扩图任务失败");
            }
            CreateOutPaintingTaskResponse response = JSONUtil.toBean(httpResponse.body(), CreateOutPaintingTaskResponse.class);
            String errorCode = response.getCode();
            if (StrUtil.isNotBlank(errorCode)) {
                String errorMessage = response.getMessage();
                log.error("AI 扩图接口响应异常,异常信息：{}", errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图接口响应异常");
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
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务ID为空");
        }
        try (HttpResponse httpResponse = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL, taskId))
                .header(Header.AUTHORIZATION, "Bearer " + apiKey).execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求失败,异常信息：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求失败,查询任务失败");
            }
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }
}

