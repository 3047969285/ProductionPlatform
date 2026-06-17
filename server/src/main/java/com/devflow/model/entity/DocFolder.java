package com.devflow.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocFolder {
    private Long id;
    private Long projectId;
    private String moduleType;
    private Long parentId;
    private String name;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
