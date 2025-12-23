package com.cloud.picture.space.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.picture.space.backend.model.entity.Picture;
import com.cloud.picture.space.backend.service.PictureService;
import com.cloud.picture.space.backend.mapper.PictureMapper;
import org.springframework.stereotype.Service;

/**
* @author yz2025120101
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-12-22 18:06:56
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

}




