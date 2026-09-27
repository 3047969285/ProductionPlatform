package com.devflow.model.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Milestone {
    private Long id;
    private Long projectId;
    private String name;
    private String description;
    private LocalDate dueDate;
    private String status;
    private LocalDateTime createdAt;
}
