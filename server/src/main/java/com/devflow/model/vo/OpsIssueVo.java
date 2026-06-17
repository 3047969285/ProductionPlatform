package com.devflow.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OpsIssueVo {
    private Long id;
    private Long projectId;
    private String projectName;
    private String title;
    private String content;
    private String severity;
    private String status;
    private String owner;
    private String reporter;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
