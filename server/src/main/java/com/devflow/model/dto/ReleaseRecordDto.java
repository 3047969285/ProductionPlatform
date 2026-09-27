package com.devflow.model.dto;

import com.devflow.common.validation.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发布记录请求
 */
@Data
public class ReleaseRecordDto {

    @NotNull(message = "ID 不能为空", groups = ValidGroups.Update.class)
    private Long id;

    @NotBlank(message = "版本号不能为空", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String version;

    @NotNull(message = "请选择项目", groups = ValidGroups.Create.class)
    private Long projectId;

    private String environment;
    private String description;
    private String status;
    private String operator;
}
