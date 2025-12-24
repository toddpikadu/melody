package com.company.production.controller;

import com.company.production.dto.AddProcessRequest;
import com.company.production.dto.ProcessDTO;
import com.company.production.entity.Part;
import com.company.production.service.PartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workpoints")
@CrossOrigin(origins = "http://localhost:8080") // 允许前端Vue项目访问
public class WorkpointController {
    
    @Autowired
    private PartService partService;

    /**
     * 分页查询所有工分数据，支持按品名和图号查询
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param productName 品名（可选）
     * @param productType 图号（可选）
     * @return 分页查询结果
     */
    @GetMapping
    public Map<String, Object> getWorkpoints(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productType) {
        
        try {
            // 创建分页请求，按productType排序
            Pageable pageable = PageRequest.of(page, size, Sort.by("productType").ascending());
            Page<Part> partPage;

            // 根据查询参数调用不同的查询方法
            if (productName != null && !productName.isEmpty() && productType != null && !productType.isEmpty()) {
                // 同时按品名和图号查询
                partPage = partService.findPartsByProductNameAndType(productName, productType, pageable);
            } else if (productName != null && !productName.isEmpty()) {
                // 仅按品名查询
                partPage = partService.findPartsByProductName(productName, pageable);
            } else if (productType != null && !productType.isEmpty()) {
                // 仅按图号查询
                Part part = partService.findPartByProductType(productType);
                if (part != null) {
                    // 创建一个包含单个Part的列表
                    java.util.List<Part> partList = java.util.Collections.singletonList(part);
                    // 创建一个Page对象，包含这个列表
                    partPage = new org.springframework.data.domain.PageImpl<>(partList, pageable, 1L);
                } else {
                    partPage = Page.empty(pageable);
                }
            } else {
                // 查询所有
                partPage = partService.findAllParts(pageable);
            }

            // 构建响应结果
            Map<String, Object> response = new HashMap<>();
            response.put("parts", partPage.getContent());
            response.put("currentPage", partPage.getNumber());
            response.put("totalItems", partPage.getTotalElements());
            response.put("totalPages", partPage.getTotalPages());
            response.put("size", partPage.getSize());
            response.put("success", true);

            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "查询失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 按图号查询所有工序（用于确保获取完整的零件工序信息）
     * @param productType 图号
     * @return 该图号对应的所有工序
     */
    @GetMapping("/by-type/{productType}")
    public Map<String, Object> getWorkpointsByType(@PathVariable String productType) {
        try {
            // 先查询零件
            Part part = partService.findPartByProductType(productType);
            if (part == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("workpoints", java.util.Collections.emptyList());
                response.put("count", 0);
                response.put("success", true);
                return response;
            }
            
            // 再查询该零件的所有工序
            List<ProcessDTO> processes = partService.findProcessesByPartId(part.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("workpoints", processes);
            response.put("count", processes.size());
            response.put("success", true);
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "查询失败: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * 添加单个工艺路线
     * @param request 零件和工艺信息
     * @return 添加结果
     */
    @PostMapping
    public Map<String, Object> addWorkpoint(@RequestBody AddProcessRequest request) {
        try {
            // 数据验证
            if (request.getProductName() == null || request.getProductName().isEmpty() ||
                request.getProductType() == null || request.getProductType().isEmpty() ||
                request.getProcessID() == null || request.getProcessID().isEmpty() ||
                request.getProcess() == null || request.getProcess().isEmpty()) {
                throw new IllegalArgumentException("产品名称、图号、工序编号和工序描述不能为空");
            }

            // 获取零件对象
            Part savedPart;
            if (request.getPartId() != null) {
                // 如果提供了partId，直接根据ID查询零件
                savedPart = partService.findPartById(request.getPartId());
            } else {
                // 否则创建零件对象
                Part part = new Part();
                part.setProductName(request.getProductName());
                part.setProductType(request.getProductType());

                // 保存零件（如果不存在则创建，存在则返回现有）
                savedPart = partService.savePart(part);
            }

            // 创建工艺DTO对象
            ProcessDTO processDTO = new ProcessDTO();
            processDTO.setProcessID(request.getProcessID());
            processDTO.setProcess(request.getProcess());
            processDTO.setDebugTime(request.getDebugTime());
            processDTO.setWorkPoints(request.getWorkPoints());
            processDTO.setOrder(request.getOrder());

            // 保存工艺并关联到零件
            ProcessDTO savedProcess = partService.saveProcess(processDTO, savedPart.getId());

            // 构建响应结果
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "工艺路线添加成功");
            response.put("process", savedProcess);
            response.put("part", savedPart);

            return response;
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "工艺路线添加失败: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "工艺路线添加失败: 服务器内部错误 - " + e.getMessage());
            return errorResponse;
        }
    }
}