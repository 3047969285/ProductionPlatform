package com.devflow.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApiDoc {
    private Long id;
    private String apiNo;
    private String title;
    private String content;
    private String method;
    private String path;
    private Long projectId;
    private Long folderId;
    private String status;
    private String proposer;
    private String owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
