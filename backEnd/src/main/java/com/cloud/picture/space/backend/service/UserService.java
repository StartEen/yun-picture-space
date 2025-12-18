package com.cloud.picture.space.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.picture.space.backend.model.dto.user.UserQueryRequest;
import com.cloud.picture.space.backend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.picture.space.backend.model.vo.LoginUserVo;
import com.cloud.picture.space.backend.model.vo.UserVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      请求
     * @return 脱敏后的用户信息
     */
    LoginUserVo userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户（未脱敏信息）
     *
     * @param request 请求
     * @return 当前登录用户未脱敏信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（脱敏信息）
     *
     * @param user 当前登录用户（未脱敏）
     * @return 当前登录用户（脱敏）
     */
    LoginUserVo getLoginUserVO(User user);

    /**
     * 用户注销
     *
     * @param request 请求
     * @return 是否注销成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 单个用户信息脱敏
     *
     * @param user 用户信息
     * @return 脱敏用户信息
     */
    public UserVo getUserVo(User user);


    /**
     * 用户列表信息脱敏
     *
     * @param users 用户列表
     * @return 脱敏用户列表
     */
    public List<UserVo> getUserVoList(List<User> users);


    /**
     * 用户信息分页查询
     *
     * @param userQueryRequest 用户查询条件
     * @return 查询条件
     */
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);


}
