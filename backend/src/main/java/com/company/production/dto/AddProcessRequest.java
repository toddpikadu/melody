package com.company.production.dto;

import lombok.Data;

@Data
public class AddProcessRequest {
    private String productName;
    private String productType;
    private Integer partId;
    private String processID;
    private String process;
    private Double debugTime;
    private Double workPoints;
    private Integer order;
}