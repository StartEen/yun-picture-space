package com.cloud.picture.space.backend.controller;


import com.cloud.picture.space.backend.common.BaseResponse;
import com.cloud.picture.space.backend.common.ResultUtils;
import com.cloud.picture.space.backend.excel.dto.ReadData;
import com.cloud.picture.space.backend.excel.service.EasyExcelService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-27  09:07
 * @Description: TODO
 */

@RestController
@RequestMapping("/easyExcel")
public class EasyExcelController {

    @Resource
    private EasyExcelService easyExcelService;

    @PostMapping("/import")
    public BaseResponse<List<ReadData>> importExcel(MultipartFile file) {
        List<ReadData> readData = easyExcelService.importExcel(file);
        return ResultUtils.success(readData);
    }


}
