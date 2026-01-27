package com.cloud.picture.space.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.manager.auth.StpKit;
import com.cloud.picture.space.backend.model.dto.user.UserQueryRequest;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.enums.UserRoleEnum;
import com.cloud.picture.space.backend.model.vo.user.LoginUserVo;
import com.cloud.picture.space.backend.model.vo.user.UserVo;
import com.cloud.picture.space.backend.service.UserConstant;
import com.cloud.picture.space.backend.service.UserService;
import com.cloud.picture.space.backend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yz2025120101
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-12-18 13:40:49
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 用户注册
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验
        // 参数不能为空
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 账户长度不小于4
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        // 密码长度不小于8
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 2.检查是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        // long count = this.count(queryWrapper);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3.加密
        String encryptPassword = getEncryptPassword(userPassword);

        // 4.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserRole("Nobody");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败,数据库错误");
        }
        return user.getId();
    }

    /**
     * 获取加密密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 设置盐值，混稀密码
        String salt1 = "I'm old six!";
        String salt2 = "You can't get password!";
        return DigestUtils.md5DigestAsHex((salt1 + userPassword + salt2).getBytes());
    }

    /**
     * 用户登录
     */
    @Override
    public LoginUserVo userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 2.查询用户
        // 密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (ObjectUtil.isEmpty(user)) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3.记录当前用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);

        // 4. 记录用户登录态到Sa-token,便于空间鉴权时使用；
        // 注意：保证用户信息与SpringSession中的用户信息过期时间一致
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);

        return getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户（未脱敏信息）
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断当前是否已经登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        // if (ObjectUtil.isEmpty(currentUser)) {
        //     throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "当前账号未登录");
        // }
        ThrowUtils.throwIf(ObjectUtil.isEmpty(currentUser), ErrorCode.NOT_LOGIN_ERROR, "当前账号未登录");
        // 从数据库查询（追求性能的话可以注释）
        long userId = currentUser.getId();
        currentUser = this.baseMapper.selectById(userId);
        if (ObjectUtil.isEmpty(currentUser)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "当前账号未找到");
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（脱敏信息）
     */
    @Override
    public LoginUserVo getLoginUserVO(User user) {
        if (ObjectUtil.isEmpty(user)) {
            return null;
        }
        LoginUserVo loginUserVO = new LoginUserVo();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 用户注销
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断当前账户是否已经登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        ThrowUtils.throwIf(ObjectUtil.isEmpty(currentUser), ErrorCode.OPERATION_ERROR, "当前用户未登录");

        // 移除当前用户登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 单个用户信息脱敏
     */
    @Override
    public UserVo getUserVo(User user) {
        if (ObjectUtil.isEmpty(user)) {
            return null;
        }
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        return userVo;
    }

    /**
     * 用户列表信息脱敏
     */
    @Override
    public List<UserVo> getUserVoList(List<User> users) {
        if (CollUtil.isEmpty(users)) {
            return new ArrayList<>();
        }
        // 这行代码使用Stream API
        // 将users列表中的每个User对象对象通过getUserVo方法
        // 转换为脱敏的UserVo对象，并收集为新的列表
        return users.stream().map(this::getUserVo).collect(Collectors.toList());
    }

    /**
     * 用户信息分页查询
     **/
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(userQueryRequest), ErrorCode.PARAMS_ERROR);
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 是否为管理员
     **/
    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

}




