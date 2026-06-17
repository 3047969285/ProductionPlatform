package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.TestItemDto;
import com.devflow.service.TestItemService;
import com.devflow.model.vo.TestItemVo;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试项管理接口
 */
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@Validated
public class TestItemController {

    private final TestItemService testItemService;

    /**
     * 查询测试项列表
     *
     * @param projectId 项目编号可选传入时按项目筛选
     * @return 测试项视图列表
     */
    @GetMapping
    public ApiResult<List<TestItemVo>> list(@RequestParam(required = false) Long projectId) {
        // 有项目 ID 则按项目筛，否则查全部
        if (projectId != null) {
            return ApiResult.ok(testItemService.listByProject(projectId));
        }
        return ApiResult.ok(testItemService.list());
    }

    /**
     * 新增测试项
     *
     * @param dto 测试项信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@Validated(ValidGroups.Create.class) @RequestBody TestItemDto dto) {
        BizAssert.isTrue(testItemService.save(dto, true), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新测试项
     *
     * @param dto 测试项信息须包含测试项编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@Validated(ValidGroups.Update.class) @RequestBody TestItemDto dto) {
        BizAssert.isTrue(testItemService.save(dto, false), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 删除测试项
     *
     * @param id 测试项编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        BizAssert.isTrue(testItemService.delete(id), "删除失败");
        return ApiResult.ok();
    }
}
