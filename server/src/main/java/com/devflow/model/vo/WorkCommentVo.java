package com.devflow.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkCommentVo {
    private Long id;
    private String workType;
    private Long workId;
    private Long userId;
    private String userName;
    private String content;
    private LocalDateTime createdAt;
}
