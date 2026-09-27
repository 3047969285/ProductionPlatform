package com.devflow.mapper;

import com.devflow.model.entity.WorkComment;
import com.devflow.model.vo.WorkCommentVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 工作项评论数据访问
 */
@Mapper
public interface WorkCommentMapper {

    /**
     * 按工作项查询评论
     *
     * @param workType 工作类型
     * @param workId 工作项编号
     * @return 评论视图列表
     */
    List<WorkCommentVo> findByWork(String workType, Long workId);

    /**
     * 新增评论
     *
     * @param entity 评论实体
     * @return 影响行数
     */
    int insert(WorkComment entity);

    /**
     * 删除评论
     *
     * @param id 评论编号
     * @return 影响行数
     */
    int deleteById(Long id);
}
