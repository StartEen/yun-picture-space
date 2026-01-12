package com.cloud.picture.space.backend.api.imageSearch.sub;


import com.cloud.picture.space.backend.api.imageSearch.model.ImageSearchResult;
import com.cloud.picture.space.backend.api.imageSearch.sub.baiduApi.GetImageFirstUrlApi;
import com.cloud.picture.space.backend.api.imageSearch.sub.baiduApi.GetImageListApi;
import com.cloud.picture.space.backend.api.imageSearch.sub.baiduApi.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-12  14:59
 * @Description: TODO 图片搜索服务门面类
 * <p>
 * 百度以图搜图多个API整合门面类
 */

@Slf4j
public class ImageSearchApiFacade {


    /**
     * 图片搜索
     *
     * @param imageUrl 图片URL
     * @return 图片搜索结果
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        String imageUrl = "https://www.bijingdi.com/uploadfile/2025/0609/20250609235204914.jpg";
        List<ImageSearchResult> imageList = searchImage(imageUrl);
        System.out.println(imageList);
    }


}
