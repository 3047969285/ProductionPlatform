package com.example.productionplatform.admin.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QualityRecord {
    private Long id;
    private Long orderId;
    private String orderNo;
    private String productName;
    private String result;
    private Integer defectCount;
    private String inspector;
    private String remark;
    private LocalDateTime inspectTime;
}
