package com.cloud.picture.space.backend.model.vo.space;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-08  17:07
 * @Description: TODO 用于展示空间级别信息
 */
@Data
@AllArgsConstructor
public class SpaceLevel {

    private int value;

    private String text;

    private long maxCount;

    private long maxSize;

}
