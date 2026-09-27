package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.TaskDto;
import com.devflow.model.vo.TaskVo;
import com.devflow.service.TaskService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务管理接口
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Validated
public class TaskController {

    private final TaskService taskService;

    /**
     * 查询任务列表
     *
     * @param projectId 项目编号可选按项目筛选
     * @param sprintId 迭代编号可选按迭代筛选
     * @return 任务视图列表
     */
    @GetMapping
    public ApiResult<List<TaskVo>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long sprintId) {
        if (sprintId != null) {
            return ApiResult.ok(taskService.listBySprint(sprintId));
        }
        if (projectId != null) {
            return ApiResult.ok(taskService.listByProject(projectId));
        }
        return ApiResult.ok(taskService.list());
    }

    /**
     * 新增任务
     *
     * @param dto 任务信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@Validated(ValidGroups.Create.class) @RequestBody TaskDto dto) {
        BizAssert.isTrue(taskService.save(dto, true), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新任务
     *
     * @param dto 任务信息须包含任务编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@Validated(ValidGroups.Update.class) @RequestBody TaskDto dto) {
        BizAssert.isTrue(taskService.save(dto, false), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 更新任务状态
     *
     * @param id 任务编号
     * @param status 目标状态
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public ApiResult<Void> status(
            @PathVariable Long id,
            @RequestParam @Pattern(regexp = "todo|doing|done", message = "无效状态") String status) {
        BizAssert.isTrue(taskService.updateStatus(id, status), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 删除任务
     *
     * @param id 任务编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        BizAssert.isTrue(taskService.delete(id), "删除失败");
        return ApiResult.ok();
    }
}