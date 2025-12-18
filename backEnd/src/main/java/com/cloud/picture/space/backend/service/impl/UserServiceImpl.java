package com.cloud.picture.space.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.service.UserService;
import com.cloud.picture.space.backend.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author yz2025120101
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-12-18 13:40:49
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




