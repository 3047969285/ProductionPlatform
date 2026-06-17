package com.devflow.model.dto;

import com.devflow.common.validation.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 运维问题请求
 */
@Data
public class OpsIssueDto {

    @NotNull(message = "ID 不能为空", groups = ValidGroups.Update.class)
    private Long id;

    @NotNull(message = "项目不能为空", groups = ValidGroups.Create.class)
    private Long projectId;

    @NotBlank(message = "标题不能为空", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String title;

    private String content;
    private String severity;
    private String status;
    private String owner;
    private String reporter;
}
