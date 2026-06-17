package com.devflow.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocFolderVo {
    private Long id;
    private Long projectId;
    private String moduleType;
    private Long parentId;
    private String name;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
