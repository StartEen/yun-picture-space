package com.cloud.picture.space.backend.excel.write.dto;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @Author: StartEnd
 * @CreateTime: 2026-01-10  08:32
 * @Description: TODO 模版写出
 */

@Data
public class ExportModel {

    @ExcelProperty("字段一")
    private String fieldOne;

    @ExcelProperty("字段二")
    private Integer fieldTwo;

    @ExcelProperty("字段三") 
    private String fieldThree;

    @ExcelProperty("字段四")
    private String fieldFlour;

    @ExcelProperty("字段五")
    private String fieldFive;

    @ExcelProperty("字段六")
    private String fieldSix;

    @ExcelProperty("字段七")
    private String fieldSeven;

    @ExcelProperty("字段八")
    private String fieldEight;
}
