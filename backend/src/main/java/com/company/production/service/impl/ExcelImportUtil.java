package com.company.production.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.company.production.dto.ExcelImportPreviewDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel导入工具类
 */
public class ExcelImportUtil {
    
    /**
     * 解析Excel文件，生成预览数据
     * 
     * @param inputStream Excel文件输入流
     * @return 预览数据列表
     */
    public static List<ExcelImportPreviewDto> parseExcelForPreview(InputStream inputStream) {
        List<ExcelImportPreviewDto> previewData = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            
            // 遍历所有行
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                
                int rowNum = rowIndex + 1; // 行号从1开始
                ExcelImportPreviewDto dto = new ExcelImportPreviewDto();
                dto.setRowNum(rowNum);
                List<String> errors = new ArrayList<>();
                
                try {
                    // B列：工艺路线编号（索引为1）
                    String processRouteNumber = getCellValue(row, 1);
                    dto.setProcessRouteNumber(processRouteNumber);
                    
                    // 根据工艺路线编号判断行类型
                    if (processRouteNumber == null || processRouteNumber.trim().isEmpty()) {
                        // 空行，标记为无效
                        dto.setValid(false);
                        errors.add("工艺路线编号不能为空");
                        dto.setErrors(errors);
                        previewData.add(dto);
                        continue;
                    }
                    
                    // 初始化基本信息
                    dto.setValid(true);
                    
                    // 处理GYLX开头的行（基础信息行）
                    if (processRouteNumber.startsWith("GYLX")) {
                        // GYLX行不需要提取具体数据，只记录行号
                        dto.setValid(true);
                    }
                    // 处理【工序】行
                    else if ("【工序】".equals(processRouteNumber)) {
                        // E列：加工次序（索引为4）
                        Integer processOrder = parseInteger(getCellValue(row, 4));
                        if (processOrder == null) {
                            errors.add("加工次序必须为整数");
                        }
                        dto.setProcessOrder(processOrder);
                        
                        // F列：工序名称（索引为5）
                        String process = getCellValue(row, 5);
                        if (process == null || process.trim().isEmpty()) {
                            errors.add("工序名称不能为空");
                        }
                        dto.setProcess(process);
                        
                        // K列：准备时间（索引为10）
                        Double debugTime = parseDouble(getCellValue(row, 10));
                        if (debugTime == null) {
                            errors.add("准备时间必须为数字");
                        } else {
                            // 保留一位小数
                            debugTime = new BigDecimal(debugTime).setScale(1, RoundingMode.HALF_UP).doubleValue();
                        }
                        dto.setDebugTime(debugTime);
                        
                        // L列：定额工时/加工工时（索引为11）
                        Double workPoints = parseDouble(getCellValue(row, 11));
                        if (workPoints == null) {
                            errors.add("定额工时必须为数字");
                        } else {
                            // 保留一位小数
                            workPoints = new BigDecimal(workPoints).setScale(1, RoundingMode.HALF_UP).doubleValue();
                        }
                        dto.setWorkPoints(workPoints);
                        
                        // AA列：备注（工序ID）（索引为26）
                        String processID = getCellValue(row, 26);
                        if (processID == null || processID.trim().isEmpty()) {
                            errors.add("工序ID不能为空");
                        }
                        dto.setProcessID(processID);
                    }
                    // 处理【产品】行
                    else if ("【产品】".equals(processRouteNumber)) {
                        // AB列：产品名称（索引为27）
                        String productName = getCellValue(row, 27);
                        if (productName == null || productName.trim().isEmpty()) {
                            errors.add("产品名称不能为空");
                        }
                        dto.setProductName(productName);
                        
                        // AD列：产品型号（索引为29）
                        String productType = getCellValue(row, 29);
                        if (productType == null || productType.trim().isEmpty()) {
                            errors.add("产品型号不能为空");
                        }
                        dto.setProductType(productType);
                    }
                    
                    // 检查是否有错误
                    if (!errors.isEmpty()) {
                        dto.setValid(false);
                        dto.setErrors(errors);
                    }
                    
                } catch (Exception e) {
                    dto.setValid(false);
                    errors.add("解析错误：" + e.getMessage());
                    dto.setErrors(errors);
                }
                
                previewData.add(dto);
            }
        } catch (Exception e) {
            // 创建一个错误的预览记录
            ExcelImportPreviewDto errorDto = new ExcelImportPreviewDto();
            errorDto.setRowNum(0);
            errorDto.setValid(false);
            List<String> errors = new ArrayList<>();
            errors.add("预览Excel数据失败：" + e.getMessage());
            errorDto.setErrors(errors);
            previewData.add(errorDto);
        }
        
        return previewData;
    }
    
    /**
     * 获取单元格的值
     * 
     * @param row 行对象
     * @param cellIndex 列索引
     * @return 单元格值
     */
    private static String getCellValue(Row row, int cellIndex) {
        if (row == null || row.getCell(cellIndex) == null) {
            return null;
        }
        
        try {
            return row.getCell(cellIndex).getStringCellValue().trim();
        } catch (Exception e) {
            // 尝试获取数字值
            try {
                double numericValue = row.getCell(cellIndex).getNumericCellValue();
                if (numericValue == (int) numericValue) {
                    return String.valueOf((int) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }
            } catch (Exception ex) {
                return null;
            }
        }
    }
    
    /**
     * 将字符串解析为整数
     * 
     * @param value 字符串值
     * @return 整数，解析失败返回null
     */
    private static Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 将字符串解析为双精度浮点数
     * 
     * @param value 字符串值
     * @return 双精度浮点数，解析失败返回null
     */
    private static Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}