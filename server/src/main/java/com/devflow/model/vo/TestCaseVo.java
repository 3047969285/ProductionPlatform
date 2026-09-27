package com.devflow.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestCaseVo {
    private Long id;
    private Long projectId;
    private String projectName;
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
