package com.cloud.picture.space.backend.api.imageSearch.sub.baiduApi;


import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-12  12:58
 * @Description: TODO 解析jsoup爬取到的页面信息
 */
@Slf4j
public class GetImageFirstUrlApi {


    /**
     * 获取图片的第一张图片
     *
     * @param url 页面链接
     * @return 返回图片的URL
     */
    public static String getImageFirstUrl(String url) {
        try {
            // 使用jsoup获取html内容
            Document document = Jsoup.connect(url)
                    .timeout(5000).get();

            // 获取所有<script>标签
            Elements scriptElements = document.getElementsByTag("script");

            // 遍历找到firstUrl的脚本内容
            for (Element script : scriptElements) {
                String scriptContent = script.html();
                if (scriptContent.contains("\"firstUrl\"")) {
                    // 正则表达式提取firstUrl的值
                    String regex = "\"firstUrl\"\\s*:\\s*\"(.*?)\"";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(scriptContent);
                    if (matcher.find()) {
                        String firstUrl = matcher.group(1);
                        // 处理转义字符
                        firstUrl = firstUrl.replace("\\/", "/");
                        return firstUrl;
                    }

                }

            }

            throw new BusinessException(ErrorCode.OPERATION_ERROR, "页面抓取失败,未找到url");
        } catch (Exception e) {
            log.error("页面抓取失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "页面抓取失败");
        }
    }

    public static void main(String[] args) {
        // 请求目标URl
        String url = "https://graph.baidu.com/s?card_key=&entrance=GENERAL&extUiData[isLogoShow]=1&f=all&isLogoShow=1&session_id=8450295440288186905&sign=1269233f98b434f059cc701768198724&tpl_from=pc";
        String imageFirstUrl = getImageFirstUrl(url);
        System.out.println("图片URL：" + imageFirstUrl);
    }


}
