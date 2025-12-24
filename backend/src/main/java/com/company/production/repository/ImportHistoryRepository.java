package com.company.production.repository;

import com.company.production.entity.ImportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 导入历史记录Repository接口
 */
public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Integer> {
    
    /**
     * 根据导入时间范围查询历史记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 导入历史记录列表
     */
    List<ImportHistory> findByImportTimeBetweenOrderByImportTimeDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据状态查询历史记录
     * @param status 状态
     * @return 导入历史记录列表
     */
    List<ImportHistory> findByStatusOrderByImportTimeDesc(String status);
    
    /**
     * 查询最近N条导入历史记录
     * @param limit 限制数量
     * @return 导入历史记录列表
     */
    @Query(value = "SELECT * FROM import_history ORDER BY import_time DESC LIMIT :limit", nativeQuery = true)
    List<ImportHistory> findRecentImportHistories(@Param("limit") Integer limit);
}
