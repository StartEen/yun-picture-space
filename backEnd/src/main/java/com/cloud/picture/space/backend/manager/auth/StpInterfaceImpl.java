package com.cloud.picture.space.backend.manager.auth;


import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.*;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.manager.auth.model.SpaceUserAuthConstant;
import com.cloud.picture.space.backend.manager.auth.model.SpaceUserPermissionConstant;
import com.cloud.picture.space.backend.model.entity.Picture;
import com.cloud.picture.space.backend.model.entity.Space;
import com.cloud.picture.space.backend.model.entity.SpaceUser;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.enums.SpaceRoleEnum;
import com.cloud.picture.space.backend.model.enums.SpaceTypeEnum;
import com.cloud.picture.space.backend.service.*;
import com.squareup.okhttp.internal.framed.Http2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.util.*;


/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-27  14:02
 * @Description: TODO 自定义权限加载接口实现类
 */
@Slf4j
@Component // 保证此类被SpringBoot扫描，完成Sa-Token的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;


    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 判断loginType,仅对space模块进行权限验证
        if (!StpKit.SPACE_TYPE.equals(loginType)) {
            return new ArrayList<String>();
        }
        List<String> ADMIN_PERMISSIONS = spaceUserAuthManager.getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 获取授权上下文对象
        SpaceUserAuthConstant authConstant = getAuthConstantByRequest();
        // 如果所有字段为空，标识查询公共图库，可以通过
        if (isAllFieldsNull(authConstant)) {
            return ADMIN_PERMISSIONS;
        }
        // 获取登录用户id
        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(UserConstant.USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "当前账号未登录");
        }
        Long userId = loginUser.getId();
        // 优先从上下文中获取SpaceUser对象
        SpaceUser spaceUser = authConstant.getSpaceUser();
        if (spaceUser != null) {
            return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }
        // 如果有spaceUserId，必然是团队空间，通过数据库查询SpaceUser对象
        Long spaceUserId = authConstant.getSpaceUserId();
        if (spaceUserId != null) {
            spaceUser = spaceUserService.getById(spaceUserId);
            if (spaceUser == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到相关空间用户信息");
            }
            // 查询当前登录用户对应的SpaceUser对象
            SpaceUser loginSpaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, spaceUser.getSpaceId())
                    .eq(SpaceUser::getUserId, userId)
                    .one();
            if (loginSpaceUser == null) {
                return new ArrayList<>();
            }
            // 存在问题待优化：
            // 这里会导致管理员在私有空间没有权限，可以在查一次库处理
            return spaceUserAuthManager.getPermissionsByRole(loginSpaceUser.getSpaceRole());
        }
        // 如果没有spaceUserId，则尝试通过pictureId获取Picture对象和Space对象
        Long spaceId = authConstant.getSpaceId();
        if (spaceId == null) {
            // 如果spaceId为空，则尝试通过pictureId获取Picture对象和Space对象
            Long pictureId = authConstant.getPictureId();
            // 如果pictureId为空，则默认通过权限校验
            if (pictureId == null) {
                return ADMIN_PERMISSIONS;
            }
            Picture picture = pictureService.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .select(Picture::getId, Picture::getSpaceId, Picture::getUserId)
                    .one();

            if (picture == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到相关图片信息");
            }
            spaceId = picture.getSpaceId();
            // 公共图库，仅本人可操作
            if (spaceId == null) {
                if (picture.getUserId().equals(userId) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    // 不是自己的图片，仅可查看
                    return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
                }
            }
        }
        // 获取Space对象
        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到相关空间信息");
        }
        // 根据Space类型判断权限
        if (space.getSpaceType() == SpaceTypeEnum.PRIVATE.getValue()) {
            // 私有空间仅本人或管理员可操作
            if (space.getUserId().equals(userId) || userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            } else {
                return new ArrayList<>();
            }
        } else {
            // 团队空间，查询SpaceUser并获取角色和权限
            spaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, spaceId)
                    .eq(SpaceUser::getUserId, userId)
                    .one();

            if (spaceUser == null) {
                return new ArrayList<>();
            }
            return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }

    }

    /**
     * 判断所有字段都为空
     */
    private boolean isAllFieldsNull(Object object) {
        if (object == null) {
            return true;// 对象本身为空
        }
        // 获取所有字段是否为空
        boolean result = Arrays.stream(ReflectUtil.getFields(object.getClass()))
                // 获取字段值
                .map(field -> ReflectUtil.getFieldValue(object, field))
                // 判断字段是否为空
                .allMatch(ObjectUtil::isEmpty);
        return result;
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object o, String s) {
        List<String> list = new ArrayList<String>();
        list.add("admin");
        list.add("super-admin");
        return list;
    }


    /**
     * 从请求中获取上下文对象
     */
    private SpaceUserAuthConstant getAuthConstantByRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthConstant authRequest;
        // 兼容 get 和 post 操作
        if (ContentType.JSON.getValue().equals(contentType)) {
            String body = ServletUtil.getBody(request);
            authRequest = JSONUtil.toBean(body, SpaceUserAuthConstant.class);
        } else {
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            authRequest = BeanUtil.toBean(paramMap, SpaceUserAuthConstant.class);
        }
        // 根据请求路径区分id字段的含义
        Long id = authRequest.getId();
        if (ObjUtil.isNotNull(id)) {
            String requestURI = request.getRequestURI();
            String partUrl = requestURI.replace(contextPath + "/", "");
            String moduleName = StrUtil.subBefore(partUrl, "/", false);
            switch (moduleName) {
                case "space":
                    authRequest.setSpaceId(id);
                    break;
                case "picture":
                    authRequest.setPictureId(id);
                    break;
                case "spaceUser":
                    authRequest.setSpaceUserId(id);
                    break;
                default:
                    log.error("请求路径错误");
                    break;
            }
        }
        return authRequest;
    }


}
