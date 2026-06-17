package com.devflow.model.dto;

import com.devflow.common.validation.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 文档目录请求
 */
@Data
public class DocFolderDto {

    @NotNull(message = "ID 不能为空", groups = ValidGroups.Update.class)
    private Long id;

    @NotNull(message = "项目不能为空", groups = ValidGroups.Create.class)
    private Long projectId;

    @NotBlank(message = "模块类型不能为空", groups = ValidGroups.Create.class)
    @Pattern(regexp = "requirement|api", message = "模块类型只能是 requirement 或 api", groups = ValidGroups.Create.class)
    private String moduleType;

    private Long parentId;

    @NotBlank(message = "目录名称不能为空", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String name;

    private Integer sortOrder;
}
