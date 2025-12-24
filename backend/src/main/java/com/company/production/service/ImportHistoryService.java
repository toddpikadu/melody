package com.company.production.service;

import com.company.production.entity.ImportHistory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 导入历史记录服务接口
 */
public interface ImportHistoryService {
    
    /**
     * 保存导入历史记录
     * @param importHistory 导入历史记录实体
     * @return 保存后的导入历史记录
     */
    ImportHistory saveImportHistory(ImportHistory importHistory);
    
    /**
     * 根据ID查询导入历史记录
     * @param id 历史记录ID
     * @return 导入历史记录实体
     */
    ImportHistory findImportHistoryById(Integer id);
    
    /**
     * 查询所有导入历史记录
     * @return 导入历史记录列表
     */
    List<ImportHistory> findAllImportHistories();
    
    /**
     * 根据导入时间范围查询历史记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 导入历史记录列表
     */
    List<ImportHistory> findImportHistoriesByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据状态查询历史记录
     * @param status 状态
     * @return 导入历史记录列表
     */
    List<ImportHistory> findImportHistoriesByStatus(String status);
    
    /**
     * 查询最近N条导入历史记录
     * @param limit 限制数量
     * @return 导入历史记录列表
     */
    List<ImportHistory> findRecentImportHistories(Integer limit);
    
    /**
     * 删除导入历史记录
     * @param id 历史记录ID
     */
    void deleteImportHistory(Integer id);
    
    /**
     * 批量删除导入历史记录
     * @param ids 历史记录ID列表
     */
    void deleteImportHistories(List<Integer> ids);
}
