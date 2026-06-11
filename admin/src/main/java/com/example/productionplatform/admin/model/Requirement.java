package com.example.productionplatform.admin.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Requirement {
    private Long id;
    private String reqNo;
    private String title;
    private String description;
    private Long productId;
    private Long orderId;
    private String productName;
    private String orderNo;
    /** high | medium | low */
    private String priority;
    /** draft | review | approved | developing | done | rejected */
    private String status;
    private String proposer;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
