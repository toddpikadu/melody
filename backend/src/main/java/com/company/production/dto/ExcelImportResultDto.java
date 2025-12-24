package com.company.production.dto;

import lombok.Data;

/**
 * Excel导入结果DTO
 */
@Data
public class ExcelImportResultDto {
    /**
     * 导入是否成功
     */
    private boolean success;
    
    /**
     * 成功导入的记录数
     */
    private int successCount;
    
    /**
     * 失败的记录数
     */
    private int errorCount;
    
    /**
     * 导入失败的信息
     */
    private String message;
    
    /**
     * 跳过的记录数（工序无变动的重复产品）
     */
    private int skipCount;
}