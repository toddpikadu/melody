package com.company.production.service;

import com.company.production.entity.Part;
import com.company.production.dto.ProcessDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PartService {
    // 查询所有零件（分页）
    Page<Part> findAllParts(Pageable pageable);
    
    // 查询所有零件（不分页）
    List<Part> findAllParts();
    
    // 按品名查询零件（分页）
    Page<Part> findPartsByProductName(String productName, Pageable pageable);
    
    // 按图号查询零件
    Part findPartByProductType(String productType);
    
    // 按品名和图号查询零件（分页）
    Page<Part> findPartsByProductNameAndType(String productName, String productType, Pageable pageable);
    
    // 按零件ID查询所有工序
    List<ProcessDTO> findProcessesByPartId(Integer partId);
    
    // 获取零件列表（包含工序信息）- 用于工分核准界面
    List<Map<String, Object>> getPartsWithProcesses();
    
    // 获取零件列表（包含工序信息）- 用于工分核准界面（分页）
    Page<Map<String, Object>> getPartsWithProcesses(Pageable pageable);
    
    // 保存零件（如果不存在则创建，存在则返回现有）
    Part savePart(Part part);
    
    // 保存工序并关联到零件
    ProcessDTO saveProcess(ProcessDTO processDTO, Integer partId);
    
    // 修改零件
    Part updatePart(Integer id, Part part);
    
    // 删除零件（级联删除工序）
    void deletePart(Integer id);
    
    // 修改工序
    ProcessDTO updateProcess(Integer id, ProcessDTO processDTO);
    
    // 删除工序
    void deleteProcess(Integer id);
    
    // 批量删除工序
    void deleteProcesses(List<Integer> ids);
    
    // 根据ID查询零件
    Part findPartById(Integer id);
    
    // 根据ID查询工序
    ProcessDTO findProcessById(Integer id);
    
    // 核准零件
    Part approvePart(Integer partId);
    
    // 取消核准零件
    Part unapprovePart(Integer partId);

}