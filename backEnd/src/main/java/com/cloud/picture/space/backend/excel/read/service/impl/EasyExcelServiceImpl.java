package com.cloud.picture.space.backend.excel.read.service.impl;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.util.ListUtils;
import com.cloud.picture.space.backend.excel.read.dto.ReadData;
import com.cloud.picture.space.backend.excel.read.service.EasyExcelService;
import com.cloud.picture.space.backend.exception.BusinessException;
import com.cloud.picture.space.backend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-27  09:10
 * @Description: TODO
 */

@Slf4j
@Service
public class EasyExcelServiceImpl implements EasyExcelService {
    @Override
    public List<ReadData> importExcel(MultipartFile file) {
        try (ExcelReader build = EasyExcel.read(file.getInputStream()).build()) {
            List<ReadSheet> readAll = ListUtils.newArrayList();
            List<ReadSheet> allSheets = build.excelExecutor().sheetList();
            ThreadLocal<List<ReadData>> result = new ThreadLocal<>();
            result.set(new ArrayList<>());

            ReadListener<?> readListener = new ReadDataListener(this);
            Class<?> clazz = ReadData.class;
            for (ReadSheet sheet : allSheets) {
                ReadSheet readSheet = EasyExcel.readSheet(sheet.getSheetNo())
                        .head(clazz).registerReadListener(readListener).build();
                readAll.add(readSheet);
            }
            if (!readAll.isEmpty()) {
                build.read(readAll);
            }
            return result.get();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
        }
    }

    @Override
    public int savaData(List<ReadData> cachedDataList) {
            //添加持久层存储逻辑

        return 0;
    }
}
