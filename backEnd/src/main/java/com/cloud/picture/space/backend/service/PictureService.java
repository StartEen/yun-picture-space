package com.cloud.picture.space.backend.service;

import com.cloud.picture.space.backend.model.dto.picture.PictureUploadRequest;
import com.cloud.picture.space.backend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.vo.PictureVo;
import org.springframework.web.multipart.MultipartFile;

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

}
