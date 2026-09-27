package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.BugDto;
import com.devflow.model.vo.BugVo;
import com.devflow.service.BugService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 缺陷管理接口
 */
@RestController
@RequestMapping("/api/bugs")
@RequiredArgsConstructor
@Validated
public class BugController {

    private final BugService bugService;

    /**
     * 查询缺陷列表
     *
     * @param projectId 项目编号可选按项目筛选
     * @return 缺陷视图列表
     */
    @GetMapping
    public ApiResult<List<BugVo>> list(@RequestParam(required = false) Long projectId) {
        if (projectId != null) {
            return ApiResult.ok(bugService.listByProject(projectId));
        }
        return ApiResult.ok(bugService.list());
    }

    /**
     * 新增缺陷
     *
     * @param dto 缺陷信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@Validated(ValidGroups.Create.class) @RequestBody BugDto dto) {
        BizAssert.isTrue(bugService.save(dto, true), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新缺陷
     *
     * @param dto 缺陷信息须包含缺陷编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@Validated(ValidGroups.Update.class) @RequestBody BugDto dto) {
        BizAssert.isTrue(bugService.save(dto, false), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 更新缺陷状态
     *
     * @param id 缺陷编号
     * @param status 目标状态
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public ApiResult<Void> status(
            @PathVariable Long id,
            @RequestParam @Pattern(regexp = "open|fixing|resolved|closed|reopened", message = "无效状态") String status) {
        BizAssert.isTrue(bugService.updateStatus(id, status), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 删除缺陷
     *
     * @param id 缺陷编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        BizAssert.isTrue(bugService.delete(id), "删除失败");
        return ApiResult.ok();
    }
}
