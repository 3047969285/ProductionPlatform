package com.devflow.model.dto;

import com.devflow.common.validation.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 项目请求
 */
@Data
public class ProjectDto {

    @NotNull(message = "ID 不能为空", groups = ValidGroups.Update.class)
    private Long id;

    @NotBlank(message = "编码不能为空", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String code;

    @NotBlank(message = "名称不能为空", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String name;

    private String description;
    private String techStack;
    private String deliveryType;
}
