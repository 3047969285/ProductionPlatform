package com.devflow.model.dto;

import com.devflow.common.validation.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 测试用例请求
 */
@Data
public class TestCaseDto {

    @NotNull(message = "ID 不能为空", groups = ValidGroups.Update.class)
    private Long id;

    @NotBlank(message = "用例标题不能为空", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String title;

    @NotNull(message = "请选择项目", groups = ValidGroups.Create.class)
    private Long projectId;

    private String preconditions;
    private String steps;
    private String expectedResult;
    private String priority;
    private String status;
    private String owner;
}
