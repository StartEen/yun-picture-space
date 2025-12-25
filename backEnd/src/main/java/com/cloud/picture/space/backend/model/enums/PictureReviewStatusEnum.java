package com.cloud.picture.space.backend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-25  10:24
 * @Description: TODO 审核状态枚举类
 */
@Getter
public enum PictureReviewStatusEnum {
    REVIEWING("审核中", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    private final String text;
    private final Integer value;

    PictureReviewStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     */
    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (PictureReviewStatusEnum valueEnum : PictureReviewStatusEnum.values()) {
            if (valueEnum.value.equals(value)) {
                return valueEnum;
            }
        }
        return null;
    }


}
