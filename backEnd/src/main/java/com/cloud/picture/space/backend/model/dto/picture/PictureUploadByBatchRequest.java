package com.cloud.picture.space.backend.model.dto.picture;


import lombok.Data;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-30  17:24
 * @Description: TODO 图片上传批量请求通过抓取
 */
@Data
public class PictureUploadByBatchRequest {

    /**
     * 搜索关键词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count = 10;

}
