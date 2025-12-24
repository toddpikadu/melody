package com.company.production.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导入历史记录实体类
 */
@Entity
@Table(name = "import_history")
@Data
public class ImportHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 导入时间
     */
    @Column(name = "import_time")
    private LocalDateTime importTime;

    /**
     * 导入的文件名
     */
    @Column(name = "file_name")
    private String fileName;

    /**
     * 成功导入的记录数
     */
    @Column(name = "success_count")
    private Integer successCount;

    /**
     * 导入失败的记录数
     */
    @Column(name = "error_count")
    private Integer errorCount;

    /**
     * 总记录数
     */
    @Column(name = "total_count")
    private Integer totalCount;

    /**
     * 导入状态：SUCCESS（成功）、FAILED（失败）、PARTIAL_SUCCESS（部分成功）
     */
    @Column(name = "status")
    private String status;

    /**
     * 操作人
     */
    @Column(name = "operator")
    private String operator;

    /**
     * 导入结果消息
     */
    @Column(name = "message")
    private String message;
}