package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.MilestoneDto;
import com.devflow.model.vo.MilestoneVo;
import com.devflow.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 里程碑管理接口
 */
@RestController
@RequestMapping("/api/milestones")
@RequiredArgsConstructor
@Validated
public class MilestoneController {

    private final MilestoneService milestoneService;

    /**
     * 查询项目里程碑列表
     *
     * @param projectId 项目编号
     * @return 里程碑视图列表
     */
    @GetMapping
    public ApiResult<List<MilestoneVo>> list(@RequestParam Long projectId) {
        return ApiResult.ok(milestoneService.list(projectId));
    }

    /**
     * 新增里程碑
     *
     * @param dto 里程碑信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@Validated(ValidGroups.Create.class) @RequestBody MilestoneDto dto) {
        BizAssert.isTrue(milestoneService.save(dto, true), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新里程碑
     *
     * @param dto 里程碑信息须包含里程碑编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@Validated(ValidGroups.Update.class) @RequestBody MilestoneDto dto) {
        BizAssert.isTrue(milestoneService.save(dto, false), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 删除里程碑
     *
     * @param id 里程碑编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        BizAssert.isTrue(milestoneService.delete(id), "删除失败");
        return ApiResult.ok();
    }
}
