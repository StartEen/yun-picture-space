package com.cloud.picture.space.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.manager.sharding.DynamicShardingManager;
import com.cloud.picture.space.backend.model.dto.space.SpaceAddRequest;
import com.cloud.picture.space.backend.model.dto.space.SpaceQueryRequest;
import com.cloud.picture.space.backend.model.entity.Space;
import com.cloud.picture.space.backend.model.entity.SpaceUser;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.enums.SpaceLevelEnum;
import com.cloud.picture.space.backend.model.enums.SpaceRoleEnum;
import com.cloud.picture.space.backend.model.enums.SpaceTypeEnum;
import com.cloud.picture.space.backend.model.vo.space.SpaceVo;
import com.cloud.picture.space.backend.model.vo.user.UserVo;
import com.cloud.picture.space.backend.service.SpaceService;
import com.cloud.picture.space.backend.mapper.SpaceMapper;
import com.cloud.picture.space.backend.service.SpaceUserService;
import com.cloud.picture.space.backend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author yz2025120101
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2026-01-01 15:05:53
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;


    // @Lazy
    // @Resource
    // private DynamicShardingManager dynamicShardingManager;


    @Resource
    private SpaceUserService spaceUserService;



    @Override
    public void validSpace(Space space, boolean add) {
        // 校验参数
        ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.PARAMS_ERROR);

        // 获取参数
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(ObjectUtil.isNull(spaceLevelEnum), ErrorCode.PARAMS_ERROR, "空间等级不能为空");
            ThrowUtils.throwIf(ObjectUtil.isNull(spaceTypeEnum), ErrorCode.PARAMS_ERROR, "空间类型不能为空");
        }

        // 修改数据时，如果要改空间级别
        ThrowUtils.throwIf((spaceTypeEnum != null && spaceLevelEnum == null), ErrorCode.PARAMS_ERROR, "空间等级与类型不能为空");
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

    private final Map<Long, Object> lockMap = new ConcurrentHashMap<>();

    /**
     * 创建空间
     */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 实体类转化
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        // 设置默认值
        if (StrUtil.isBlank(spaceAddRequest.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (spaceAddRequest.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (spaceAddRequest.getSpaceType() == null) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        // 填充数据
        this.fillSpaceBySpaceLevel(space);
        // 数据校验
        this.validSpace(space, true);
        Long userId = loginUser.getId();
        space.setUserId(userId);
        // 权限校验
        if (SpaceLevelEnum.COMMON.getValue() != spaceAddRequest.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "您当前的权限，不足以创建指定级别空间");
        }
        // 针对用户进行加锁
        // region
        // String lock = String.valueOf(userId).intern();
        // synchronized (lock) {
        //     Long newSpaceId = transactionTemplate.execute(action -> {
        //         // 每个用户只有一个私有空间，所以需要检验
        //         boolean exists = this.lambdaQuery().eq(Space::getUserId, userId).exists();
        //         ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR,
        //                 "您已创建过私有空间,每个用户仅能拥有一个私有空间");
        //         // 写入数据库
        //         boolean result = this.save(space);
        //         ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建空间失败");
        //         return space.getId();
        //     });
        //     // 返回结果是包装类；
        //     return Optional.ofNullable(newSpaceId).orElse(-1L);
        // }
        // endregion
        Object lock = lockMap.computeIfAbsent(userId, k -> new Object());
        synchronized (lock) {
            try {
                Long newSpaceId = transactionTemplate.execute(action -> {
                    // 每个用户只有一个私有空间，所以需要检验
                    boolean exists = this.lambdaQuery().eq(Space::getUserId, userId)
                            .eq(Space::getSpaceType, spaceAddRequest.getSpaceType())
                            .exists();
                    ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "您已创建过私有空间,每个用户仅能拥有一个私有空间");
                    // 写入数据库
                    boolean result = this.save(space);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建空间失败");
                    if (SpaceTypeEnum.TEAM.getValue() == spaceAddRequest.getSpaceType()) {
                        SpaceUser spaceUser = new SpaceUser();
                        spaceUser.setSpaceId(space.getId());
                        spaceUser.setUserId(userId);
                        spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                        result = spaceUserService.save(spaceUser);
                        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队空间成员记录失败");
                    }

                    // 创建分表
                    // dynamicShardingManager.createSpacePictureTable(space);

                    return space.getId();
                });
                // 返回结果是包装类；
                return Optional.ofNullable(newSpaceId).orElse(-1L);
            } finally {
                lockMap.remove(userId);
            }
        }
    }

    /**
     * 删除空间
     */
    @Override
    public boolean deleteSpace(Long spaceId, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(spaceId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 判断当前空间是否存在
        Space oldSpace = this.getById(spaceId);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !loginUser.getUserRole().equals("admin")) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = this.removeById(spaceId);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败");
        }
        return true;
    }

    /**
     * 校验空间权限
     */
    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        // 仅本人或管理员可编辑
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }


    /**
     * 获取空间视图(单条)
     */
    @Override
    public SpaceVo getSpaceVO(Space space, HttpServletRequest request) {
        SpaceVo spaceVo = SpaceVo.objToVo(space);
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVo userVo = userService.getUserVo(user);
            spaceVo.setUser(userVo);
        }
        return spaceVo;
    }

    /**
     * 获取空间视图列表
     */
    @Override
    public Page<SpaceVo> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVo> spaceVoPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVoPage;
        }
        // 对象列表进行流转换
        List<SpaceVo> spaceVoList = spaceList.stream()
                .map(SpaceVo::objToVo).collect(Collectors.toList());

        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        spaceVoList.forEach(spaceVo -> {
            Long userId = spaceVo.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVo.setUser(userService.getUserVo(user));
        });
        spaceVoPage.setRecords(spaceVoList);

        return spaceVoPage;
    }

    /**
     * 获取查询包装类
     */
    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);

        return queryWrapper;
    }


}




