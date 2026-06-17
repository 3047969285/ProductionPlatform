package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.OpsIssueDto;
import com.devflow.service.OpsIssueService;
import com.devflow.model.vo.OpsIssueVo;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 运维问题管理接口
 */
@RestController
@RequestMapping("/api/ops")
@RequiredArgsConstructor
@Validated
public class OpsIssueController {

    private final OpsIssueService opsIssueService;

    /**
     * 查询运维问题列表
     *
     * @param projectId 项目编号可选传入时按项目筛选
     * @return 运维问题视图列表
     */
    @GetMapping
    public ApiResult<List<OpsIssueVo>> list(@RequestParam(required = false) Long projectId) {
        // 有项目 ID 则按项目筛，否则查全部
        if (projectId != null) {
            return ApiResult.ok(opsIssueService.listByProject(projectId));
        }
        return ApiResult.ok(opsIssueService.list());
    }

    /**
     * 新增运维问题
     *
     * @param dto 运维问题信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@Validated(ValidGroups.Create.class) @RequestBody OpsIssueDto dto) {
        BizAssert.isTrue(opsIssueService.save(dto, true), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新运维问题
     *
     * @param dto 运维问题信息须包含问题编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@Validated(ValidGroups.Update.class) @RequestBody OpsIssueDto dto) {
        BizAssert.isTrue(opsIssueService.save(dto, false), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 删除运维问题
     *
     * @param id 问题编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        BizAssert.isTrue(opsIssueService.delete(id), "删除失败");
        return ApiResult.ok();
    }
}
