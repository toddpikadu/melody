package com.company.production.controller;

import com.company.production.entity.ImportHistory;
import com.company.production.service.ImportHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 导入历史记录Controller
 */
@RestController
@RequestMapping("/api/import-history")
public class ImportHistoryController {
    
    @Autowired
    private ImportHistoryService importHistoryService;
    
    /**
     * 获取所有导入历史记录
     * @return 导入历史记录列表
     */
    @GetMapping
    public ResponseEntity<List<ImportHistory>> getAllImportHistories() {
        List<ImportHistory> histories = importHistoryService.findAllImportHistories();
        return new ResponseEntity<>(histories, HttpStatus.OK);
    }
    
    /**
     * 根据ID获取导入历史记录
     * @param id 历史记录ID
     * @return 导入历史记录实体
     */
    @GetMapping("/{id}")
    public ResponseEntity<ImportHistory> getImportHistoryById(@PathVariable Integer id) {
        ImportHistory history = importHistoryService.findImportHistoryById(id);
        if (history != null) {
            return new ResponseEntity<>(history, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * 根据时间范围获取导入历史记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 导入历史记录列表
     */
    @GetMapping("/time-range")
    public ResponseEntity<List<ImportHistory>> getImportHistoriesByTimeRange(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        List<ImportHistory> histories = importHistoryService.findImportHistoriesByTimeRange(startTime, endTime);
        return new ResponseEntity<>(histories, HttpStatus.OK);
    }
    
    /**
     * 根据状态获取导入历史记录
     * @param status 状态
     * @return 导入历史记录列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ImportHistory>> getImportHistoriesByStatus(@PathVariable String status) {
        List<ImportHistory> histories = importHistoryService.findImportHistoriesByStatus(status);
        return new ResponseEntity<>(histories, HttpStatus.OK);
    }
    
    /**
     * 获取最近N条导入历史记录
     * @param limit 限制数量
     * @return 导入历史记录列表
     */
    @GetMapping("/recent")
    public ResponseEntity<List<ImportHistory>> getRecentImportHistories(@RequestParam(defaultValue = "10") Integer limit) {
        List<ImportHistory> histories = importHistoryService.findRecentImportHistories(limit);
        return new ResponseEntity<>(histories, HttpStatus.OK);
    }
    
    /**
     * 删除导入历史记录
     * @param id 历史记录ID
     * @return 响应状态
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImportHistory(@PathVariable Integer id) {
        importHistoryService.deleteImportHistory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * 批量删除导入历史记录
     * @param ids 历史记录ID列表
     * @return 响应状态
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteImportHistories(@RequestBody List<Integer> ids) {
        importHistoryService.deleteImportHistories(ids);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
