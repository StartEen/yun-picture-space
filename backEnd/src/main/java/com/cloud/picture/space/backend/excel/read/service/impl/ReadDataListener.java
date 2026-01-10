package com.cloud.picture.space.backend.excel.read.service.impl;


import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.cloud.picture.space.backend.excel.read.dto.ReadData;
import com.cloud.picture.space.backend.excel.read.service.EasyExcelService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-27  09:04
 * @Description: TODO 读的监听器
 */
@Slf4j
public class ReadDataListener implements ReadListener<ReadData> {

    /**
     * 每隔5条存储数据库，实际使用中可以1000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 1000;

    /**
     * 缓存的数据
     */
    private List<ReadData> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    /**
     * 引入 service
     */
    private EasyExcelService easyExcelService;


    public ReadDataListener(EasyExcelService easyExcelService) {
        this.easyExcelService = easyExcelService;
    }


    /**
     * 统计导入数量
     */
    private int totalCount = 0;
    private int successCount = 0;
    private int failCount = 0;


    /**
     * 错误信息记录
     */
    private final StringBuilder errorMsg = new StringBuilder();

    /**
     * 每读取一行数据会调用此方法
     */
    @Override
    public void invoke(ReadData readData, AnalysisContext analysisContext) {
        totalCount++;
        log.info("读取到第{}行数剧，解析到一条数据:{}",totalCount, readData);
        try {
            //添加缓存
            cachedDataList.add(readData);
            //达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
            if (cachedDataList.size() >= BATCH_COUNT) {
                saveData();
                // 存储完成清理 list
                cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
            }
        }catch (Exception e){
            log.error("数据处理异常：{}", e.getMessage());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!cachedDataList.isEmpty()) {
            saveData();
            // 存储完成清理 list
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    /**
     * 批量保存数据
     */
    private void saveData() {
        // 调用业务服务处理数据（如保存到数据库）
        int savedCount = easyExcelService.savaData(cachedDataList);
        successCount += savedCount;
        log.info("批量保存{}条数据成功", cachedDataList.size());
    }



}
