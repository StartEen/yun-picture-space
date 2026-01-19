package com.cloud.picture.space.backend.model.enums;


import cn.hutool.core.util.ObjUtil;
import lombok.Data;
import lombok.Getter;
import org.apache.catalina.LifecycleState;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-19  10:05
 * @Description: TODO 空间角色枚举类
 */


@Getter
public enum SpaceRoleEnum {

    VIEWER("viewer", "浏览者"),
    EDITOR("editor", "编辑者"),
    ADMIN("admin", "管理员");

    private final String text;

    private final String value;

    SpaceRoleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }


    /**
     * 根据value值获取枚举
     */
    public static SpaceRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceRoleEnum anEnum : SpaceRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }


    /**
     * 获取所有枚举的文本列表
     *
     * @return 文本列表
     */
    public static List<String> getAllTexts() {
        return Arrays.stream(SpaceRoleEnum.values())
                .map(SpaceRoleEnum::getText)
                .collect(Collectors.toList());
    }



    /**
     * 获取所有枚举的value列表
     *
     * @return value列表
     */
    public static List<String> getAllValues() {
        return Arrays.stream(SpaceRoleEnum.values())
                .map(SpaceRoleEnum::getValue)
                .collect(Collectors.toList());
    }


}
