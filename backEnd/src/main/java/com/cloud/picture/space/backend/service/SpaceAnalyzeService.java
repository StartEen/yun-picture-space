package com.cloud.picture.space.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.picture.space.backend.model.dto.analyze.SpaceAnalyzeRequest;
import com.cloud.picture.space.backend.model.dto.analyze.SpaceUsageAnalyzeRequest;
import com.cloud.picture.space.backend.model.entity.Picture;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.vo.analyze.SpaceUsageAnalyzeResponse;

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


    /**
     * 填充图片空间数据分析查询条件
     *
     * @param spaceAnalyzeRequest 图片空间数据分析请求体
     * @param queryWrapper        查询条件
     **/
    void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper);


    /**
     * 获取图片空间使用情况
     * TODO 分析空间分为全空间、公共空间的情况（使用仅管理员可访问）与分析单个空间情况（使用普通用户可访问）
     *
     * @param spaceUsageAnalyzeRequest 图片空间数据分析请求体
     * @param loginUser                登录用户
     * @return 图片空间使用情况
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);


}
