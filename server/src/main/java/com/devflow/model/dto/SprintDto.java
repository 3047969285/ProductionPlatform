package com.devflow.model.dto;

import com.devflow.common.validation.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 迭代请求
 */
@Data
public class SprintDto {

    @NotNull(message = "ID 不能为空", groups = ValidGroups.Update.class)
    private Long id;

    @NotBlank(message = "迭代名称不能为空", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String name;

    @NotNull(message = "请选择项目", groups = ValidGroups.Create.class)
    private Long projectId;

    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
