package com.cloud.picture.space.backend.aop;


import cn.hutool.core.util.ObjectUtil;
import com.cloud.picture.space.backend.annotation.AuthCheck;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import com.cloud.picture.space.backend.model.entity.User;
import com.cloud.picture.space.backend.model.enums.UserRoleEnum;
import com.cloud.picture.space.backend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-18  17:05
 * @Description: TODO 权限校验切面
 *
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;


    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 从 @AuthCheck 注解中提取 mustRole 属性值，用于后续权限校验判断
        String mustRole = authCheck.mustRole();
        // 获取当前线程绑定的请求属性
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        // 从中获取原始的 HttpServletRequest 对象
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User user = userService.getLoginUser(request);
        UserRoleEnum mustUserRole = UserRoleEnum.getEnumByValue(mustRole);
        // 不需要权限放行
        if (ObjectUtil.isEmpty(mustUserRole)) {
            return joinPoint.proceed();
        }
        // 下面是需要权限才能通过
        // 获取当前用户权限
        UserRoleEnum currentUserRole = UserRoleEnum.getEnumByValue(user.getUserRole());
        // 没有权限不予通过
        ThrowUtils.throwIf(ObjectUtil.isEmpty(currentUserRole), ErrorCode.NO_AUTH_ERROR);

        // 要求必须有管理员权限，但用户没有
        if (UserRoleEnum.ADMIN.equals(mustUserRole) && !UserRoleEnum.ADMIN.equals(currentUserRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 允许通过
        return joinPoint.proceed();
    }


}
