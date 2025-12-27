package com.cloud.picture.space.backend.excel.dto;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @Author: StartEnd
 * @CreateTime: 2025-12-27  08:56
 * @Description: TODO excel读取对象
 *
 *
 */
@Data
@ExcelIgnoreUnannotated
public class ReadData {

    @ExcelProperty(value = "费用项目", index = 0)
    private String costProject;

    @ExcelProperty(value = "一级科目", index = 1)
    private String firstSubject;

    @ExcelProperty(value = "二级科目", index = 2)
    private String secondSubject;

    @ExcelProperty(value = "三级科目", index = 3)
    private String thirdSubject;
}
