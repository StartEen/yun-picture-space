package com.cloud.picture.space.backend;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ClusterOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-31  11:21
 * @Description: TODO Redis测试类
 * <p>
 * 测试对Redis的基础操作
 */

@SpringBootTest
public class RedisStringTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Test
    public void testRedisStringOperation() {
        // 获取RedisTemplate操作对象
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();


        // key 和 value
        String key = "testKey";
        String value = "testValue";

        // 1.测试新增或更新操作
        valueOperations.set(key, value);
        String storedValue = valueOperations.get(key);
        assertEquals(value, storedValue, "新增或更新操作失败,存储值与预期值不一样");

        // 2.测试修改操作
        String updateValue = "updateValue";
        valueOperations.set(key, updateValue);
        storedValue = valueOperations.get(key);
        assertEquals(updateValue, storedValue, "更新操作失败,存储值与预期值不一样");

        // 3.测试查询操作
        storedValue = valueOperations.get(key);
        assertNotNull(storedValue, "查询操作失败,存储值为空");
        assertEquals(updateValue, storedValue, "查询操作失败,存储值与预期值不一样");


        // 4.测试删除操作
        stringRedisTemplate.delete(key);
        storedValue = valueOperations.get(key);
        assertNull( storedValue, "删除操作失败,存储值不为空");

    }


}
