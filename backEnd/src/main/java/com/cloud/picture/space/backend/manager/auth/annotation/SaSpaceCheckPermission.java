package com.cloud.picture.space.backend.manager.auth.annotation;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.annotation.AliasFor;
import com.cloud.picture.space.backend.manager.auth.StpKit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-02-07  09:07
 * @Description: TODO 空间去啊你先人呢正：必须具有指定权限带能进入该方法
 * <p>
 * 可标注子啊函数、类上（效果等同于比哦啊合租在此类的所有方法上）
 */

@SaCheckPermission(type = StpKit.SPACE_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SaSpaceCheckPermission {


    /**
     * 需要校验的权限码
     *
     * @return 需要校验的权限码
     */
    @AliasFor(annotation = SaCheckPermission.class)
    String[] value() default {};

    /**
     * 验证模式：AND|OR,默认AND
     *
     */
    @AliasFor(annotation = SaCheckPermission.class)
    SaMode mode() default SaMode.AND;

    /**
     * 在权限校验不通过时的次要选择，两者只要其一校验成功即可通过校验
     *
     * <p>
     * eg1: @SaCheckPermission(value="user-add",orRole="admin"),
     * 代表本次请求只要具有user-add,admin角色其一即可通过校验
     * </p>
     *
     * <p>
     * eg2:orRole={"admin","manager","staff"},具有三个角色其一即可。
     * eg3:orRole={"admin,manager,staff"},三个角色同时具备。
     * </p>
     */
    @AliasFor(annotation = SaCheckPermission.class)
    String[] orRole() default {};


}
