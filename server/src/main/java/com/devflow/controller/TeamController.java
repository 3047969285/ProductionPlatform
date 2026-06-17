package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.DevTeamDto;
import com.devflow.service.TeamService;
import com.devflow.model.vo.DevTeamVo;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 研发团队接口
 */
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Validated
public class TeamController {

    private final TeamService teamService;

    /**
     * 查询团队列表
     *
     * @return 团队视图列表
     */
    @GetMapping
    public ApiResult<List<DevTeamVo>> list() {
        return ApiResult.ok(teamService.list());
    }

    /**
     * 新增团队
     *
     * @param dto 团队信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@Validated(ValidGroups.Create.class) @RequestBody DevTeamDto dto) {
        BizAssert.isTrue(teamService.save(dto, true), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新团队
     *
     * @param dto 团队信息须包含团队编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@Validated(ValidGroups.Update.class) @RequestBody DevTeamDto dto) {
        BizAssert.isTrue(teamService.save(dto, false), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 删除团队
     *
     * @param id 团队编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        BizAssert.isTrue(teamService.delete(id), "删除失败");
        return ApiResult.ok();
    }
}
