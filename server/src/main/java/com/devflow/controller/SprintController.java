package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.SprintDto;
import com.devflow.model.vo.SprintVo;
import com.devflow.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 迭代管理接口
 */
@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
@Validated
public class SprintController {

    private final SprintService sprintService;

    /**
     * 查询项目迭代列表
     *
     * @param projectId 项目编号
     * @return 迭代视图列表
     */
    @GetMapping
    public ApiResult<List<SprintVo>> list(@RequestParam Long projectId) {
        return ApiResult.ok(sprintService.list(projectId));
    }

    /**
     * 查询迭代详情
     *
     * @param id 迭代编号
     * @return 迭代详情
     */
    @GetMapping("/{id}")
    public ApiResult<SprintVo> get(@PathVariable Long id) {
        SprintVo vo = sprintService.getById(id);
        BizAssert.notNullResource(vo, "迭代不存在");
        return ApiResult.ok(vo);
    }

    /**
     * 查询迭代燃尽图数据
     *
     * @param id 迭代编号
     * @return 燃尽图数据
     */
    @GetMapping("/{id}/burndown")
    public ApiResult<Map<String, Object>> burndown(@PathVariable Long id) {
        BizAssert.notNullResource(sprintService.getById(id), "迭代不存在");
        return ApiResult.ok(sprintService.burndown(id));
    }

    /**
     * 新增迭代
     *
     * @param dto 迭代信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@Validated(ValidGroups.Create.class) @RequestBody SprintDto dto) {
        BizAssert.isTrue(sprintService.save(dto, true), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新迭代
     *
     * @param dto 迭代信息须包含迭代编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@Validated(ValidGroups.Update.class) @RequestBody SprintDto dto) {
        BizAssert.isTrue(sprintService.save(dto, false), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 删除迭代
     *
     * @param id 迭代编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        BizAssert.isTrue(sprintService.delete(id), "删除失败");
        return ApiResult.ok();
    }
}
