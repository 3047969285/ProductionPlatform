package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.ReleaseRecordDto;
import com.devflow.model.vo.ReleaseRecordVo;
import com.devflow.service.ReleaseRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 发布记录管理接口
 */
@RestController
@RequestMapping("/api/releases")
@RequiredArgsConstructor
@Validated
public class ReleaseRecordController {

    private final ReleaseRecordService releaseRecordService;

    /**
     * 查询项目发布记录列表
     *
     * @param projectId 项目编号
     * @return 发布记录视图列表
     */
    @GetMapping
    public ApiResult<List<ReleaseRecordVo>> list(@RequestParam Long projectId) {
        return ApiResult.ok(releaseRecordService.list(projectId));
    }

    /**
     * 新增发布记录
     *
     * @param dto 发布记录信息
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@Validated(ValidGroups.Create.class) @RequestBody ReleaseRecordDto dto) {
        BizAssert.isTrue(releaseRecordService.save(dto, true), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新发布记录
     *
     * @param dto 发布记录信息须包含发布记录编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@Validated(ValidGroups.Update.class) @RequestBody ReleaseRecordDto dto) {
        BizAssert.isTrue(releaseRecordService.save(dto, false), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 删除发布记录
     *
     * @param id 发布记录编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        BizAssert.isTrue(releaseRecordService.delete(id), "删除失败");
        return ApiResult.ok();
    }
}
