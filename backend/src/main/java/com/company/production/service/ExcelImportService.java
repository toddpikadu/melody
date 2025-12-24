package com.company.production.service;

import com.company.production.dto.ExcelImportPreviewDto;
import com.company.production.dto.ExcelImportResultDto;

import java.io.InputStream;
import java.util.List;

/**
 * Excel导入服务接口
 */
public interface ExcelImportService {
    
    /**
     * 预览Excel导入数据
     * 
     * @param inputStream Excel文件输入流
     * @return 预览数据列表
     */
    List<ExcelImportPreviewDto> previewImport(InputStream inputStream);
    
    /**
     * 执行Excel导入
     * 
     * @param inputStream Excel文件输入流
     * @return 导入结果
     */
    ExcelImportResultDto importExcel(InputStream inputStream);
}