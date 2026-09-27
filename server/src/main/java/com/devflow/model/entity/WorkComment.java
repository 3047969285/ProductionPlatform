package com.devflow.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkComment {
    private Long id;
    private String workType;
    private Long workId;
    private Long userId;
    private String userName;
    private String content;
    private LocalDateTime createdAt;
}
