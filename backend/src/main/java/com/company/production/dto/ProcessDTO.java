package com.company.production.dto;

import lombok.Data;

@Data
public class ProcessDTO {
    private Integer id;
    private String processID;
    private String process;
    private Double debugTime;
    private Double workPoints;
    private Integer order;
    private Integer partId;
}