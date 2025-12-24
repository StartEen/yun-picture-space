package com.cloud.picture.space.backend.manager;


import com.cloud.picture.space.backend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.demo.PutObjectDemo;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-23  17:27
 * @Description: TODO COS对象存储管理类
 * <p>
 * 提供通用的、可复用的cos对象存储操作
 */
@Component
public class CosManager {


    /**
     * 存储COS（对象存储）客户端的配置信息
     *
     */
    @Resource
    private CosClientConfig clientConfig;

    /**
     * COS客户端实例，用于执行对象存储相关操作
     * 通过@Resource注解自动注入，提供与腾讯云COS服务交互的能力
     *
     */
    @Resource
    private COSClient cosClient;


    /**
     * 上传图片对象(附带图片信息)
     *
     * @param key  对象的键（key）
     * @param file 要上传的文件
     * @return 返回一个PutObjectRequest对象，用于描述上传操作的详细信息
     */
    public PutObjectResult putPictureObject(String key, File file) {
        // 创建一个PutObjectRequest对象，用于描述上传操作的详细信息
        PutObjectRequest putObjectRequest = new PutObjectRequest(clientConfig.getBucket(), key, file);
        // 对图片进行处理（获取基本信息）
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }


    /**
     * 上传对象
     *
     * @param key  对象的键（key）
     * @param file 要上传的文件
     * @return 返回一个PutObjectRequest对象，用于描述上传操作的详细信息
     */
    public PutObjectResult putObject(String key, File file) {
        // 创建一个PutObjectRequest对象，用于描述上传操作的详细信息
        PutObjectRequest putObjectRequest = new PutObjectRequest(clientConfig.getBucket(), key, file);
        // 执行上传操作
        return cosClient.putObject(putObjectRequest);
    }


    /**
     * 获取对象
     *
     * @param key 对象的键（key）
     * @return 返回一个COSObject对象，用于描述获取到的对象信息
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(clientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }


}
