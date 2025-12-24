package com.company.production.service.impl;

import com.company.production.dto.ProcessDTO;
import com.company.production.entity.Part;
import com.company.production.entity.Process;
import com.company.production.entity.User;
import com.company.production.repository.PartRepository;
import com.company.production.repository.ProcessRepository;
import com.company.production.service.AuthenticationService;
import com.company.production.service.PartService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PartServiceImpl implements PartService {

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public Page<Part> findAllParts(Pageable pageable) {
        return partRepository.findAll(pageable);
    }

    @Override
    public List<Part> findAllParts() {
        // 由于Part实体中的processes是懒加载，我们需要通过HQL查询来确保工序信息被加载
        List<Part> parts = partRepository.findAll();
        // 触发懒加载
        parts.forEach(part -> part.getProcesses().size());
        return parts;
    }

    @Override
    public Page<Part> findPartsByProductName(String productName, Pageable pageable) {
        return partRepository.findByProductName(productName, pageable);
    }

    @Override
    public Part findPartByProductType(String productType) {
        return partRepository.findByProductType(productType);
    }

    @Override
    public Page<Part> findPartsByProductNameAndType(String productName, String productType, Pageable pageable) {
        return partRepository.findByProductNameAndProductType(productName, productType, pageable);
    }

    @Override
    public List<ProcessDTO> findProcessesByPartId(Integer partId) {
        List<Process> processes = processRepository.findByPartIdOrderByOrderAsc(partId);
        return processes.stream()
                .map(process -> modelMapper.map(process, ProcessDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Part savePart(Part part) {
        // 检查零件是否已存在
        Part existingPart = partRepository.findByProductType(part.getProductType());
        if (existingPart != null) {
            return existingPart;
        }
        // 设置默认值
        if (part.getIsApproved() == null) {
            part.setIsApproved(0);
        }
        // 保存新零件
        return partRepository.save(part);
    }

    @Override
    public ProcessDTO saveProcess(ProcessDTO processDTO, Integer partId) {
        // 查询零件实体
        Part part = partRepository.findById(partId).orElseThrow(() -> new IllegalArgumentException("零件不存在"));
        
        // 将ProcessDTO转换为Process实体
        Process process = modelMapper.map(processDTO, Process.class);
        process.setPart(part);
        // 保存工序
        Process savedProcess = processRepository.save(process);
        // 转换为DTO并返回
        return modelMapper.map(savedProcess, ProcessDTO.class);
    }

    @Override
    public Part updatePart(Integer id, Part part) {
        // 检查零件是否存在
        Part existingPart = partRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("零件不存在"));

        // 如果要修改品名或图号，检查是否有管理员权限
        if ((part.getProductName() != null && !part.getProductName().equals(existingPart.getProductName())) ||
            (part.getProductType() != null && !part.getProductType().equals(existingPart.getProductType()))) {
            authenticationService.checkRole("admin");
        }

        // 更新零件信息
        if (part.getProductName() != null) {
            existingPart.setProductName(part.getProductName());
        }
        if (part.getProductType() != null) {
            existingPart.setProductType(part.getProductType());
        }

        // 保存并返回更新后的零件
        return partRepository.save(existingPart);
    }

    @Override
    public void deletePart(Integer id) {
        // 检查是否有管理员权限
        authenticationService.checkRole("admin");

        // 检查零件是否存在
        partRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("零件不存在"));

        // 级联删除工序
        processRepository.deleteByPartId(id);

        // 删除零件
        partRepository.deleteById(id);
    }

    @Override
    public ProcessDTO updateProcess(Integer id, ProcessDTO processDTO) {
        // 检查用户是否为部长或超级管理员
        User currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("用户未认证");
        }
        String roleName = currentUser.getRole().getName();
        if (!roleName.equals("director") && !roleName.equals("admin")) {
            throw new SecurityException("只有部长和超级管理员可以修改工序信息");
        }

        // 检查工序是否存在
        Process existingProcess = processRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("工序不存在"));

        // 更新工序信息
        if (processDTO.getProcessID() != null) {
            existingProcess.setProcessID(processDTO.getProcessID());
        }
        if (processDTO.getProcess() != null) {
            existingProcess.setProcess(processDTO.getProcess());
        }
        if (processDTO.getDebugTime() != null) {
            existingProcess.setDebugTime(processDTO.getDebugTime());
        }
        if (processDTO.getWorkPoints() != null) {
            existingProcess.setWorkPoints(processDTO.getWorkPoints());
        }
        if (processDTO.getOrder() != null) {
            existingProcess.setOrder(processDTO.getOrder());
        }

        // 记录修改人和修改时间
        existingProcess.setLastModifiedBy(currentUser.getId());
        existingProcess.setLastModifiedAt(LocalDateTime.now());

        // 同时更新所属零件的最后修改信息
        Part part = existingProcess.getPart();
        if (part != null) {
            part.setLastModifiedBy(currentUser.getId());
            part.setLastModifiedAt(LocalDateTime.now());
            partRepository.save(part);
        }

        // 保存并返回更新后的工序
        Process savedProcess = processRepository.save(existingProcess);
        return modelMapper.map(savedProcess, ProcessDTO.class);
    }

    @Override
    public void deleteProcess(Integer id) {
        // 检查工序是否存在
        processRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("工序不存在"));

        // 删除工序
        processRepository.deleteById(id);
    }

    @Override
    public void deleteProcesses(List<Integer> ids) {
        // 批量删除工序
        processRepository.deleteAllById(ids);
    }

    @Override
    public Part findPartById(Integer id) {
        return partRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("零件不存在"));
    }

    @Override
    public ProcessDTO findProcessById(Integer id) {
        Process process = processRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("工序不存在"));
        return modelMapper.map(process, ProcessDTO.class);
    }

    @Override
    public Part approvePart(Integer partId) {
        // 检查用户是否为部长或超级管理员
        User currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("用户未认证");
        }
        String roleName = currentUser.getRole().getName();
        if (!roleName.equals("director") && !roleName.equals("admin")) {
            throw new SecurityException("只有部长和超级管理员可以核准零件");
        }

        // 查找零件
        Part part = partRepository.findById(partId).orElseThrow(() -> new IllegalArgumentException("零件不存在"));

        // 更新零件核准状态
        part.setIsApproved(1); // 1表示已核准
        part.setApprovedBy(currentUser.getId());
        part.setApprovedAt(LocalDateTime.now());
        part.setLastModifiedBy(currentUser.getId());
        part.setLastModifiedAt(LocalDateTime.now());

        // 保存并返回
        return partRepository.save(part);
    }

    @Override
    public Part unapprovePart(Integer partId) {
        // 检查用户是否为部长或超级管理员
        User currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new SecurityException("用户未认证");
        }
        String roleName = currentUser.getRole().getName();
        if (!roleName.equals("director") && !roleName.equals("admin")) {
            throw new SecurityException("只有部长和超级管理员可以取消核准零件");
        }

        // 查找零件
        Part part = partRepository.findById(partId).orElseThrow(() -> new IllegalArgumentException("零件不存在"));

        // 更新零件核准状态
        part.setIsApproved(0); // 0表示未核准
        part.setApprovedBy(null);
        part.setApprovedAt(null);
        part.setLastModifiedBy(currentUser.getId());
        part.setLastModifiedAt(LocalDateTime.now());

        // 保存并返回
        return partRepository.save(part);
    }

    @Override
    public List<Map<String, Object>> getPartsWithProcesses() {
        // 获取所有零件
        List<Part> parts = partRepository.findAll();
        return parts.stream().map(part -> {
            Map<String, Object> partMap = new HashMap<>();
            // 添加零件信息
            partMap.put("id", part.getId());
            partMap.put("productName", part.getProductName());
            partMap.put("productType", part.getProductType());
            partMap.put("isApproved", part.getIsApproved());
            partMap.put("approvedBy", part.getApprovedBy());
            partMap.put("approvedAt", part.getApprovedAt());
            partMap.put("lastModifiedBy", part.getLastModifiedBy());
            partMap.put("lastModifiedAt", part.getLastModifiedAt());
            // 添加工序信息
            List<ProcessDTO> processes = findProcessesByPartId(part.getId());
            partMap.put("processes", processes);
            return partMap;
        }).collect(Collectors.toList());
    }
    
    @Override
    public Page<Map<String, Object>> getPartsWithProcesses(Pageable pageable) {
        // 获取分页的零件列表
        Page<Part> partPage = partRepository.findAll(pageable);
        
        // 将每个Part转换为包含工序信息的Map
        List<Map<String, Object>> partsWithProcesses = partPage.getContent().stream().map(part -> {
            Map<String, Object> partMap = new HashMap<>();
            // 添加零件信息
            partMap.put("id", part.getId());
            partMap.put("productName", part.getProductName());
            partMap.put("productType", part.getProductType());
            partMap.put("isApproved", part.getIsApproved());
            partMap.put("approvedBy", part.getApprovedBy());
            partMap.put("approvedAt", part.getApprovedAt());
            partMap.put("lastModifiedBy", part.getLastModifiedBy());
            partMap.put("lastModifiedAt", part.getLastModifiedAt());
            // 添加工序信息
            List<ProcessDTO> processes = findProcessesByPartId(part.getId());
            partMap.put("processes", processes);
            return partMap;
        }).collect(Collectors.toList());
        
        // 创建并返回新的Page对象
        return new org.springframework.data.domain.PageImpl<>(partsWithProcesses, pageable, partPage.getTotalElements());
    }
}