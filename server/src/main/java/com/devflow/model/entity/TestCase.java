package com.devflow.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestCase {
    private Long id;
    private Long projectId;
    private String title;
    private String preconditions;
    private String steps;
    private String expectedResult;
    private String priority;
    private String status;
    private String owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
