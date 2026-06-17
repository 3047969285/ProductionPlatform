package com.devflow.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestItemVo {
    private Long id;
    private Long projectId;
    private String projectName;
    private String title;
    private String description;
    private Integer progress;
    private String owner;
    private String proposer;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
