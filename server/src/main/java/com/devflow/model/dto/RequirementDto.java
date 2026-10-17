package com.devflow.model.dto;

import com.devflow.common.validation.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 需求请求
 */
@Data
public class RequirementDto {

    @NotNull(message = "ID 不能为空", groups = ValidGroups.Update.class)
    private Long id;

    private String reqNo;

    @NotBlank(message = "标题不能为空", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    @Size(max = 200, message = "标题不能超过 200 个字符", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String title;

    private String content;

    @NotNull(message = "请选择项目", groups = ValidGroups.Create.class)
    private Long projectId;

    private Long folderId;
    @Pattern(regexp = "high|medium|low", message = "优先级只能是 high、medium 或 low", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String priority;
    @Pattern(regexp = "draft|review|approved|developing|done|rejected", message = "需求状态不合法", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String status;
    private String proposer;
    private String owner;
}
