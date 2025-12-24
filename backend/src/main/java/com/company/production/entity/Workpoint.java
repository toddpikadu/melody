package com.company.production.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "workpoint")
public class Workpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "productName", nullable = false)
    private String productName;

    @Column(name = "productType", nullable = false)
    private String productType;

    @Column(name = "debugTime", nullable = false, columnDefinition = "double default 0")
    private Double debugTime;

    @Column(name = "workPoints", nullable = false, columnDefinition = "double default 0")
    private Double workPoints;

    @Column(name = "processID", nullable = false)
    private String processID;

    @Column(name = "process", nullable = false)
    private String process;

    @Column(name = "order")
    private Integer order;
}