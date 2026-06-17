package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.DocFolderDto;
import com.devflow.service.DocFolderService;
import com.devflow.model.vo.DocFolderVo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文档目录接口
 */
@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
@Validated
public class DocFolderController {

    private final DocFolderService folderService;

    /**
     * 查询目录列表
     *
     * @param projectId 项目编号
     * @param moduleType 模块类型 requirement 或 api
     * @return 目录视图列表
     */
    @GetMapping
    public ApiResult<List<DocFolderVo>> list(
            @RequestParam @NotNull(message = "项目不能为空") Long projectId,
            @RequestParam @NotBlank(message = "模块类型不能为空")
            @Pattern(regexp = "requirement|api", message = "模块类型只能是 requirement 或 api") String moduleType) {
        // 按项目 + 模块类型查目录树数据
        return ApiResult.ok(folderService.list(projectId, moduleType));
    }

    /**
     * 新增目录
     *
     * @param dto 目录信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@Validated(ValidGroups.Create.class) @RequestBody DocFolderDto dto) {
        BizAssert.isTrue(folderService.save(dto, true), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新目录
     *
     * @param dto 目录信息须包含目录编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@Validated(ValidGroups.Update.class) @RequestBody DocFolderDto dto) {
        BizAssert.isTrue(folderService.save(dto, false), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 删除目录
     *
     * @param id 目录编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        BizAssert.isTrue(folderService.delete(id), "删除失败");
        return ApiResult.ok();
    }
}
