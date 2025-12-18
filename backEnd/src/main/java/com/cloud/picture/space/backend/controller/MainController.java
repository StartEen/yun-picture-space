package com.cloud.picture.space.backend.controller;


import com.cloud.picture.space.backend.common.BaseResponse;
import com.cloud.picture.space.backend.common.ResultUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: LT
 * @CreateTime: 2025-12-18  10:40
 * @Description: TODO 主类测试
 * <p>
 * 编写健康接口
 */
@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检查
     **/
    @RequestMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("你老健康路！！！");
    }


}
