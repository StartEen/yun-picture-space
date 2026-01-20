package com.cloud.picture.space.backend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-17  13:41
 * @Description: TODO 空间类别枚举类
 */
@Getter
public enum SpaceTypeEnum {

    PRIVATE("私有空间", 0),
    TEAM("团队空间", 1);


    private final String text;
    private final int value;

    SpaceTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举项
     */
    public static SpaceTypeEnum getEnumByValue(Integer value) {
        if (ObjectUtil.isNull(value)) {
            return null;
        }
        for (SpaceTypeEnum valueEnum : values()) {
            if (valueEnum.value == value) {
                return valueEnum;
            }
        }
        return null;
    }
}
