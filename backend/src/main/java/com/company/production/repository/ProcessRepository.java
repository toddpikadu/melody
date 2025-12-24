package com.company.production.repository;

import com.company.production.entity.Process;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessRepository extends JpaRepository<Process, Integer> {
    // 按partId查询所有工序，并按process_order字段升序排序
    List<Process> findByPartIdOrderByOrderAsc(Integer partId);
    
    // 按partId删除所有工序
    void deleteByPartId(Integer partId);
}
