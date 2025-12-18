package com.cloud.picture.space.backend.controller;


import com.cloud.picture.space.backend.common.BaseResponse;
import com.cloud.picture.space.backend.common.ResultUtils;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.model.dto.UserRegisterRequest;
import com.cloud.picture.space.backend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: LT
 * @CreateTime: 2025-12-18  14:46
 * @Description: TODO
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;


    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {

        // 校验前端传来的信息是否完整
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR, "传来的参数为空");
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);

    }


}
