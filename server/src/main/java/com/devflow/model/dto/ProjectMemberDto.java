package com.devflow.model.dto;

import com.devflow.common.validation.ValidGroups;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 项目成员请求
 */
@Data
public class ProjectMemberDto {

    @NotNull(message = "ID 不能为空", groups = ValidGroups.Update.class)
    private Long id;

    // projectId 由 URL 路径传入，不需要 body 校验
    private Long projectId;

    @NotNull(message = "请选择用户", groups = ValidGroups.Create.class)
    private Long userId;

    private String role;
}