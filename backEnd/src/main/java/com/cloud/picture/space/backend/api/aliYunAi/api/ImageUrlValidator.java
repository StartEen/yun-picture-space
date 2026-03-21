package com.cloud.picture.space.backend.api.aliYunAi.api;

import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageUrlValidator {

    /**
     * 验证图片 URL 是否能被外部 AI 服务正常访问
     * @param imageUrl 待检测的图片地址
     * @return 是否通过验证
     */
    public static boolean validateImageAccessibility(String imageUrl) {
        try {
            // 1. 使用 HEAD 请求只获取响应头，节省带宽
            HttpResponse response = HttpUtil.createRequest(Method.HEAD, imageUrl)
                    .timeout(5000) // 设置 5 秒超时
                    .execute();

            int status = response.getStatus();
            String contentType = response.header("Content-Type");

            log.info("检测图片 URL: {}, 状态码: {}, Content-Type: {}", imageUrl, status, contentType);

            // 2. 检查状态码是否为 200
            if (status != 200) {
                log.error("❌ 图片无法访问！状态码: {}. 请检查腾讯云 COS 权限是否为 '公共读'。", status);
                return false;
            }

            // 3. 检查 Content-Type 是否为图片类型
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("⚠️ URL 返回的内容类型不是图片: {}", contentType);
                // 某些 CDN 可能不返回正确的 content-type，这里可以根据实际情况决定是否 return false
            }

            return true;
        } catch (Exception e) {
            log.error("❌ 网络连接异常，无法访问图片地址: {}", e.getMessage());
            return false;
        }
    }
}