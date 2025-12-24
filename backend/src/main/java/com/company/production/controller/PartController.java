package com.company.production.controller;

import com.company.production.entity.ImportHistory;
import com.company.production.entity.Part;
import com.company.production.service.ExcelImportService;
import com.company.production.service.ImportHistoryService;
import com.company.production.service.PartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parts")
@CrossOrigin(origins = "http://localhost:8080") // 允许前端Vue项目访问
public class PartController {
    @Autowired
    private PartService partService;
    
    @Autowired
    private ExcelImportService excelImportService;
    
    @Autowired
    private ImportHistoryService importHistoryService;

    /**
     * 获取所有零件列表（包含工序信息）- 用于工分核准界面
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页的零件列表（包含工序信息）
     */
    @GetMapping("/with-processes")
    public Map<String, Object> getPartsWithProcesses(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 创建分页请求
            Pageable pageable = PageRequest.of(page, size);
            Page<Map<String, Object>> partPage = partService.getPartsWithProcesses(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取零件列表成功");
            response.put("parts", partPage.getContent());
            response.put("total", partPage.getTotalElements()); // 总记录数
            response.put("currentPage", page + 1); // 转换为前端使用的页码（从1开始）
            response.put("totalPages", partPage.getTotalPages()); // 总页数
            response.put("size", partPage.getSize()); // 每页大小
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取零件列表失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 修改零件信息
     * @param id 零件ID
     * @param part 零件信息
     * @return 修改结果
     */
    @PutMapping("/{id}")
    public Map<String, Object> updatePart(@PathVariable Integer id, @RequestBody Part part) {
        try {
            Part updatedPart = partService.updatePart(id, part);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "零件信息修改成功");
            response.put("part", updatedPart);
            return response;
        } catch (SecurityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "权限不足: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "零件信息修改失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 删除零件（级联删除工序）
     * @param id 零件ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deletePart(@PathVariable Integer id) {
        try {
            partService.deletePart(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "零件删除成功");
            return response;
        } catch (SecurityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "权限不足: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "零件删除失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 核准零件
     * @param id 零件ID
     * @return 核准结果
     */
    @PostMapping("/{id}/approve")
    public Map<String, Object> approvePart(@PathVariable Integer id) {
        try {
            Part approvedPart = partService.approvePart(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "零件核准成功");
            response.put("part", approvedPart);
            return response;
        } catch (SecurityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "权限不足: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "零件核准失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 取消核准零件
     * @param id 零件ID
     * @return 取消核准结果
     */
    @PostMapping("/{id}/unapprove")
    public Map<String, Object> unapprovePart(@PathVariable Integer id) {
        try {
            Part unapprovedPart = partService.unapprovePart(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "取消零件核准成功");
            response.put("part", unapprovedPart);
            return response;
        } catch (SecurityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "权限不足: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "取消零件核准失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 获取所有零件列表（基本信息）- 支持分页
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页的零件列表
     */
    @GetMapping
    public Map<String, Object> getAllParts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 创建分页请求
            Pageable pageable = PageRequest.of(page, size);
            Page<Part> partPage = partService.findAllParts(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取零件列表成功");
            response.put("products", partPage.getContent()); // 使用products键以匹配前端期望
            response.put("total", partPage.getTotalElements()); // 总记录数
            response.put("currentPage", partPage.getNumber()); // 当前页码
            response.put("totalPages", partPage.getTotalPages()); // 总页数
            response.put("size", partPage.getSize()); // 每页大小
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取零件列表失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 创建零件
     * @param part 零件信息
     * @return 创建结果
     */
    @PostMapping
    public Map<String, Object> createPart(@RequestBody Part part) {
        try {
            Part createdPart = partService.savePart(part);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "零件创建成功");
            response.put("part", createdPart);
            return response;
        } catch (SecurityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "权限不足: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "零件创建失败: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * 预览Excel导入数据
     * @param file Excel文件
     * @return 预览结果
     */
    @PostMapping("/preview-import")
    public Map<String, Object> previewImport(@RequestParam("file") MultipartFile file) {
        try {
            List<Map<String, Object>> previewData = excelImportService.previewImport(file.getInputStream())
                    .stream()
                    .map(dto -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("rowNum", dto.getRowNum());
                        map.put("processRouteNumber", dto.getProcessRouteNumber());
                        map.put("processOrder", dto.getProcessOrder());
                        map.put("process", dto.getProcess());
                        map.put("debugTime", dto.getDebugTime());
                        map.put("workPoints", dto.getWorkPoints());
                        map.put("processID", dto.getProcessID());
                        map.put("productName", dto.getProductName());
                        map.put("productType", dto.getProductType());
                        map.put("errors", dto.getErrors());
                        map.put("valid", dto.isValid());
                        return map;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "文件预览成功");
            response.put("previewData", previewData);
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "文件预览失败: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * 执行Excel导入
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/import")
    public Map<String, Object> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> response = new HashMap<>();
            var importResult = excelImportService.importExcel(file.getInputStream());
            
            response.put("success", importResult.isSuccess());
            response.put("message", importResult.getMessage());
            response.put("successCount", importResult.getSuccessCount());
            response.put("errorCount", importResult.getErrorCount());
            response.put("skipCount", importResult.getSkipCount());
            
            // 保存导入历史记录
            ImportHistory history = new ImportHistory();
            history.setImportTime(java.time.LocalDateTime.now());
            history.setFileName(file.getOriginalFilename());
            history.setSuccessCount(importResult.getSuccessCount());
            history.setErrorCount(importResult.getErrorCount());
            history.setTotalCount(importResult.getSuccessCount() + importResult.getErrorCount() + importResult.getSkipCount());
            
            // 确定导入状态
            if (importResult.getErrorCount() == 0) {
                history.setStatus("SUCCESS");
            } else if (importResult.getSuccessCount() == 0) {
                history.setStatus("FAILED");
            } else {
                history.setStatus("PARTIAL_SUCCESS");
            }
            
            // 这里暂时使用固定值，因为没有实现用户认证系统
            history.setOperator("system");
            history.setMessage(importResult.getMessage());
            
            importHistoryService.saveImportHistory(history);
            
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "文件导入失败: " + e.getMessage());
            return errorResponse;
        }
    }
}



