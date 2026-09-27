package com.devflow.mapper;

import com.devflow.model.vo.TaskVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 任务数据访问
 */
@Mapper
public interface TaskMapper {

    /**
     * 查询全部任务
     *
     * @return 任务视图列表
     */
    List<TaskVo> findAll();

    /**
     * 按项目查询任务
     *
     * @param projectId 项目编号
     * @return 任务视图列表
     */
    List<TaskVo> findByProject(Long projectId);

    /**
     * 按迭代查询任务
     *
     * @param sprintId 迭代编号
     * @return 任务视图列表
     */
    List<TaskVo> findBySprint(Long sprintId);

    /**
     * 统计任务总数
     *
     * @return 任务数量
     */
    int count();

    /**
     * 按状态统计任务数
     *
     * @param status 状态
     * @return 任务数量
     */
    int countByStatus(String status);

    /**
     * 新增任务
     *
     * @param entity 任务实体
     * @return 影响行数
     */
    int insert(com.devflow.model.entity.Task entity);

    /**
     * 更新任务
     *
     * @param entity 任务实体
     * @return 影响行数
     */
    int update(com.devflow.model.entity.Task entity);

    /**
     * 更新任务状态（完成时记录完成时间）
     *
     * @param id 任务编号
     * @param status 目标状态
     * @param resolvedAt 完成时间可为空
     * @return 影响行数
     */
    int updateStatus(Long id, String status, java.time.LocalDateTime resolvedAt);

    /**
     * 删除任务
     *
     * @param id 任务编号
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 删除项目全部任务（项目删除级联）
     *
     * @param projectId 项目编号
     * @return 影响行数
     */
    int deleteByProject(Long projectId);
}
