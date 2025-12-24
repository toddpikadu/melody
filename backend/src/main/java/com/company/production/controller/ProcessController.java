package com.company.production.controller;

import com.company.production.dto.ProcessDTO;
import com.company.production.service.PartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/processes")
@CrossOrigin(origins = "http://localhost:8080") // 允许前端Vue项目访问
public class ProcessController {
    @Autowired
    private PartService partService;

    /**
     * 修改工序信息
     * @param id 工序ID
     * @param processDTO 工序信息
     * @return 修改结果
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateProcess(@PathVariable Integer id, @RequestBody ProcessDTO processDTO) {
        try {
            ProcessDTO updatedProcess = partService.updateProcess(id, processDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "工序信息修改成功");
            response.put("process", updatedProcess);
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "工序信息修改失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 删除工序
     * @param id 工序ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteProcess(@PathVariable Integer id) {
        try {
            partService.deleteProcess(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "工序删除成功");
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "工序删除失败: " + e.getMessage());
            return errorResponse;
        }
    }
}
