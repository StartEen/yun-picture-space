package com.cloud.picture.space.backend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.enums.UserRoleEnum;
import com.cloud.picture.space.backend.service.UserService;
import com.cloud.picture.space.backend.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * @author yz2025120101
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-12-18 13:40:49
 */
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

}




