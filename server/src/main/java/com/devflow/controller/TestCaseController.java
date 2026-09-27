package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.TestCaseDto;
import com.devflow.model.vo.TestCaseVo;
import com.devflow.service.TestCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试用例接口
 */
@RestController
@RequestMapping("/api/test-cases")
@RequiredArgsConstructor
@Validated
public class TestCaseController {

    private final TestCaseService testCaseService;

    /**
     * 查询用例列表
     *
     * @param projectId 项目编号可选按项目筛选
     * @return 用例视图列表
     */
    @GetMapping
    public ApiResult<List<TestCaseVo>> list(@RequestParam(required = false) Long projectId) {
        if (projectId != null) {
            return ApiResult.ok(testCaseService.list(projectId));
        }
        return ApiResult.ok(testCaseService.listAll());
    }

    /**
     * 新增用例
     *
     * @param dto 用例信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@Validated(ValidGroups.Create.class) @RequestBody TestCaseDto dto) {
        BizAssert.isTrue(testCaseService.save(dto, true), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新用例
     *
     * @param dto 用例信息须包含用例编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@Validated(ValidGroups.Update.class) @RequestBody TestCaseDto dto) {
        BizAssert.isTrue(testCaseService.save(dto, false), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 删除用例
     *
     * @param id 用例编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        BizAssert.isTrue(testCaseService.delete(id), "删除失败");
        return ApiResult.ok();
    }
}
