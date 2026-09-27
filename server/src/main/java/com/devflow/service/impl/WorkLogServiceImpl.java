package com.devflow.service.impl;

import com.devflow.mapper.ActivityLogMapper;
import com.devflow.mapper.WorkCommentMapper;
import com.devflow.model.dto.WorkCommentDto;
import com.devflow.model.entity.ActivityLog;
import com.devflow.model.entity.WorkComment;
import com.devflow.model.vo.ActivityLogVo;
import com.devflow.model.vo.WorkCommentVo;
import com.devflow.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作项评论与操作历史服务实现
 */
@Service
@RequiredArgsConstructor
public class WorkLogServiceImpl implements WorkLogService {

    private final WorkCommentMapper commentMapper;
    private final ActivityLogMapper activityMapper;

    /**
     * 查询工作项评论
     *
     * @param workType 工作类型
     * @param workId 工作项编号
     * @return 评论视图列表
     */
    @Override
    public List<WorkCommentVo> comments(String workType, Long workId) {
        return commentMapper.findByWork(workType, workId);
    }

    /**
     * 新增评论
     *
     * @param dto 评论内容
     * @param operator 操作人
     * @return 是否成功
     */
    @Override
    public boolean addComment(WorkCommentDto dto, String operator) {
        WorkComment c = new WorkComment();
        c.setWorkType(dto.getWorkType());
        c.setWorkId(dto.getWorkId());
        c.setUserName(operator);
        c.setContent(dto.getContent());
        c.setCreatedAt(LocalDateTime.now());
        boolean ok = commentMapper.insert(c) > 0;
        if (ok) {
            log(dto.getWorkType(), dto.getWorkId(), "comment", operator, "添加评论");
        }
        return ok;
    }

    /**
     * 删除评论
     *
     * @param id 评论编号
     * @return 是否成功
     */
    @Override
    public boolean deleteComment(Long id) {
        return commentMapper.deleteById(id) > 0;
    }

    /**
     * 查询工作项操作历史
     *
     * @param workType 工作类型
     * @param workId 工作项编号
     * @return 历史视图列表
     */
    @Override
    public List<ActivityLogVo> activities(String workType, Long workId) {
        return activityMapper.findByWork(workType, workId);
    }

    /**
     * 记录操作历史
     *
     * @param workType 工作类型
     * @param workId 工作项编号
     * @param action 动作
     * @param operator 操作人
     * @param detail 详情
     */
    @Override
    public void log(String workType, Long workId, String action, String operator, String detail) {
        ActivityLog log = new ActivityLog();
        log.setWorkType(workType);
        log.setWorkId(workId);
        log.setAction(action);
        log.setOperator(operator);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        activityMapper.insert(log);
    }
}
