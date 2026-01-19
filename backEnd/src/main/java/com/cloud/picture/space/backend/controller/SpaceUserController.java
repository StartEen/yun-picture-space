package com.cloud.picture.space.backend.controller;


import com.cloud.picture.space.backend.common.BaseResponse;
import com.cloud.picture.space.backend.common.ResultUtils;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.model.dto.spaceUser.SpaceUserAddRequest;
import com.cloud.picture.space.backend.service.SpaceService;
import com.cloud.picture.space.backend.service.SpaceUserService;
import com.cloud.picture.space.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-19  15:51
 * @Description: TODO
 */
@Slf4j
@RestController
@RequestMapping("/spaceUser")
public class SpaceUserController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserService spaceUserService;

    /**
     * 添加成员到空间
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        long id = spaceUserService.addSpaceUser(spaceUserAddRequest);
        return ResultUtils.success(id);
    }








}
