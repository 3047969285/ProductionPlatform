package com.example.productionplatform.admin.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private String code;
    private String name;
    private String spec;
    private String unit;
    private LocalDateTime createTime;
}
