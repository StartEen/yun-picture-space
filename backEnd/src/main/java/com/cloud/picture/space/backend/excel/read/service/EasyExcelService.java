package com.cloud.picture.space.backend.excel.read.service;

import com.cloud.picture.space.backend.excel.read.dto.ReadData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-27  09:09
 * @Description: TODO
 */
public interface EasyExcelService {
    List<ReadData> importExcel( MultipartFile file);

    int savaData(List<ReadData> cachedDataList);
}
