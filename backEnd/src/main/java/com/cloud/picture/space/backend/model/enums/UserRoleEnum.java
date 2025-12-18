package com.cloud.picture.space.backend.model.enums;


import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * @author LT
 * @date 2025/12/17 14:09
 * @description: TODO 角色权限枚举类
 */

@Getter
public enum UserRoleEnum {
    USER("user", "普通用户"),
    ADMIN("admin", "管理员");

    private final String value;
    private final String text;

    UserRoleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据value获取枚举
     *
     * @param value 枚举值
     * @return 枚举
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }


}
