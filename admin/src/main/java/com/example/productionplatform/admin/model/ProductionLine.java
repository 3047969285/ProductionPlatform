package com.example.productionplatform.admin.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductionLine {
    private Long id;
    private String code;
    private String name;
    private Integer capacity;
    private String status;
    private LocalDateTime createTime;
}
