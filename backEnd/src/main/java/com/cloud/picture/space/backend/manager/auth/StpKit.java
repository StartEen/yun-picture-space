package com.cloud.picture.space.backend.manager.auth;


import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-20  14:11
 * @Description: TODO Sa-Token权限验证工具类(Kit模式)
 * <p>
 * 使用Kit模式，实现多账套授权；
 * 1. StpLogic 门面类，管理项目中的所有StpLogic账号体系
 * 2. 添加@Component注解，确保镜头属性DEFAULT和SPACE被初始化
 */

@Component
public class StpKit {

    public static final String SPACE_TYPE = "space";

    /**
     * 默认原生会话对象，项目中目前没有使用到
     */
    public static final StpLogic DEFAULT = StpUtil.stpLogic;

    /**
     * Space会话对象，管理SPACE表所有账号的登录、权限认证
     */
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE);



}
