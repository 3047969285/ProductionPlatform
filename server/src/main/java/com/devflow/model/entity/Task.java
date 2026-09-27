package com.devflow.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Task {
    private Long id;
    private Long projectId;
    private Long sprintId;
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
