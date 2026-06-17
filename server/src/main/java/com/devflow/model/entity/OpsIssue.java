package com.devflow.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OpsIssue {
    private Long id;
    private Long projectId;
    private String title;
    private String content;
    private String severity;
    private String status;
    private String owner;
    private String reporter;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
