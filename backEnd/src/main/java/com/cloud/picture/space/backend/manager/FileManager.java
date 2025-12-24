package com.cloud.picture.space.backend.manager;


import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.cloud.picture.space.backend.config.CosClientConfig;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-24  09:59
 * @Description: TODO 图片上传服务
 * <p>
 * 提供上传图片并返回图片解析的方法
 */
@Slf4j
@Service
public class FileManager {

    @Resource
    private CosClientConfig clientConfig;

    @Resource
    private CosManager cosManager;

    // 方法

    /**
     * 校验文件信息
     *
     * @param multipartFile 文件
     */
    public void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 4 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 4M");
        // 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 允许上传的文件后缀
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        // 删除临时文件
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("文件删除失败，filepath = {}", file.getAbsolutePath());
        }
    }

    /**
     * 上传图片
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 上传文件路径前缀
     * @return 图片解析结果
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验文件
        validPicture(multipartFile);
        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originFilename = multipartFile.getOriginalFilename();
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            // 上传图片
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setUrl(clientConfig.getHost() + "/" + uploadPath);
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传失败,存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件失败");
        } finally {
            this.deleteTempFile(file);
        }
    }


}
