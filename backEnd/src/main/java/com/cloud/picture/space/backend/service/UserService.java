package com.cloud.picture.space.backend.service;

import com.cloud.picture.space.backend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author yz2025120101
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-12-18 13:40:49
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取加密密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);


}
