package com.cloud.picture.space.backend.controller;


import com.cloud.picture.space.backend.common.BaseResponse;
import com.cloud.picture.space.backend.common.ResultUtils;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.model.dto.analyze.SpaceUsageAnalyzeRequest;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.vo.analyze.SpaceUsageAnalyzeResponse;
import com.cloud.picture.space.backend.service.SpaceAnalyzeService;
import com.cloud.picture.space.backend.service.SpaceService;
import com.cloud.picture.space.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-16  17:30
 * @Description: TODO 空间分析
 */

@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {


    @Resource
    private UserService userService;

    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    /**
     * 空间资源使用分析
     */
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(@RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
                                                                        HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUsageAnalyzeResponse);
    }


}
