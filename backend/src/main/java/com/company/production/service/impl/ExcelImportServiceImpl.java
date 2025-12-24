package com.company.production.service.impl;

import com.company.production.dto.ExcelImportPreviewDto;
import com.company.production.dto.ExcelImportResultDto;
import com.company.production.entity.Part;
import com.company.production.entity.Process;
import com.company.production.repository.PartRepository;
import com.company.production.repository.ProcessRepository;
import com.company.production.service.ExcelImportService;
import com.company.production.util.ProcessComparisonUtil;
import com.company.production.util.ExcelImportUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelImportServiceImpl implements ExcelImportService {

    @Autowired
    private PartRepository partRepository;
    
    @Autowired
    private ProcessRepository processRepository;
    
    @Override
    // 移除外层事务注解，避免事务嵌套问题
    public ExcelImportResultDto importExcel(InputStream inputStream) {
        ExcelImportResultDto result = new ExcelImportResultDto();
        int successCount = 0;
        int errorCount = 0;
        int skipCount = 0; // 新增跳过计数
        
        try {
            // 解析Excel文件，获取产品-工序映射关系
            Map<String, List<ExcelImportPreviewDto>> productProcessMap = ExcelImportUtil.parseExcelForImport(inputStream);
            
            // 遍历每个产品进行处理
            for (Map.Entry<String, List<ExcelImportPreviewDto>> entry : productProcessMap.entrySet()) {
                String productType = entry.getKey();
                List<ExcelImportPreviewDto> productProcesses = entry.getValue();
                
                if (productProcesses.isEmpty()) {
                    continue;
                }
                
                try {
                    // 为每个产品创建独立事务
                    int[] counts = importSingleProduct(productType, productProcesses);
                    successCount += counts[0];
                    skipCount += counts[1];
                    errorCount += counts[2];
                } catch (Exception e) {
                    errorCount++;
                    // 记录错误，但继续处理其他产品
                }
            }
            
            result.setSuccess(true);
            result.setSuccessCount(successCount);
            result.setErrorCount(errorCount);
            result.setSkipCount(skipCount); // 设置跳过计数
            result.setMessage(String.format("导入完成，成功 %d 条，失败 %d 条，跳过 %d 条", successCount, errorCount, skipCount));
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setSuccessCount(0);
            result.setErrorCount(0);
            result.setSkipCount(0);
            result.setMessage("导入失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 导入单个产品的方法，使用独立事务
     * @param productType 产品型号
     * @param productProcesses 产品的工序列表
     * @return int数组 [成功数, 跳过数, 错误数]
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private int[] importSingleProduct(String productType, List<ExcelImportPreviewDto> productProcesses) {
        int[] counts = new int[3]; // [success, skip, error]
        
        try {
            // 获取产品信息（从产品组中找到包含产品名称的记录）
            ExcelImportPreviewDto firstDto = productProcesses.get(0);
            String productName = firstDto.getProductName();
            
            // 如果第一条记录没有产品名称，尝试从其他记录中获取
            if (productName == null || productName.isEmpty()) {
                for (ExcelImportPreviewDto dto : productProcesses) {
                    if (dto.getProductName() != null && !dto.getProductName().isEmpty()) {
                        productName = dto.getProductName();
                        break;
                    }
                }
            }
            
            // 如果仍然没有产品名称，使用产品型号作为默认值
            if (productName == null || productName.isEmpty()) {
                productName = productType;
            }
            
            Part part = new Part();
            part.setProductName(productName);
            part.setProductType(productType);
            part.setIsApproved(0); // 设置默认未核准状态
            part.setLastModifiedAt(LocalDateTime.now());
            
            // 创建工序列表
            List<Process> processes = new ArrayList<>();
            for (ExcelImportPreviewDto dto : productProcesses) {
                Process process = new Process();
                process.setProcessID(dto.getProcessID());
                process.setProcess(dto.getProcess());
                process.setDebugTime(dto.getDebugTime() != null ? dto.getDebugTime() : 0.0);
                process.setWorkPoints(dto.getWorkPoints() != null ? dto.getWorkPoints() : 0.0);
                process.setOrder(dto.getProcessOrder());
                process.setLastModifiedAt(LocalDateTime.now()); // 设置最后修改时间
                processes.add(process);
            }
            
            // 检查是否存在相同图号的产品
            Part existingPart = partRepository.findByProductType(productType);
            
            if (existingPart != null) {
                // 存在重复产品，获取现有工序
                List<Process> existingProcesses = processRepository.findByPartIdOrderByOrderAsc(existingPart.getId());
                
                // 比较工序是否相同
                if (ProcessComparisonUtil.processesAreSame(existingProcesses, processes)) {
                    // 工序相同，跳过
                    counts[1] = 1;
                } else {
                    // 工序不同，执行覆盖操作
                    // 1. 更新产品信息
                    existingPart.setProductName(part.getProductName());
                    existingPart.setLastModifiedAt(LocalDateTime.now());
                    partRepository.save(existingPart);
                    
                    // 2. 删除现有工序
                    processRepository.deleteByPartId(existingPart.getId());
                    
                    // 3. 保存新工序
                    for (Process process : processes) {
                        process.setPart(existingPart);
                        processRepository.save(process);
                    }
                    
                    counts[0] = 1;
                }
            } else {
                // 不存在重复产品，直接保存
                // 保存产品
                Part savedPart = partRepository.save(part);
                
                // 为产品添加工序
                for (Process process : processes) {
                    process.setPart(savedPart);
                    processRepository.save(process);
                }
                
                counts[0] = 1;
            }
        } catch (Exception e) {
            counts[2] = 1;
            // 记录错误日志
            e.printStackTrace();
        }
        
        return counts;
    }

    /**
     * 从DTO创建Part实体
     */
    private Part createPartFromDto(ExcelImportPreviewDto dto) {
        Part part = new Part();
        part.setProductName(dto.getProductName());
        part.setProductType(dto.getProductType());
        part.setIsApproved(0); // 默认未核准
        part.setLastModifiedAt(LocalDateTime.now());
        return part;
    }

    /**
     * 从DTO创建Process实体
     */
    private Process createProcessFromDto(ExcelImportPreviewDto dto) {
        Process process = new Process();
        
        // 设置工序编号
        String processID = dto.getProcessID();
        if (processID == null || processID.trim().isEmpty()) {
            return null;
        }
        process.setProcessID(processID.trim());
        
        // 设置工序名称
        String processName = dto.getProcess();
        if (processName == null || processName.trim().isEmpty()) {
            return null;
        }
        process.setProcess(processName.trim());
        
        // 设置调机时间
        Double debugTime = dto.getDebugTime();
        if (debugTime == null) {
            debugTime = 0.0;
        }
        process.setDebugTime(debugTime);
        
        // 设置工分
        Double workPoints = dto.getWorkPoints();
        if (workPoints == null) {
            workPoints = 0.0;
        }
        process.setWorkPoints(workPoints);
        
        // 设置工序顺序
        Integer processOrder = dto.getProcessOrder();
        if (processOrder == null) {
            processOrder = 0;
        }
        process.setOrder(processOrder);
        
        // 设置最后修改时间
        process.setLastModifiedAt(LocalDateTime.now());
        
        return process;
    }

    /**
     * 产品导入分组类，用于临时存储产品和关联的工序
     */
    private static class ProductImportGroup {
        private Part part;
        private List<Process> processes = new ArrayList<>();
        
        public ProductImportGroup(Part part) {
            this.part = part;
        }
        
        public void addProcess(Process process) {
            this.processes.add(process);
        }
        
        public Part getPart() {
            return part;
        }
        
        public List<Process> getProcesses() {
            return processes;
        }
    }

    @Override
    public List<ExcelImportPreviewDto> previewImport(InputStream inputStream) {
        try {
            return ExcelImportUtil.parseExcelForPreview(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("预览Excel数据失败：" + e.getMessage());
        }
    }
}