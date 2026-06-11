package com.example.productionplatform.admin.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductionOrder {
    private Long id;
    private String orderNo;
    private Long productId;
    private Long lineId;
    private String productName;
    private String lineName;
    private Integer quantity;
    private Integer completedQty;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
