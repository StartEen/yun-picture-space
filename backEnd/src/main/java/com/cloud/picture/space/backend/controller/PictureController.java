package com.cloud.picture.space.backend.controller;


import com.cloud.picture.space.backend.annotation.AuthCheck;
import com.cloud.picture.space.backend.common.BaseResponse;
import com.cloud.picture.space.backend.common.ResultUtils;
import com.cloud.picture.space.backend.model.dto.picture.PictureUploadRequest;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.vo.PictureVo;
import com.cloud.picture.space.backend.service.PictureService;
import com.cloud.picture.space.backend.service.UserConstant;
import com.cloud.picture.space.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-24  11:49
 * @Description: TODO 图片控制类
 */
@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    /**
     * 上传图片(可重新上传)
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVo> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVo pictureVo = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVo);
    }


}
