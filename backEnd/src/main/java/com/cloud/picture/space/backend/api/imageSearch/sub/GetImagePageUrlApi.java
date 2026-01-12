package com.cloud.picture.space.backend.api.imageSearch.sub;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import com.cloud.picture.space.backend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-09  11:13
 * @Description: TODO  获取图片的页面链接
 */
@Slf4j
public class GetImagePageUrlApi {

    /**
     * 获取图片的页面链接
     *
     * @param imageUrl 图片链接
     * @return 图片的页面链接
     */
    public static String getImagePageUrl(String imageUrl) {
        // 1.准备请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 获取当前时间戳
        long uptime = System.currentTimeMillis();
        // 请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;
        // 2.发送Post请求到百度接口
        try {
            HttpResponse response = HttpRequest.post(url)
                    .header("acs-token", RandomUtil.randomString(1))
                    .form(formData).timeout(5000)
                    .execute();
            // 判断响应状态
            if (HttpStatus.HTTP_OK != response.getStatus()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求百度接口,响应异常");
            }
            // 解析响应
            String responseBody = response.body();
            Map<String, Object> result = JSONUtil.toBean(responseBody, Map.class);


            // 3. 响应结果处理
            ThrowUtils.throwIf((result == null || !Integer.valueOf(0).equals(result.get("status")))
                    , ErrorCode.OPERATION_ERROR, "请求百度接口，返回结果异常");
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = (String) data.get("url");
            // 对URL解码
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            // 如果URL为空抛异常
            ThrowUtils.throwIf(StrUtil.isBlank(searchResultUrl), ErrorCode.OPERATION_ERROR, "请求百度接口，返回Url异常");
            return searchResultUrl;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求百度接口异常");
        }
    }

    public static void main(String[] args) {
        // 测试以图搜图，获取网络图片URL
        String imageUrl = "https://www.bijingdi.com/uploadfile/2025/0609/20250609235204914.jpg";
        String result = getImagePageUrl(imageUrl);
        System.out.println("搜索成功，结果URL：" + result);
    }


}
