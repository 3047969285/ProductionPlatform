package com.devflow.mapper;

import com.devflow.model.vo.SprintVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 迭代数据访问
 */
@Mapper
public interface SprintMapper {

    /**
     * 按项目查询迭代
     *
     * @param projectId 项目编号
     * @return 迭代视图列表
     */
    List<SprintVo> findByProject(Long projectId);

    /**
     * 按编号查询迭代
     *
     * @param id 迭代编号
     * @return 迭代视图
     */
    SprintVo findById(Long id);

    /**
     * 新增迭代
     *
     * @param entity 迭代实体
     * @return 影响行数
     */
    int insert(com.devflow.model.entity.Sprint entity);

    /**
     * 更新迭代
     *
     * @param entity 迭代实体
     * @return 影响行数
     */
    int update(com.devflow.model.entity.Sprint entity);

    /**
     * 删除迭代
     *
     * @param id 迭代编号
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 删除项目全部迭代（项目删除级联）
     *
     * @param projectId 项目编号
     * @return 影响行数
     */
    int deleteByProject(Long projectId);

    /**
     * 解除任务对迭代的引用
     *
     * @param sprintId 迭代编号
     * @return 影响行数
     */
    int unlinkTasks(Long sprintId);

    /**
     * 解除缺陷对迭代的引用
     *
     * @param sprintId 迭代编号
     * @return 影响行数
     */
    int unlinkBugs(Long sprintId);

    /**
     * 统计迭代内任务按状态分组数量
     *
     * @param sprintId 迭代编号
     * @return 状态与数量映射
     */
    List<Map<String, Object>> countTasksByStatus(Long sprintId);

    /**
     * 统计迭代内缺陷按状态分组数量
     *
     * @param sprintId 迭代编号
     * @return 状态与数量映射
     */
    List<Map<String, Object>> countBugsByStatus(Long sprintId);
}