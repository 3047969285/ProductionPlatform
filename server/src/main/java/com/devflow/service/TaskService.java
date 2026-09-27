package com.devflow.service;

import com.devflow.model.dto.TaskDto;
import com.devflow.model.vo.TaskVo;

import java.util.List;
import java.util.Map;

/**
 * 任务服务
 */
public interface TaskService {

    /**
     * 查询全部任务
     *
     * @return 任务视图列表
     */
    List<TaskVo> list();

    /**
     * 按项目查询任务
     *
     * @param projectId 项目编号
     * @return 任务视图列表
     */
    List<TaskVo> listByProject(Long projectId);

    /**
     * 按迭代查询任务
     *
     * @param sprintId 迭代编号
     * @return 任务视图列表
     */
    List<TaskVo> listBySprint(Long sprintId);

    /**
     * 统计任务总数
     *
     * @return 任务数量
     */
    int count();

    /**
     * 统计各状态任务数量
     *
     * @return 状态与数量映射
     */
    Map<String, Integer> stats();

    /**
     * 新增或更新任务
     *
     * @param dto 任务信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    boolean save(TaskDto dto, boolean isNew);

    /**
     * 更新任务状态
     *
     * @param id 任务编号
     * @param status 目标状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, String status);

    /**
     * 删除任务
     *
     * @param id 任务编号
     * @return 是否成功
     */
    boolean delete(Long id);
}