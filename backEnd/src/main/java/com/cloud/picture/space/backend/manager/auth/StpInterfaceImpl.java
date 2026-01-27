package com.cloud.picture.space.backend.manager.auth;


import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.cloud.picture.space.backend.manager.auth.model.SpaceUserAuthConstant;
import com.squareup.okhttp.internal.framed.Http2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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


    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object o, String s) {
        List<String> list = new ArrayList<String>();
        list.add("user.add");
        list.add("user.update");
        list.add("user.delete");
        list.add("user.get");
        list.add("art.*");
        return list;
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
