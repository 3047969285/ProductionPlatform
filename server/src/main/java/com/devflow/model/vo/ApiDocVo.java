package com.devflow.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiDocVo {
    private Long id;
    private String apiNo;
    private String title;
    private String content;
    private String method;
    private String path;
    private Long projectId;
    private Long folderId;
    private String projectName;
    private String folderName;
    private String status;
    private String proposer;
    private String owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
