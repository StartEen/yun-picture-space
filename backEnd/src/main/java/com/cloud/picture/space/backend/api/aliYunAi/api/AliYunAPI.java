package com.cloud.picture.space.backend.api.aliYunAi.api;


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


}
