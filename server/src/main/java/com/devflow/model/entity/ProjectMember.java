package com.devflow.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectMember {
    private Long id;
    private Long projectId;
    private Long userId;
    private String role;
    private LocalDateTime joinedAt;
}
