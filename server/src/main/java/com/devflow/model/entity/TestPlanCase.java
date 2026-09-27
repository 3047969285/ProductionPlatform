package com.devflow.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestPlanCase {
    private Long id;
    private Long planId;
    private Long caseId;
    private String status;
    private String actualResult;
    private String executor;
    private LocalDateTime executedAt;
}
