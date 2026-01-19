package com.cloud.picture.space.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.picture.space.backend.model.dto.spaceUser.SpaceUserAddRequest;
import com.cloud.picture.space.backend.model.dto.spaceUser.SpaceUserQueryRequest;
import com.cloud.picture.space.backend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.picture.space.backend.model.vo.spaceUser.SpaceUserVo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yz2025120101
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2026-01-17 14:15:30
 */
public interface SpaceUserService extends IService<SpaceUser> {


    /**
     * 校验空间成员对象
     *
     * @param spaceUser 空间成员对象
     * @param add       是否为创建
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 添加空间用户
     *
     * @param spaceUserAddRequest 添加的空间用户信息
     * @return 添加的空间用户id
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);


    /**
     * 获取空间用户查询条件
     *
     * @param spaceUserQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);


    /**
     * 获取单个空间成员视图
     *
     * @param spaceUser  空间用户
     * @param request    请求
     * @return 空间用户视图
     */
    SpaceUserVo getSpaceUserVo(SpaceUser spaceUser, HttpServletRequest request);


}
