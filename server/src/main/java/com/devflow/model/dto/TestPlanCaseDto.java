package com.devflow.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 计划-用例执行请求
 */
@Data
public class TestPlanCaseDto {

    @NotNull(message = "计划编号不能为空")
    private Long planId;

    @NotNull(message = "用例编号不能为空")
    private Long caseId;

    private String status;
    private String actualResult;
    private String executor;
}
