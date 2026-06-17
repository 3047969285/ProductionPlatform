package com.devflow.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DevTeamVo {
    private Long id;
    private String code;
    private String name;
    private Integer capacity;
    private String status;
    private LocalDateTime createdAt;
}
