package com.company.production.service;

import com.company.production.entity.Workpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WorkpointService {
    Page<Workpoint> findAllWorkpoints(Pageable pageable);
    
    Page<Workpoint> findWorkpointsByProductName(String productName, Pageable pageable);
    
    Page<Workpoint> findWorkpointsByProductType(String productType, Pageable pageable);
    
    Page<Workpoint> findWorkpointsByProductNameAndType(String productName, String productType, Pageable pageable);
    
    // 按图号查询所有工序（用于验证图号唯一）
    List<Workpoint> findAllByProductType(String productType);
    
    // 添加工艺路线
    Workpoint addWorkpoint(Workpoint workpoint);
}