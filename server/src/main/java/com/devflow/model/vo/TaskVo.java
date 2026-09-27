package com.devflow.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskVo {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long sprintId;
    private String sprintName;
    private String title;
    private String content;
    private String priority;
    private String status;
    private String assignee;
    private String creator;
    private Integer estimateHours;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
