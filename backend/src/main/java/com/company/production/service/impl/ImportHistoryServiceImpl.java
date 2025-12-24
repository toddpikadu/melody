package com.company.production.service.impl;

import com.company.production.entity.ImportHistory;
import com.company.production.repository.ImportHistoryRepository;
import com.company.production.service.ImportHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 导入历史记录服务实现类
 */
@Service
public class ImportHistoryServiceImpl implements ImportHistoryService {
    
    @Autowired
    private ImportHistoryRepository importHistoryRepository;
    
    @Override
    public ImportHistory saveImportHistory(ImportHistory importHistory) {
        return importHistoryRepository.save(importHistory);
    }
    
    @Override
    public ImportHistory findImportHistoryById(Integer id) {
        return importHistoryRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<ImportHistory> findAllImportHistories() {
        return importHistoryRepository.findAll();
    }
    
    @Override
    public List<ImportHistory> findImportHistoriesByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return importHistoryRepository.findByImportTimeBetweenOrderByImportTimeDesc(startTime, endTime);
    }
    
    @Override
    public List<ImportHistory> findImportHistoriesByStatus(String status) {
        return importHistoryRepository.findByStatusOrderByImportTimeDesc(status);
    }
    
    @Override
    public List<ImportHistory> findRecentImportHistories(Integer limit) {
        return importHistoryRepository.findRecentImportHistories(limit);
    }
    
    @Override
    public void deleteImportHistory(Integer id) {
        importHistoryRepository.deleteById(id);
    }
    
    @Override
    public void deleteImportHistories(List<Integer> ids) {
        importHistoryRepository.deleteAllById(ids);
    }
}
