package com.cloud.picture.space.backend.manager.upload;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.cloud.picture.space.backend.config.CosClientConfig;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.manager.CosManager;
import com.cloud.picture.space.backend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-29  10:44
 * @Description: TODO 将图片上传的方式模版化
 */

@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    protected CosManager cosManager;

    @Resource
    protected CosClientConfig clientConfig;


    /**
     * 模版方法，定义上传流程
     *
     * @param inputSource      输入源: 1.文件; 2.url;
     * @param uploadPathPrefix 上传文件路径前缀
     * @return 图片解析结果
     */
    public final UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1.校验输入源
        validPicture(inputSource);

        // 2. 上传图片地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginalFilename(inputSource);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);

        File file = null;
        try {
            // 3. 创建临时文件
            file = File.createTempFile(uploadPath, null);
            // 处理文件来源
            processFile(inputSource, file);

            // 4.上传图片到对象存储中
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                CIObject ciObject = objectList.get(0);
                return buildResult(originalFilename, ciObject);
            }


            // 5. 封装返回结果
            return buildResult(originalFilename, file, uploadPath, imageInfo);

        } catch (Exception e) {
            log.error("图片上传失败,存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件失败");
        } finally {
            // 6. 清理缓存
            deleteTemplateFile(file);
        }


    }

    /**
     * 校验数据源（本地文件活或URL）
     */
    protected abstract void validPicture(Object inputSource);


    /**
     * 获取输入源的原始文件名
     */
    protected abstract String getOriginalFilename(Object inputSource);


    /**
     * 处理输入源并生成本地文件
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;


    /**
     * 封装返回结果
     *
     * @version 1.0
     */
    private UploadPictureResult buildResult(String originalFilename, File file, String uploadPath, ImageInfo imageInfo) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setUrl(clientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        return uploadPictureResult;
    }

    /**
     * 封装返回结果
     *
     * @version 2.0
     */
    private UploadPictureResult buildResult(String originalFilename, CIObject ciObject) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = ciObject.getWidth();
        int picHeight = ciObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(ciObject.getFormat());
        uploadPictureResult.setPicSize(ciObject.getSize().longValue());
        uploadPictureResult.setUrl(clientConfig.getHost() + "/" + ciObject.getKey());
        return uploadPictureResult;
    }


    /**
     * 清理缓存文件
     */
    public void deleteTemplateFile(File file) {
        if (file == null) {
            return;
        }
        boolean delete = file.delete();
        if (!delete) {
            log.error("删除缓存文件失败,file:{}", file.getAbsolutePath());
        }
    }


}
