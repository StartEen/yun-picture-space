package com.cloud.picture.space.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.model.dto.spaceUser.SpaceUserAddRequest;
import com.cloud.picture.space.backend.model.dto.spaceUser.SpaceUserQueryRequest;
import com.cloud.picture.space.backend.model.entity.Space;
import com.cloud.picture.space.backend.model.entity.SpaceUser;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.enums.SpaceRoleEnum;
import com.cloud.picture.space.backend.model.enums.SpaceTypeEnum;
import com.cloud.picture.space.backend.model.vo.space.SpaceVo;
import com.cloud.picture.space.backend.model.vo.spaceUser.SpaceUserVo;
import com.cloud.picture.space.backend.model.vo.user.UserVo;
import com.cloud.picture.space.backend.service.SpaceService;
import com.cloud.picture.space.backend.service.SpaceUserService;
import com.cloud.picture.space.backend.mapper.SpaceUserMapper;
import com.cloud.picture.space.backend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yz2025120101
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * @createDate 2026-01-17 14:15:30
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    /**
     * 校验空间用户关联
     */
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        // 创建时，空间ID和用户ID必填
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        if (add) {
            ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }

        // 校验空间角色
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        ThrowUtils.throwIf((spaceRole != null && spaceRoleEnum == null), ErrorCode.PARAMS_ERROR, "空间角色不存在");
    }

    /**
     * 添加空间用户关联
     */
    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);

        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);

        validSpaceUser(spaceUser, true);

        boolean result = this.save(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return spaceUser.getId();
    }

    /**
     * 获取查询条件
     */
    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        if (spaceUserQueryRequest == null) {
            return queryWrapper;
        }
        // 取值；
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();

        // 封装判断
        queryWrapper.eq(ObjectUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjectUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtil.isNotEmpty(spaceRole), "spaceRole", spaceRole);
        return queryWrapper;
    }

    /**
     * 获取空间用户VO
     */
    @Override
    public SpaceUserVo getSpaceUserVo(SpaceUser spaceUser, HttpServletRequest request) {
        // 对象转封装类
        SpaceUserVo spaceUserVo = SpaceUserVo.objToVo(spaceUser);

        // 关联查询用户信息
        Long userId = spaceUser.getUserId();
        if (ObjectUtil.isNotEmpty(userId)) {
            User user = userService.getById(userId);
            UserVo userVo = userService.getUserVo(user);
            spaceUserVo.setUser(userVo);
        }
        // 关联空间信息
        Long spaceId = spaceUser.getSpaceId();
        if (ObjectUtil.isNotEmpty(spaceId)) {
            Space space = spaceService.getById(spaceId);
            SpaceVo spaceVO = spaceService.getSpaceVO(space, request);
            spaceUserVo.setSpace(spaceVO);
        }

        return spaceUserVo;
    }

    /**
     * 获取空间用户VO列表
     */
    @Override
    public List<SpaceUserVo> getSpaceUserVoList(List<SpaceUser> spaceUserList) {
        // 判断输入列表是否为空
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }
        // 列表转换
        List<SpaceUserVo> spaceUserVoList = spaceUserList.stream().map(SpaceUserVo::objToVo).collect(Collectors.toList());
        // 收集用户ID和空间ID
        Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());

        // 批零查询用户和空间
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        Map<Long, List<Space>> spaceIdSpaceListMap = spaceService.listByIds(spaceIdSet).stream().collect(Collectors.groupingBy(Space::getId));

        // 填充信息
        spaceUserVoList.forEach(spaceUserVo -> {
            Long userId = spaceUserVo.getUserId();
            Long spaceId = spaceUserVo.getSpaceId();
            // 填充用户信息
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceUserVo.setUser(userService.getUserVo(user));
            // 填充空间信息
            Space space = null;
            if (spaceIdSpaceListMap.containsKey(spaceId)) {
                space = spaceIdSpaceListMap.get(spaceId).get(0);
            }
            spaceUserVo.setSpace(SpaceVo.objToVo(space));
        });
        return spaceUserVoList;
    }


}




