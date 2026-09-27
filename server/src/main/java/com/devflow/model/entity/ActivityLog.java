package com.devflow.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityLog {
    private Long id;
    private String workType;
    private Long workId;
    private String action;
    private String operator;
    private String detail;
    private LocalDateTime createdAt;
}
