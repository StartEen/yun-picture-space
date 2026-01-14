package com.cloud.picture.space.backend.service;

import com.cloud.picture.space.backend.model.dto.analyze.SpaceAnalyzeRequest;
import com.cloud.picture.space.backend.model.entity.User;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-14  16:29
 * @Description: TODO 针对图片空间进行数据分析
 */
public interface SpaceAnalyzeService {

    /**
     * 检查图片空间数据分析权限
     *
     * @param spaceAnalyzeRequest 图片空间数据分析请求体
     * @param loginUser           登录用户
     */
    void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser);


}
