package com.cloud.picture.space.backend.manager.webSocket.model;

import lombok.Getter;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-02-10  13:48
 * @Description: TODO
 */
@Getter
public enum PictureEditActionEnum {

    ZOOM_IN("放大错误","ZOOM_IN"),
    ZOOM_OUT("缩小错误","ZOOM_OUT"),
    ROTATE_LEFT("左旋转操作","ROTATE_LEFT"),
    ROTATE_RIGHT("右旋转操作","ROTATE_RIGHT");

    private final String text;
    private final String value;


    PictureEditActionEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     */
    public static PictureEditActionEnum getEnumByValue(String value){
        if (value == null||value.isEmpty()){
            return null;
        }
        for (PictureEditActionEnum typeEnum : PictureEditActionEnum.values()){
            if (typeEnum.value.equals(value)){
                return typeEnum;
            }
        }
        return null;
    }

}
