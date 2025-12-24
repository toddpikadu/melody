package com.company.production.util;

import com.company.production.dto.ExcelImportPreviewDto;
import org.apache.poi.ss.usermodel.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel导入工具类
 */
public class ExcelImportUtil {

    /**
     * 解析Excel文件生成预览数据，保持与原始Excel表格相同的结构
     * 
     * @param inputStream Excel文件输入流
     * @return 预览数据列表
     * @throws Exception 解析异常
     */
    public static List<ExcelImportPreviewDto> parseExcelForPreview(InputStream inputStream) throws Exception {
        List<ExcelImportPreviewDto> previewData = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            
            // 遍历所有行
            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                
                // 获取B列（工艺路线编号）的值，用于判断行类型
                Cell typeCell = row.getCell(1); // B列是第2列，索引为1
                String rowType = getCellValueAsString(typeCell);
                
                // 如果B列值为null或为空，跳过此行
                if (rowType == null || rowType.trim().isEmpty()) {
                    continue;
                }
                
                // 去除空格进行判断
                String normalizedRowType = rowType.trim();
                String processRouteNumber = normalizedRowType;
                
                // 1. 如果是GYLX开头的行，创建一个记录用于标记
                if (normalizedRowType.startsWith("GYLX")) {
                    ExcelImportPreviewDto gylxDto = new ExcelImportPreviewDto();
                    gylxDto.setRowNum(rowIndex + 1);
                    gylxDto.setProcessRouteNumber(processRouteNumber);
                    previewData.add(gylxDto);
                } else if (normalizedRowType.equals("【工序】")) {
                    // 2. 如果是"【工序】"行，收集工序信息并直接添加到预览数据
                    ExcelImportPreviewDto processDto = new ExcelImportPreviewDto();
                    processDto.setRowNum(rowIndex + 1);
                    processDto.setProcessRouteNumber(processRouteNumber);
                    
                    // 解析E列：加工次序 → process_order
                    Cell processOrderCell = row.getCell(4); // E列是第5列，索引为4
                    if (processOrderCell != null) {
                        try {
                            processDto.setProcessOrder(getCellValueAsInteger(processOrderCell));
                        } catch (Exception e) {
                            processDto.addError("加工次序格式错误");
                        }
                    } else {
                        processDto.addError("加工次序不能为空");
                    }
                    
                    // 解析F列：工序名称 → process
                    Cell processCell = row.getCell(5); // F列是第6列，索引为5
                    if (processCell != null) {
                        processDto.setProcess(getCellValueAsString(processCell));
                    } else {
                        processDto.addError("工序名称不能为空");
                    }
                    
                    // 解析K列：准备时间 → debugTime
                    Cell debugTimeCell = row.getCell(10); // K列是第11列，索引为10
                    if (debugTimeCell != null) {
                        try {
                            processDto.setDebugTime(getCellValueAsDouble(debugTimeCell));
                        } catch (Exception e) {
                            processDto.addError("准备时间格式错误");
                        }
                    }
                    
                    // 解析L列：定额工时加工工时 → workPoints
                    Cell workPointsCell = row.getCell(11); // L列是第12列，索引为11
                    if (workPointsCell != null) {
                        try {
                            processDto.setWorkPoints(getCellValueAsDouble(workPointsCell));
                        } catch (Exception e) {
                            processDto.addError("定额工时格式错误");
                        }
                    } else {
                        processDto.addError("定额工时不能为空");
                    }
                    
                    // 解析AA列：备注 → processID
                    Cell processIDCell = row.getCell(26); // AA列是第27列，索引为26
                    if (processIDCell != null) {
                        processDto.setProcessID(getCellValueAsString(processIDCell));
                    } else {
                        processDto.addError("工序ID不能为空");
                    }
                    
                    // 直接将工序信息添加到预览数据中（保持与Excel表格相同的结构）
                    previewData.add(processDto);
                } else if (normalizedRowType.equals("【产品】")) {
                    // 3. 如果是"【产品】"行，收集产品信息并直接添加到预览数据
                    ExcelImportPreviewDto productDto = new ExcelImportPreviewDto();
                    productDto.setRowNum(rowIndex + 1);
                    productDto.setProcessRouteNumber(processRouteNumber);
                    
                    // 解析AB列：产品名称 → productName
                    Cell productNameCell = row.getCell(27); // AB列是第28列，索引为27
                    String productName = getCellValueAsString(productNameCell);
                    
                    // 解析AD列：产品型号 → productType
                    Cell productTypeCell = row.getCell(29); // AD列是第30列，索引为29
                    String productType = getCellValueAsString(productTypeCell);
                    
                    // 设置产品信息
                    productDto.setProductName(productName);
                    productDto.setProductType(productType);
                    
                    // 验证产品信息
                    if (productName == null || productName.trim().isEmpty()) {
                        productDto.addError("产品名称不能为空");
                    }
                    if (productType == null || productType.trim().isEmpty()) {
                        productDto.addError("产品型号不能为空");
                    }
                    
                    // 直接将产品信息添加到预览数据中（保持与Excel表格相同的结构）
                    previewData.add(productDto);
                }
            }
        }
        
        return previewData;
    }
    
    /**
     * 解析Excel文件并构建产品-工序列表，用于实际导入处理
     * 
     * @param inputStream Excel文件输入流
     * @return 产品-工序列表的映射，键为产品型号，值为该产品的所有工序记录
     * @throws Exception 解析异常
     */
    public static Map<String, List<ExcelImportPreviewDto>> parseExcelForImport(InputStream inputStream) throws Exception {
        // 产品-工序列表的映射，键为产品型号，值为该产品的所有工序记录
        Map<String, List<ExcelImportPreviewDto>> productProcessMap = new HashMap<>();
        
        // 当前收集的工序列表（用于关联到后续的产品）
        List<ExcelImportPreviewDto> currentProcesses = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            
            // 遍历所有行
            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                
                // 获取B列（工艺路线编号）的值，用于判断行类型
                Cell typeCell = row.getCell(1); // B列是第2列，索引为1
                String rowType = getCellValueAsString(typeCell);
                
                // 如果B列值为null或为空，跳过此行
                if (rowType == null || rowType.trim().isEmpty()) {
                    continue;
                }
                
                // 去除空格进行判断
                String normalizedRowType = rowType.trim();
                String processRouteNumber = normalizedRowType;
                
                // 1. 如果是GYLX开头的行，表示一个新的产品组开始
                if (normalizedRowType.startsWith("GYLX")) {
                    // 清空当前工序列表，准备收集新的工序信息
                    currentProcesses.clear();
                } else if (normalizedRowType.equals("【工序】")) {
                    // 2. 如果是"【工序】"行，收集工序信息
                    ExcelImportPreviewDto processDto = new ExcelImportPreviewDto();
                    processDto.setRowNum(rowIndex + 1);
                    processDto.setProcessRouteNumber(processRouteNumber);
                    
                    // 解析E列：加工次序 → process_order
                    Cell processOrderCell = row.getCell(4); // E列是第5列，索引为4
                    if (processOrderCell != null) {
                        try {
                            processDto.setProcessOrder(getCellValueAsInteger(processOrderCell));
                        } catch (Exception e) {
                            processDto.addError("加工次序格式错误");
                        }
                    } else {
                        processDto.addError("加工次序不能为空");
                    }
                    
                    // 解析F列：工序名称 → process
                    Cell processCell = row.getCell(5); // F列是第6列，索引为5
                    if (processCell != null) {
                        processDto.setProcess(getCellValueAsString(processCell));
                    } else {
                        processDto.addError("工序名称不能为空");
                    }
                    
                    // 解析K列：准备时间 → debugTime
                    Cell debugTimeCell = row.getCell(10); // K列是第11列，索引为10
                    if (debugTimeCell != null) {
                        try {
                            processDto.setDebugTime(getCellValueAsDouble(debugTimeCell));
                        } catch (Exception e) {
                            processDto.addError("准备时间格式错误");
                        }
                    }
                    
                    // 解析L列：定额工时加工工时 → workPoints
                    Cell workPointsCell = row.getCell(11); // L列是第12列，索引为11
                    if (workPointsCell != null) {
                        try {
                            processDto.setWorkPoints(getCellValueAsDouble(workPointsCell));
                        } catch (Exception e) {
                            processDto.addError("定额工时格式错误");
                        }
                    } else {
                        processDto.addError("定额工时不能为空");
                    }
                    
                    // 解析AA列：备注 → processID
                    Cell processIDCell = row.getCell(26); // AA列是第27列，索引为26
                    if (processIDCell != null) {
                        processDto.setProcessID(getCellValueAsString(processIDCell));
                    } else {
                        processDto.addError("工序ID不能为空");
                    }
                    
                    // 添加到当前工序列表
                    currentProcesses.add(processDto);
                } else if (normalizedRowType.equals("【产品】")) {
                    // 3. 如果是"【产品】"行，处理产品信息并关联之前的工序
                    // 解析AB列：产品名称 → productName
                    Cell productNameCell = row.getCell(27); // AB列是第28列，索引为27
                    String productName = getCellValueAsString(productNameCell);
                    
                    // 解析AD列：产品型号 → productType
                    Cell productTypeCell = row.getCell(29); // AD列是第30列，索引为29
                    String productType = getCellValueAsString(productTypeCell);
                    
                    // 验证产品信息
                    boolean productInfoValid = true;
                    if (productName == null || productName.trim().isEmpty()) {
                        productInfoValid = false;
                    }
                    if (productType == null || productType.trim().isEmpty()) {
                        productInfoValid = false;
                    }
                    
                    // 如果产品信息无效，跳过处理
                    if (!productInfoValid) {
                        continue;
                    }
                    
                    // 将当前工序列表中的所有工序关联到该产品
                    if (!currentProcesses.isEmpty()) {
                        // 创建该产品的工序列表
                        List<ExcelImportPreviewDto> productProcesses = new ArrayList<>();
                        
                        // 为每个工序设置产品信息并添加到产品工序列表
                        for (ExcelImportPreviewDto processDto : currentProcesses) {
                            // 创建新实例以避免引用问题
                            ExcelImportPreviewDto productProcessDto = new ExcelImportPreviewDto();
                            productProcessDto.setRowNum(processDto.getRowNum());
                            productProcessDto.setProcessRouteNumber(processDto.getProcessRouteNumber());
                            productProcessDto.setProcessOrder(processDto.getProcessOrder());
                            productProcessDto.setProcess(processDto.getProcess());
                            productProcessDto.setDebugTime(processDto.getDebugTime());
                            productProcessDto.setWorkPoints(processDto.getWorkPoints());
                            productProcessDto.setProcessID(processDto.getProcessID());
                            productProcessDto.setProductName(productName);
                            productProcessDto.setProductType(productType);
                            
                            // 复制错误信息
                            if (processDto.getErrors() != null) {
                                for (String error : processDto.getErrors()) {
                                    productProcessDto.addError(error);
                                }
                            }
                            
                            productProcesses.add(productProcessDto);
                        }
                        
                        // 将产品工序列表添加到映射中
                        productProcessMap.put(productType, productProcesses);
                    }
                }
            }
        }
        
        return productProcessMap;
    }
    
    /**
     * 将单元格值转换为字符串
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return null;
        }
    }
    
    /**
     * 将单元格值转换为Double
     */
    private static Double getCellValueAsDouble(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                String stringValue = cell.getStringCellValue().trim();
                if (!stringValue.isEmpty()) {
                    return Double.parseDouble(stringValue);
                }
                return null;
            default:
                return null;
        }
    }
    
    /**
     * 将单元格值转换为Integer
     */
    private static Integer getCellValueAsInteger(Cell cell) {
        Double doubleValue = getCellValueAsDouble(cell);
        if (doubleValue != null) {
            return doubleValue.intValue();
        }
        return null;
    }
}