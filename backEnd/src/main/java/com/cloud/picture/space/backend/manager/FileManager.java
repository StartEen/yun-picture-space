package com.cloud.picture.space.backend.manager;


import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.cloud.picture.space.backend.config.CosClientConfig;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
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

    /**
     * 通过url上传图片
     *
     * @param fileUrl          图片url
     * @param uploadPathPrefix 上传文件路径前缀
     * @return 图片解析结果
     */
    public UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix) {

        // 校验图片


        //
        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        // String originFilename = multipartFile.getOriginalFilename();
        String originFilename = FileUtil.getName(fileUrl);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(uploadPath, null);
            // multipartFile.transferTo(file);
            HttpUtil.downloadFile(fileUrl, file);
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


    /**
     * 校验图片
     *
     */
    private void validPicture(String fileUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "图片url不能为空");

        // 1.验证URL是否合法
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            log.error("图片url不合法", e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片url不合法");
        }
        // 2.校验URL协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") || !fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR, "图片url协议错误");
        // 3.发送HEAD请求验证文件是否存在
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            // 倘若未正常返回，无需执行其他判断
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片不存在");
            }
            // 4.校验文件类型
            String contentType = httpResponse.header("Content-Type");
            if (StrUtil.isBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPE = Arrays.asList("image/jpeg", "image/png", "image/jpg", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPE.contains(contentType),
                        ErrorCode.PARAMS_ERROR, "图片类型错误");
            }
            // 5.校验文件大小
            String contentLength = httpResponse.header("Content-Length");
            if (StrUtil.isNotBlank(contentLength)){
                try {
                    long contentLengthNum = Long.parseLong(contentLength);
                    final long MAX_SIZE = 1024 * 1024 * 4L;
                    ThrowUtils.throwIf(contentLengthNum > MAX_SIZE,
                        ErrorCode.PARAMS_ERROR, "图片大小超出限制");
                }catch (NumberFormatException e){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小错误");
                }
            }
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }

    }


}
