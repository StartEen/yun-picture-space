package com.cloud.picture.space.backend.api.imageSearch.model;


import lombok.Data;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-09  10:57
 * @Description: TODO 【以图搜图】图片搜索结果类
 */
@Data
public class ImageSearchResult {

    /**
     * 缩率图片地址
     */
    private String thumbUrl;

    /**
     * 来源图片地址
     */
    private String fromUrl;

}
