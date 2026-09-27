package com.devflow.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReleaseRecord {
    private Long id;
    private Long projectId;
    private String version;
    private String environment;
    private String description;
    private String status;
    private String operator;
    private LocalDateTime releasedAt;
    private LocalDateTime createdAt;
}
