package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.ProjectDto;
import com.devflow.service.ProjectService;
import com.devflow.model.vo.ProjectOverviewVo;
import com.devflow.model.vo.ProjectVo;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目管理接口
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Validated
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 查询项目列表
     *
     * @return 项目视图列表
     */
    @GetMapping
    public ApiResult<List<ProjectVo>> list() {
        return ApiResult.ok(projectService.list());
    }

    /**
     * 根据编号查询项目详情
     *
     * @param id 项目编号
     * @return 项目详情
     */
    @GetMapping("/{id}")
    public ApiResult<ProjectVo> get(@PathVariable Long id) {
        ProjectVo project = projectService.getById(id);
        BizAssert.notNullResource(project, "项目不存在"); // 查不到返回 404
        return ApiResult.ok(project);
    }

    /**
     * 获取项目研发交付概览
     *
     * @param id 项目编号
     * @return 需求/接口/测试/运维汇总
     */
    @GetMapping("/{id}/overview")
    public ApiResult<ProjectOverviewVo> overview(@PathVariable Long id) {
        return ApiResult.ok(projectService.getOverview(id));
    }

    /**
     * 新增项目
     *
     * @param dto 项目信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@Validated(ValidGroups.Create.class) @RequestBody ProjectDto dto) {
        BizAssert.isTrue(projectService.save(dto, true), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新项目
     *
     * @param dto 项目信息须包含项目编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@Validated(ValidGroups.Update.class) @RequestBody ProjectDto dto) {
        BizAssert.isTrue(projectService.save(dto, false), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 删除项目
     *
     * @param id 项目编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        BizAssert.isTrue(projectService.delete(id), "删除失败");
        return ApiResult.ok();
    }
}
