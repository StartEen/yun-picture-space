package com.cloud.picture.space.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.picture.space.backend.model.entity.SpaceUser;
import com.cloud.picture.space.backend.service.SpaceUserService;
import com.cloud.picture.space.backend.mapper.SpaceUserMapper;
import org.springframework.stereotype.Service;

/**
* @author yz2025120101
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2026-01-17 14:15:30
*/
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{

}




