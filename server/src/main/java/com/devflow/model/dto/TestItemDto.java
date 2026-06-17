package com.devflow.model.dto;

import com.devflow.common.validation.ValidGroups;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 测试项请求
 */
@Data
public class TestItemDto {

    @NotNull(message = "ID 不能为空", groups = ValidGroups.Update.class)
    private Long id;

    @NotNull(message = "项目不能为空", groups = ValidGroups.Create.class)
    private Long projectId;

    @NotBlank(message = "标题不能为空", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String title;

    private String description;

    @Min(value = 0, message = "进度不能小于 0", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    @Max(value = 100, message = "进度不能大于 100", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private Integer progress;

    private String owner;
    private String proposer;
    private String status;
}
