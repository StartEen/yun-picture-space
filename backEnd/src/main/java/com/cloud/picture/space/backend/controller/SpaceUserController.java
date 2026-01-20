package com.cloud.picture.space.backend.controller;


import cn.hutool.core.util.ObjectUtil;
import com.cloud.picture.space.backend.common.BaseResponse;
import com.cloud.picture.space.backend.common.DeleteRequest;
import com.cloud.picture.space.backend.common.ResultUtils;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.model.dto.spaceUser.SpaceUserAddRequest;
import com.cloud.picture.space.backend.model.dto.spaceUser.SpaceUserQueryRequest;
import com.cloud.picture.space.backend.model.entity.SpaceUser;
import com.cloud.picture.space.backend.model.vo.spaceUser.SpaceUserVo;
import com.cloud.picture.space.backend.service.SpaceService;
import com.cloud.picture.space.backend.service.SpaceUserService;
import com.cloud.picture.space.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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


    /**
     * 删除空间成员
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf((deleteRequest == null || deleteRequest.getId() <= 0), ErrorCode.PARAMS_ERROR);
        long id = deleteRequest.getId();
        // 判断是否存在
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceUserService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取某个成员在某个空间的信息
     */
    @PostMapping("/get")
    public BaseResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        // 校验参数
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
        // 查询数据库
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser);
    }


    /**
     * 获取某个空间下的所有成员
     */
    @PostMapping("/list")
    public BaseResponse<List<SpaceUserVo>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVoList(spaceUserList));
    }


}
