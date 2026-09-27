package com.devflow.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestPlanCaseVo {
    private Long id;
    private Long planId;
    private Long caseId;
    private String caseTitle;
    private String priority;
    private String preconditions;
    private String steps;
    private String expectedResult;
    private String status;
    private String actualResult;
    private String executor;
    private LocalDateTime executedAt;
}
