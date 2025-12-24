package com.cloud.picture.space.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.picture.space.backend.model.dto.picture.PictureQueryRequest;
import com.cloud.picture.space.backend.model.dto.picture.PictureUploadRequest;
import com.cloud.picture.space.backend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.vo.PictureVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yz2025120101
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-12-22 18:06:56
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile        文件
     * @param pictureUploadRequest 上传图片请求体
     * @param loginUser            登录用户
     * @return 图片信息
     */
    PictureVo uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);


    /**
     * 获取图片信息,并关联用户信息
     *
     * @param picture            图片
     * @param httpServletRequest 请求
     * @return 图片信息
     */
    PictureVo getPictureVo(Picture picture, HttpServletRequest httpServletRequest);


    /**
     * 获取查询条件
     *
     * @param pictureQueryRequest 查询条件（支持从name和introduction中检索）
     * @return 查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);


    /**
     * 获取分页图片信息
     *
     * @param picturePage         分页信息
     * @param httpServletRequest  请求
     * @return 分页图片信息
     */
    Page<PictureVo> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest httpServletRequest);


    /**
     * 校验图片信息
     *
     * @param picture 图片信息
     */
    void validPicture(Picture picture);




}
