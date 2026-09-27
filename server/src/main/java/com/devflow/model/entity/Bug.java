package com.devflow.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Bug {
    private Long id;
    private Long projectId;
    private Long sprintId;
    private String title;
    private String content;
    private String severity;
    private String priority;
    private String status;
    private String steps;
    private String expectedResult;
    private String actualResult;
    private String assignee;
    private String reporter;
    private String fixVersion;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
