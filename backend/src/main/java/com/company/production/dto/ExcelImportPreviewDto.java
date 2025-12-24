package com.company.production.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel导入预览数据DTO
 */
@Data
public class ExcelImportPreviewDto {
    /**
     * 行号
     */
    private int rowNum;
    
    /**
     * 工艺路线编号
     */
    private String processRouteNumber;
    
    /**
     * 加工次序
     */
    private Integer processOrder;
    
    /**
     * 工序名称
     */
    private String process;
    
    /**
     * 准备时间（分钟）
     */
    private Double debugTime;
    
    /**
     * 定额工时/加工工时（分钟）
     */
    private Double workPoints;
    
    /**
     * 工序ID
     */
    private String processID;
    
    /**
     * 产品名称
     */
    private String productName;
    
    /**
     * 产品型号
     */
    private String productType;
    
    /**
     * 错误信息列表
     */
    private List<String> errors;
    
    /**
     * 该行是否为有效的产品或工序数据
     */
    private boolean valid;
    
    /**
     * 构造方法，初始化错误信息列表
     */
    public ExcelImportPreviewDto() {
        this.errors = new ArrayList<>();
        this.valid = true;
    }
    
    /**
     * 添加错误信息
     * 
     * @param error 错误信息
     */
    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }
}