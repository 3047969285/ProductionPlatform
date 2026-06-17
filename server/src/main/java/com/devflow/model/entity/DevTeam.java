package com.devflow.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DevTeam {
    private Long id;
    private String code;
    private String name;
    private Integer capacity;
    private String status;
    private LocalDateTime createdAt;
}
