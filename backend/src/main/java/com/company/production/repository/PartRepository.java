package com.company.production.repository;

import com.company.production.entity.Part;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartRepository extends JpaRepository<Part, Integer> {
    // 按品名查询（分页）
    Page<Part> findByProductName(String productName, Pageable pageable);
    
    // 按图号查询
    Part findByProductType(String productType);
    
    // 按品名和图号查询（分页）
    Page<Part> findByProductNameAndProductType(String productName, String productType, Pageable pageable);
}