package com.devflow.model.dto;

import com.devflow.common.validation.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 缺陷请求
 */
@Data
public class BugDto {

    @NotNull(message = "ID 不能为空", groups = ValidGroups.Update.class)
    private Long id;

    @NotBlank(message = "标题不能为空", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String title;

    private String content;

    @NotNull(message = "请选择项目", groups = ValidGroups.Create.class)
    private Long projectId;

    private Long sprintId;
    private String severity;
    private String priority;
    private String status;
    private String steps;
    private String expectedResult;
    private String actualResult;
    private String assignee;
    private String reporter;
    private String fixVersion;
}
