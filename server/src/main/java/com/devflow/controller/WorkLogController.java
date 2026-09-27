package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.model.dto.WorkCommentDto;
import com.devflow.model.vo.ActivityLogVo;
import com.devflow.model.vo.WorkCommentVo;
import com.devflow.service.WorkLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作项评论与操作历史接口
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class WorkLogController {

    private final WorkLogService workLogService;

    /**
     * 查询工作项评论
     *
     * @param workType 工作类型
     * @param workId 工作项编号
     * @return 评论视图列表
     */
    @GetMapping("/comments")
    public ApiResult<List<WorkCommentVo>> comments(
            @RequestParam String workType, @RequestParam Long workId) {
        return ApiResult.ok(workLogService.comments(workType, workId));
    }

    /**
     * 新增评论
     *
     * @param dto 评论内容
     * @param operator 操作人
     * @return 操作结果
     */
    @PostMapping("/comments")
    public ApiResult<Void> addComment(@Valid @RequestBody WorkCommentDto dto,
                                      @RequestHeader(value = "X-Operator", required = false) String operator) {
        BizAssert.isTrue(workLogService.addComment(dto, operator == null ? "anonymous" : operator), "评论失败");
        return ApiResult.ok();
    }

    /**
     * 删除评论
     *
     * @param id 评论编号
     * @return 操作结果
     */
    @DeleteMapping("/comments/{id}")
    public ApiResult<Void> deleteComment(@PathVariable Long id) {
        BizAssert.isTrue(workLogService.deleteComment(id), "删除失败");
        return ApiResult.ok();
    }

    /**
     * 查询工作项操作历史
     *
     * @param workType 工作类型
     * @param workId 工作项编号
     * @return 历史视图列表
     */
    @GetMapping("/activities")
    public ApiResult<List<ActivityLogVo>> activities(
            @RequestParam String workType, @RequestParam Long workId) {
        return ApiResult.ok(workLogService.activities(workType, workId));
    }
}
