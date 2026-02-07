package com.cloud.picture.space.backend.controller;


import com.cloud.picture.space.backend.annotation.AuthCheck;
import com.cloud.picture.space.backend.common.BaseResponse;
import com.cloud.picture.space.backend.common.DeleteRequest;
import com.cloud.picture.space.backend.common.ResultUtils;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.model.dto.space.SpaceAddRequest;
import com.cloud.picture.space.backend.model.dto.space.SpaceEditRequest;
import com.cloud.picture.space.backend.model.dto.space.SpaceUpdateRequest;
import com.cloud.picture.space.backend.model.entity.Space;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.enums.SpaceLevelEnum;
import com.cloud.picture.space.backend.model.vo.space.SpaceLevel;
import com.cloud.picture.space.backend.service.SpaceService;
import com.cloud.picture.space.backend.service.UserConstant;
import com.cloud.picture.space.backend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-01  16:39
 * @Description: TODO 空间控制类
 */

@RestController
@RequestMapping("/space")
public class SpaceController {


    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    /**
     * 创建空间
     */
    @PostMapping("/addSpace")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long newId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(newId);
    }


    /**
     * 删除空间
     */
    @PostMapping("/deleteSpace")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean delete = spaceService.deleteSpace(spaceId, loginUser);
        return ResultUtils.success(delete);
    }

    /**
     * 【管理员功能】更新空间(可修改空间级别)
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 将实体类和DTO进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        // 自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        // 数据校验
        spaceService.validSpace(space, false);

        // 判断是否存在
        Long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);

        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 编辑空间
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceEditRequest == null || spaceEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 将实体类和DTO进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        // 自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        space.setEditTime(new Date());
        // 数据校验
        spaceService.validSpace(space, false);
        // 判断是否存在
        Long id = spaceEditRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);

        // 仅本人或管理员可编辑
        spaceService.checkSpaceAuth(loginUser, oldSpace);
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取空间等级列表
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()
                )).collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }


    /**
     * 【管理员功能】根据 id 获取空间
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(space);
    }








}
