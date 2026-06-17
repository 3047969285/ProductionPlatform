package com.devflow.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestItem {
    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private Integer progress;
    private String owner;
    private String proposer;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
