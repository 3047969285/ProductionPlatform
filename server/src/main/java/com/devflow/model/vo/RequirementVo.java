package com.devflow.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RequirementVo {
    private Long id;
    private String reqNo;
    private String title;
    private String content;
    private Long projectId;
    private Long folderId;
    private String projectName;
    private String folderName;
    private String priority;
    private String status;
    private String proposer;
    private String owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
