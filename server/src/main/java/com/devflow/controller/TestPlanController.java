package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.TestPlanCaseDto;
import com.devflow.model.dto.TestPlanDto;
import com.devflow.model.vo.TestPlanCaseVo;
import com.devflow.model.vo.TestPlanVo;
import com.devflow.service.TestPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试计划接口
 */
@RestController
@RequestMapping("/api/test-plans")
@RequiredArgsConstructor
@Validated
public class TestPlanController {

    private final TestPlanService testPlanService;

    /**
     * 查询项目测试计划列表
     *
     * @param projectId 项目编号
     * @return 计划视图列表
     */
    @GetMapping
    public ApiResult<List<TestPlanVo>> list(@RequestParam Long projectId) {
        return ApiResult.ok(testPlanService.list(projectId));
    }

    /**
     * 新增测试计划
     *
     * @param dto 计划信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@Validated(ValidGroups.Create.class) @RequestBody TestPlanDto dto) {
        BizAssert.isTrue(testPlanService.save(dto, true), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新测试计划
     *
     * @param dto 计划信息须包含计划编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@Validated(ValidGroups.Update.class) @RequestBody TestPlanDto dto) {
        BizAssert.isTrue(testPlanService.save(dto, false), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 删除测试计划
     *
     * @param id 计划编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        BizAssert.isTrue(testPlanService.delete(id), "删除失败");
        return ApiResult.ok();
    }

    /**
     * 向计划批量加入用例
     *
     * @param id 计划编号
     * @param caseIds 用例编号列表
     * @return 操作结果
     */
    @PostMapping("/{id}/cases")
    public ApiResult<Void> addCases(@PathVariable Long id, @RequestBody List<Long> caseIds) {
        BizAssert.isTrue(testPlanService.addCases(id, caseIds), "加入用例失败");
        return ApiResult.ok();
    }

    /**
     * 查询计划内用例
     *
     * @param id 计划编号
     * @return 用例执行视图列表
     */
    @GetMapping("/{id}/cases")
    public ApiResult<List<TestPlanCaseVo>> cases(@PathVariable Long id) {
        return ApiResult.ok(testPlanService.cases(id));
    }

    /**
     * 更新用例执行结果
     *
     * @param id 计划编号
     * @param dto 执行信息
     * @return 操作结果
     */
    @PutMapping("/{id}/cases")
    public ApiResult<Void> execute(@PathVariable Long id, @Valid @RequestBody TestPlanCaseDto dto) {
        dto.setPlanId(id);
        BizAssert.isTrue(testPlanService.execute(dto), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 从计划移除用例
     *
     * @param id 计划编号
     * @param caseId 用例编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}/cases/{caseId}")
    public ApiResult<Void> removeCase(@PathVariable Long id, @PathVariable Long caseId) {
        BizAssert.isTrue(testPlanService.removeCase(id, caseId), "移除失败");
        return ApiResult.ok();
    }
}
