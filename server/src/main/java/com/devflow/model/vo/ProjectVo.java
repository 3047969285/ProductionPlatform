package com.devflow.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectVo {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String techStack;
    private String deliveryType;
    private LocalDateTime createdAt;
}
