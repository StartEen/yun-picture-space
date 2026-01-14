package com.cloud.picture.space.backend.service;

import com.cloud.picture.space.backend.common.DeleteRequest;
import com.cloud.picture.space.backend.model.dto.space.SpaceAddRequest;
import com.cloud.picture.space.backend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.picture.space.backend.model.entity.User;

/**
 * @author yz2025120101
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2026-01-01 15:05:53
 */
public interface SpaceService extends IService<Space> {


    /**
     * 校验（判断是创建数据时校验，还是编辑数据时校验）
     *
     * @param space 空间
     * @param add   是否为创建
     */
    void validSpace(Space space, boolean add);


    /**
     * 填充空间信息
     *
     * @param space 空间
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 创建空间
     *
     * @param spaceAddRequest 创建空间请求
     * @param loginUser       登录用户
     * @return 新空间id
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 删除空间
     *
     * @param spaceId   空间id
     * @param loginUser 登录用户
     * @return 是否成功
     */
    boolean deleteSpace(Long spaceId, User loginUser);

    /**
     * 校验空间权限
     *
     * @param loginUser 登录用户
     * @param space     空间
     */
    void checkSpaceAuth(User loginUser, Space space);
}
