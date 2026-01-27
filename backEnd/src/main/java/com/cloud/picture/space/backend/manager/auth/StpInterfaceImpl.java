package com.cloud.picture.space.backend.manager.auth;


import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-27  14:02
 * @Description: TODO 自定义权限加载接口实现类
 */

@Component // 保证此类被SpringBoot扫描，完成Sa-Token的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

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
}
