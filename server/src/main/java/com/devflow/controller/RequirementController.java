package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.RequirementDto;
import com.devflow.service.RequirementService;
import com.devflow.model.vo.RequirementVo;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 需求管理接口
 */
@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
@Validated
public class RequirementController {

    private final RequirementService requirementService;

    /**
     * 查询需求列表
     *
     * @param projectId 项目编号可选传入时按项目筛选
     * @param folderId 目录编号可选与项目编号配合按目录筛选
     * @return 需求视图列表
     */
    @GetMapping
    public ApiResult<List<RequirementVo>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long folderId) {
        // 传了项目 ID 则按项目/目录筛，否则查全部
        if (projectId != null) {
            return ApiResult.ok(requirementService.listByProject(projectId, folderId));
        }
        return ApiResult.ok(requirementService.list());
    }

    /**
     * 新增需求
     *
     * @param dto 需求信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@Validated(ValidGroups.Create.class) @RequestBody RequirementDto dto) {
        BizAssert.isTrue(requirementService.save(dto, true), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新需求
     *
     * @param dto 需求信息须包含需求编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@Validated(ValidGroups.Update.class) @RequestBody RequirementDto dto) {
        BizAssert.isTrue(requirementService.save(dto, false), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 更新需求状态
     *
     * @param id 需求编号
     * @param status 目标状态
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public ApiResult<Void> status(
            @PathVariable Long id,
            @RequestParam @Pattern(regexp = "draft|review|approved|developing|done|rejected", message = "无效状态") String status) {
        // 只改状态字段，不走完整更新
        BizAssert.isTrue(requirementService.updateStatus(id, status), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 删除需求
     *
     * @param id 需求编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        BizAssert.isTrue(requirementService.delete(id), "删除失败");
        return ApiResult.ok();
    }
}
