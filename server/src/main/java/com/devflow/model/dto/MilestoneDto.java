package com.devflow.model.dto;

import com.devflow.common.validation.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 里程碑请求
 */
@Data
public class MilestoneDto {

    @NotNull(message = "ID 不能为空", groups = ValidGroups.Update.class)
    private Long id;

    @NotBlank(message = "里程碑名称不能为空", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String name;

    @NotNull(message = "请选择项目", groups = ValidGroups.Create.class)
    private Long projectId;

    private String description;
    private LocalDate dueDate;
    private String status;
}
