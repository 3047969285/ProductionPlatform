package com.devflow.model.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TestPlanVo {
    private Long id;
    private Long projectId;
    private String projectName;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 统计字段
    private Integer caseTotal;
    private Integer casePass;
    private Integer caseFail;
    private Integer casePending;
    private Integer progress;
}
