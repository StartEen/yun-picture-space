package com.cloud.picture.space.backend.service;

import com.cloud.picture.space.backend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;

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


}
