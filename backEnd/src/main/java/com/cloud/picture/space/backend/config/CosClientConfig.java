package com.cloud.picture.space.backend.config;


import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-23  15:47
 * @Description: TODO COS客户端Bean
 * <p>
 * 负责读取配置文件
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cos.client")
public class CosClientConfig {

    /**
     * 域名
     */
    private String host;

    /**
     * secretId
     */
    private String secretId;

    /**
     * 密钥secretKey
     */
    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * 存储桶
     */
    private String bucket;

    @Bean
    public COSClient cosClient() {
        // 初始化用户身份信息（secretId，secretKey）
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 设置bucket的区域，COS地域的简称
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(cred, clientConfig);
    }

}
