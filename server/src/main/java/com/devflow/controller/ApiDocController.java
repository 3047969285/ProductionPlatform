package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.ApiDocDto;
import com.devflow.service.ApiDocService;
import com.devflow.model.vo.ApiDocVo;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 接口文档管理接口
 */
@RestController
@RequestMapping("/api/apis")
@RequiredArgsConstructor
@Validated
public class ApiDocController {

    private final ApiDocService apiDocService;

    /**
     * 查询接口文档列表
     *
     * @param projectId 项目编号可选传入时按项目筛选
     * @param folderId 目录编号可选与项目编号配合按目录筛选
     * @return 接口文档视图列表
     */
    @GetMapping
    public ApiResult<List<ApiDocVo>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long folderId) {
        // 有项目 ID 则按项目/目录筛，否则查全部
        if (projectId != null) {
            return ApiResult.ok(apiDocService.listByProject(projectId, folderId));
        }
        return ApiResult.ok(apiDocService.list());
    }

    /**
     * 新增接口文档
     *
     * @param dto 接口文档信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@Validated(ValidGroups.Create.class) @RequestBody ApiDocDto dto) {
        BizAssert.isTrue(apiDocService.save(dto, true), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新接口文档
     *
     * @param dto 接口文档信息须包含文档编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@Validated(ValidGroups.Update.class) @RequestBody ApiDocDto dto) {
        BizAssert.isTrue(apiDocService.save(dto, false), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 删除接口文档
     *
     * @param id 文档编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        BizAssert.isTrue(apiDocService.delete(id), "删除失败");
        return ApiResult.ok();
    }
}
