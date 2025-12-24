package com.company.production.repository;

import com.company.production.entity.Workpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkpointRepository extends JpaRepository<Workpoint, Integer> {
    // 按品名查询（分页）
    Page<Workpoint> findByProductName(String productName, Pageable pageable);
    
    // 按图号查询（分页）
    Page<Workpoint> findByProductType(String productType, Pageable pageable);
    
    // 按品名和图号查询（分页）
    Page<Workpoint> findByProductNameAndProductType(String productName, String productType, Pageable pageable);
    
    // 按图号查询所有工序（用于确保图号唯一时的完整查询），并按order字段升序排序
    List<Workpoint> findByProductTypeOrderByOrderAsc(String productType);
}