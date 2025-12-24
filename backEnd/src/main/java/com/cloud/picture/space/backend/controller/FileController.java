package com.cloud.picture.space.backend.controller;


import com.cloud.picture.space.backend.annotation.AuthCheck;
import com.cloud.picture.space.backend.common.BaseResponse;
import com.cloud.picture.space.backend.common.ResultUtils;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.manager.CosManager;
import com.cloud.picture.space.backend.service.UserConstant;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-23  17:40
 * @Description: TODO 文件测试类
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;


    /**
     * 文件上传测试
     *
     * @param multipartFile 文件
     * @return
     */
    @PostMapping("/test/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        // 文件目录
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);
        File file = null;
        // 上传文件
        try {
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问地址
            return ResultUtils.success(filepath);
        } catch (IOException e) {
            log.error("文件上传失败，filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件失败");
        } finally {
            if (file != null) {
                // 删除临时文件释放资源
                boolean delete = file.delete();
                if (!delete) {
                    log.error("文件删除失败，filepath = {}", filepath);
                }
            }
        }
    }


    /**
     * 文件下载测试
     *
     * @param filePath 文件路径
     * @param response 响应
     */
    @GetMapping("/test/download")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void testDownloadFile(String filePath, HttpServletResponse response) throws IOException {
        // COSObjectInputStream 是腾讯云COS
        // （Cloud Object Storage）SDK提供的输入流类，
        // 用于读取存储在COS上的文件内容
        COSObjectInputStream stream = null;
        try {
            COSObject cosObject = cosManager.getObject(filePath);
            // 处理下载下来的流
            stream = cosObject.getObjectContent();
            byte[] byteArray = IOUtils.toByteArray(stream);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filePath);
            // 写入响应
            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        } catch (IOException e) {
            log.error("文件下载失败，filePath = " + filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载文件失败");
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

}
