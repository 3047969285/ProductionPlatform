package com.devflow.model.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SprintVo {
    private Long id;
    private Long projectId;
    private String projectName;
    private String name;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDateTime createdAt;
    // 统计字段
    private Integer taskTotal;
    private Integer taskDone;
    private Integer bugTotal;
    private Integer bugClosed;
    private Integer workTotal;
    private Integer workDone;
    private Integer progress;
}
