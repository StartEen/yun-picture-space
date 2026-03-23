package com.cloud.picture.space.backend;

import com.cloud.picture.space.backend.api.volcano.api.DouBaoAPI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class BackEndApplicationTests {

    @Resource
    private DouBaoAPI douBaoAPI;

    @Test
    void textGeneratePrompt() {
        String prompt = "帮我生成一个好看的图片，尺寸就采用16:9，内容就以崩坏星穹铁道九尾停云的风格，生成一个九尾白狐的照片";
        System.out.println(douBaoAPI.generateExpandedPrompt(prompt));
    }
}
