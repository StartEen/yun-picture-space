package com.cloud.picture.space.backend.model.enums;


import lombok.Data;
import lombok.Getter;

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




}
