package com.company.production.service.impl;

import com.company.production.entity.Workpoint;
import com.company.production.repository.WorkpointRepository;
import com.company.production.service.WorkpointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkpointServiceImpl implements WorkpointService {
    
    @Autowired
    private WorkpointRepository workpointRepository;

    @Override
    public Page<Workpoint> findAllWorkpoints(Pageable pageable) {
        return workpointRepository.findAll(pageable);
    }

    @Override
    public Page<Workpoint> findWorkpointsByProductName(String productName, Pageable pageable) {
        return workpointRepository.findByProductName(productName, pageable);
    }

    @Override
    public Page<Workpoint> findWorkpointsByProductType(String productType, Pageable pageable) {
        return workpointRepository.findByProductType(productType, pageable);
    }

    @Override
    public Page<Workpoint> findWorkpointsByProductNameAndType(String productName, String productType, Pageable pageable) {
        return workpointRepository.findByProductNameAndProductType(productName, productType, pageable);
    }

    @Override
    public List<Workpoint> findAllByProductType(String productType) {
        return workpointRepository.findByProductTypeOrderByOrderAsc(productType);
    }
    
    @Override
    public Workpoint addWorkpoint(Workpoint workpoint) {
        return workpointRepository.save(workpoint);
    }
}