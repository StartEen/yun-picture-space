package com.cloud.picture.space.backend.manager.upload;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-29  13:41
 * @Description: TODO 网络URL图片上传子类
 * @Version: 1.0
 * <p>
 * 将图片上传的方式模版化
 * --本类是以URL地址上传为模板重写的子类
 */

@Slf4j
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "图片url不能为空");

        // 1.验证URL是否合法
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            log.error("图片url不合法", e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片url不合法");
        }
        // 2.校验URL协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
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
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPE = Arrays.asList("image/jpeg", "image/png", "image/jpg", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPE.contains(contentType),
                        ErrorCode.PARAMS_ERROR, "图片类型错误");
            }
            // 5.校验文件大小
            String contentLength = httpResponse.header("Content-Length");
            if (StrUtil.isNotBlank(contentLength)) {
                try {
                    long contentLengthNum = Long.parseLong(contentLength);
                    final long MAX_SIZE = 1024 * 1024 * 4L;
                    ThrowUtils.throwIf(contentLengthNum > MAX_SIZE,
                            ErrorCode.PARAMS_ERROR, "图片大小超出限制");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小错误");
                }
            }
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }

    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 从URL中获取文件名
        return FileUtil.getName(fileUrl);
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;
        // 下载图片到临时文件
        HttpUtil.downloadFile(fileUrl, file);
    }
}
