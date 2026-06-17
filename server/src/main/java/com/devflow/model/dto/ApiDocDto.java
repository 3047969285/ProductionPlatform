package com.devflow.model.dto;

import com.devflow.common.validation.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 接口文档请求
 */
@Data
public class ApiDocDto {

    @NotNull(message = "ID 不能为空", groups = ValidGroups.Update.class)
    private Long id;

    private String apiNo;

    @NotBlank(message = "标题不能为空", groups = {ValidGroups.Create.class, ValidGroups.Update.class})
    private String title;

    private String content;
    private String method;
    private String path;

    @NotNull(message = "项目不能为空", groups = ValidGroups.Create.class)
    private Long projectId;

    private Long folderId;
    private String status;
    private String proposer;
    private String owner;
}
