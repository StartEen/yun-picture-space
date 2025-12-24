package com.cloud.picture.space.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.manager.FileManager;
import com.cloud.picture.space.backend.model.dto.file.UploadPictureResult;
import com.cloud.picture.space.backend.model.dto.picture.PictureUploadRequest;
import com.cloud.picture.space.backend.model.entity.Picture;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.vo.PictureVo;
import com.cloud.picture.space.backend.service.PictureService;
import com.cloud.picture.space.backend.mapper.PictureMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * @author yz2025120101
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-12-22 18:06:56
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    private final FileManager fileManager;

    public PictureServiceImpl(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    /**
     * 上传图片
     */
    @Override
    public PictureVo uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 1.校验用户是否为空
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 2.判断当前操作是新增还是修改
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 2.1如果是新增校验图片是否存在（如果不存在抛出异常）
        if (pictureId != null) {
            boolean exist = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exist, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        // 3.上传图片，得到信息
        // 3.1根据用户id划分目录
        String uploadPathPrefix = String.format("/public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        // 4.构造入库的图片信息，使用数据万象获取
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        // 如果pictureId不为空，表示更新，否则更新
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return PictureVo.objToVo(picture);
    }


}




