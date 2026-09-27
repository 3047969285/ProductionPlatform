package com.devflow.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityLogVo {
    private Long id;
    private String workType;
    private Long workId;
    private String action;
    private String operator;
    private String detail;
    private LocalDateTime createdAt;
}
