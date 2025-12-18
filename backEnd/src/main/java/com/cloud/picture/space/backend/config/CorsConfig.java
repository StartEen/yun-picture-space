package com.cloud.picture.space.backend.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: LT
 * @CreateTime: 2025-12-17  18:18
 * @Description: TODO 全局跨域配置类
 *
 *配置全局跨域配置，让整个项目的所有接口都支持跨域访问，解决跨域报错
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //覆盖所有请求
        registry.addMapping("/**")
                //允许发送Cookie
                .allowCredentials(true)
                //放行那些域名（必须使用patterns,否则*会和allowCredentials冲突）
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}
