package com.company.production.util;

import com.company.production.entity.Process;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 工序比较工具类，用于判断两个工序列表是否相同
 */
public class ProcessComparisonUtil {
    
    /**
     * 比较两个工序列表是否相同
     * @param existingProcesses 数据库中现有工序
     * @param importProcesses 导入的工序
     * @return 是否相同
     */
    public static boolean processesAreSame(List<Process> existingProcesses, List<Process> importProcesses) {
        // 1. 比较数量
        if (existingProcesses.size() != importProcesses.size()) {
            return false;
        }
        
        // 2. 按工序号排序
        List<Process> sortedExisting = existingProcesses.stream()
            .sorted(Comparator.comparing(Process::getProcessID))
            .collect(Collectors.toList());
        
        List<Process> sortedImport = importProcesses.stream()
            .sorted(Comparator.comparing(Process::getProcessID))
            .collect(Collectors.toList());
        
        // 3. 逐个比较关键字段
        for (int i = 0; i < sortedExisting.size(); i++) {
            Process existing = sortedExisting.get(i);
            Process imported = sortedImport.get(i);
            
            // 比较工序号、工序名称、调机时间、工分
            if (!Objects.equals(existing.getProcessID(), imported.getProcessID()) ||
                !Objects.equals(existing.getProcess(), imported.getProcess()) ||
                !Objects.equals(existing.getDebugTime(), imported.getDebugTime()) ||
                !Objects.equals(existing.getWorkPoints(), imported.getWorkPoints())) {
                return false;
            }
        }
        
        return true;
    }
}
