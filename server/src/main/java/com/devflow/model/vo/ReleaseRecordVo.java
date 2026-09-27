package com.devflow.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReleaseRecordVo {
    private Long id;
    private Long projectId;
    private String projectName;
    private String version;
    private String environment;
    private String description;
    private String status;
    private String operator;
    private LocalDateTime releasedAt;
    private LocalDateTime createdAt;
}
