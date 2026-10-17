package com.devflow.model.dto;

import com.devflow.common.validation.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 缺陷请求
 */
@Data
public class BugDto {

    @NotNull(message = "ID 不能为空", groups = ValidGroups.Update.class)
    private Long id;

    @NotBlank(message = "标题不能为空", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    @Size(max = 200, message = "标题不能超过 200 个字符", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String title;

    private String content;

    @NotNull(message = "请选择项目", groups = ValidGroups.Create.class)
    private Long projectId;

    private Long sprintId;
    @Pattern(regexp = "low|medium|high|critical", message = "严重程度只能是 low、medium、high 或 critical", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String severity;
    @Pattern(regexp = "high|medium|low", message = "优先级只能是 high、medium 或 low", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String priority;
    @Pattern(regexp = "open|fixing|resolved|closed|reopened", message = "缺陷状态不合法", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String status;
    private String steps;
    private String expectedResult;
    private String actualResult;
    private String assignee;
    private String reporter;
    private String fixVersion;
}
