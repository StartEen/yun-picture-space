package com.cloud.picture.space.backend.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.model.entity.Space;
import com.cloud.picture.space.backend.model.enums.SpaceLevelEnum;
import com.cloud.picture.space.backend.service.SpaceService;
import com.cloud.picture.space.backend.mapper.SpaceMapper;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author yz2025120101
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2026-01-01 15:05:53
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {


    @Override
    public void validSpace(Space space, boolean add) {
        // 校验参数
        ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.PARAMS_ERROR);

        // 获取参数
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StrUtil.isNotBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(ObjectUtil.isNull(spaceLevelEnum), ErrorCode.PARAMS_ERROR, "空间等级不能为空");
        }

        // 修改数据时，如果要改空间级别
        ThrowUtils.throwIf((spaceLevel != null && spaceLevelEnum == null), ErrorCode.PARAMS_ERROR, "空间等级不能为空");
        ThrowUtils.throwIf(StrUtil.isNotBlank(spaceName) && spaceName.length() > 30, ErrorCode.PARAMS_ERROR, "空间名称不能过长");

    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }


    }


}




