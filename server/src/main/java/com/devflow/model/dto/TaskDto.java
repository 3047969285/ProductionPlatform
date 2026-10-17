package com.devflow.model.dto;

import com.devflow.common.validation.ValidGroups;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 任务请求
 */
@Data
public class TaskDto {

    @NotNull(message = "ID 不能为空", groups = ValidGroups.Update.class)
    private Long id;

    @NotBlank(message = "标题不能为空", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    @Size(max = 200, message = "标题不能超过 200 个字符", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String title;

    private String content;

    @NotNull(message = "请选择项目", groups = ValidGroups.Create.class)
    private Long projectId;

    private Long sprintId;
    @Pattern(regexp = "high|medium|low", message = "优先级只能是 high、medium 或 low", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String priority;
    @Pattern(regexp = "todo|doing|done", message = "任务状态只能是 todo、doing 或 done", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String status;
    private String assignee;
    private String creator;
    @Min(value = 0, message = "预估工时不能为负数", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    @Max(value = 10000, message = "预估工时不能超过 10000 小时", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private Integer estimateHours;
}
