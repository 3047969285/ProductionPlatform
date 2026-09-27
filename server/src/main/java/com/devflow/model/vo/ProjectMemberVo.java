package com.devflow.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectMemberVo {
    private Long id;
    private Long projectId;
    private Long userId;
    private String username;
    private String nickname;
    private String role;
    private LocalDateTime joinedAt;
}
