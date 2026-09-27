package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.ProjectMemberDto;
import com.devflow.model.vo.ProjectMemberVo;
import com.devflow.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目成员接口
 */
@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
@Validated
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    /**
     * 查询项目成员列表
     *
     * @param projectId 项目编号
     * @return 成员视图列表
     */
    @GetMapping
    public ApiResult<List<ProjectMemberVo>> list(@PathVariable Long projectId) {
        return ApiResult.ok(projectMemberService.list(projectId));
    }

    /**
     * 新增成员
     *
     * @param projectId 项目编号
     * @param dto 成员信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@PathVariable Long projectId,
                               @Validated(ValidGroups.Create.class) @RequestBody ProjectMemberDto dto) {
        dto.setProjectId(projectId);
        BizAssert.isTrue(projectMemberService.add(dto), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新成员角色
     *
     * @param projectId 项目编号
     * @param dto 成员信息须包含成员编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@PathVariable Long projectId,
                                  @Validated(ValidGroups.Update.class) @RequestBody ProjectMemberDto dto) {
        BizAssert.isTrue(projectMemberService.update(dto), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 移除成员
     *
     * @param projectId 项目编号
     * @param id 成员编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long projectId, @PathVariable Long id) {
        BizAssert.isTrue(projectMemberService.delete(id), "删除失败");
        return ApiResult.ok();
    }
}
