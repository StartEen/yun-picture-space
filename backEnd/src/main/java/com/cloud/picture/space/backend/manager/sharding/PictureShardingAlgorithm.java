package com.cloud.picture.space.backend.manager.sharding;


import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-02-07  14:13
 * @Description: TODO 分表算法
 * <p>
 * 实现 StandardShardingAlgorithm 接口，编写doSharding方法，根据spaceId获取到实际要查询的
 * 分表名，如果spaceId不存在分表（比如私有空间）或者spaceId为空（公共图库），那么就从原表（逻辑表）picture查询
 */

public class PictureShardingAlgorithm implements StandardShardingAlgorithm<Long> {
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> preciseShardingValue) {
        Long spaceId = preciseShardingValue.getValue();
        String logicTableName = preciseShardingValue.getLogicTableName();
        // spaceId 为null 标识查询所有图片
        if (spaceId == null) {
            return logicTableName;
        }
        // 根据spaceId东岱生成分表名
        String realTableName = "picture_" + spaceId;
        if (availableTargetNames.contains(realTableName)) {
            return realTableName;
        } else {
            return logicTableName;
        }
    }

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {
        return new ArrayList<>();
    }

    @Override
    public Properties getProps() {
        return null;
    }

    @Override
    public void init(Properties properties) {

    }
}
