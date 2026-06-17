package com.devflow.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Requirement {
    private Long id;
    private String reqNo;
    private String title;
    private String content;
    private Long projectId;
    private Long folderId;
    private String priority;
    private String status;
    private String proposer;
    private String owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
