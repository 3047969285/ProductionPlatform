package com.devflow.service;

import com.devflow.model.dto.WorkCommentDto;
import com.devflow.model.vo.ActivityLogVo;
import com.devflow.model.vo.WorkCommentVo;

import java.util.List;

/**
 * 工作项评论与操作历史服务
 */
public interface WorkLogService {

    /**
     * 查询工作项评论
     *
     * @param workType 工作类型
     * @param workId 工作项编号
     * @return 评论视图列表
     */
    List<WorkCommentVo> comments(String workType, Long workId);

    /**
     * 新增评论
     *
     * @param dto 评论内容
     * @param operator 操作人
     * @return 是否成功
     */
    boolean addComment(WorkCommentDto dto, String operator);

    /**
     * 删除评论
     *
     * @param id 评论编号
     * @return 是否成功
     */
    boolean deleteComment(Long id);

    /**
     * 查询工作项操作历史
     *
     * @param workType 工作类型
     * @param workId 工作项编号
     * @return 历史视图列表
     */
    List<ActivityLogVo> activities(String workType, Long workId);

    /**
     * 记录操作历史
     *
     * @param workType 工作类型
     * @param workId 工作项编号
     * @param action 动作
     * @param operator 操作人
     * @param detail 详情
     */
    void log(String workType, Long workId, String action, String operator, String detail);
}
