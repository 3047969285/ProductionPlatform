package com.devflow.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 工作项评论请求
 */
@Data
public class WorkCommentDto {

    @NotNull(message = "工作类型不能为空")
    private String workType;

    @NotNull(message = "工作项编号不能为空")
    private Long workId;

    @NotBlank(message = "评论内容不能为空")
    private String content;
}
