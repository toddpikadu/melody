package com.company.production.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "process")
public class Process {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 多对一关联，多个工序属于一个零件
    @ManyToOne
    @JoinColumn(name = "partId", nullable = false)
    @JsonIgnore
    private Part part;

    @Column(name = "processID", nullable = false)
    private String processID;

    @Column(name = "process", nullable = false)
    private String process;

    @Column(name = "debugTime", nullable = false, columnDefinition = "double default 0")
    private Double debugTime;

    @Column(name = "workPoints", nullable = false, columnDefinition = "double default 0")
    private Double workPoints;

    @Column(name = "process_order")
    private Integer order;
    
    // 最后修改人ID
    @Column(name = "lastModifiedBy")
    private Integer lastModifiedBy;

    // 最后修改时间
    @Column(name = "lastModifiedAt")
    private LocalDateTime lastModifiedAt;
}