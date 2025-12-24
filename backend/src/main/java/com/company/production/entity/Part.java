package com.company.production.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "part")
public class Part {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "productName", nullable = false)
    private String productName;

    @Column(name = "productType", nullable = false, unique = true)
    private String productType;

    // 核准状态：0-未核准，1-已核准
    @Column(name = "isApproved", nullable = false, columnDefinition = "int default 0")
    private Integer isApproved;

    // 核准人ID
    @Column(name = "approvedBy")
    private Integer approvedBy;

    // 核准时间
    @Column(name = "approvedAt")
    private LocalDateTime approvedAt;

    // 最后修改人ID
    @Column(name = "lastModifiedBy")
    private Integer lastModifiedBy;

    // 最后修改时间
    @Column(name = "lastModifiedAt")
    private LocalDateTime lastModifiedAt;

    // 一对多关联，一个零件对应多个工序
    @OneToMany(mappedBy = "part")
    private List<Process> processes;
}