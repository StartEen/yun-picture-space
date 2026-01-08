package com.cloud.picture.space.backend.controller;


import com.cloud.picture.space.backend.annotation.AuthCheck;
import com.cloud.picture.space.backend.common.BaseResponse;
import com.cloud.picture.space.backend.common.DeleteRequest;
import com.cloud.picture.space.backend.common.ResultUtils;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.model.dto.space.SpaceAddRequest;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.enums.SpaceLevelEnum;
import com.cloud.picture.space.backend.model.vo.SpaceLevel;
import com.cloud.picture.space.backend.service.SpaceService;
import com.cloud.picture.space.backend.service.UserConstant;
import com.cloud.picture.space.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
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
    @PostMapping("/updateSpace")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);


        return ResultUtils.success(true);
    }

    /**
     * 编辑空间
     */
    @PostMapping("/editSpace")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);


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




}
