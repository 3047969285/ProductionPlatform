package com.devflow.mapper;

import com.devflow.model.entity.Project;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 项目数据访问
 */
@Mapper
public interface ProjectMapper {

    /**
     * 查询全部项目
     *
     * @return 项目列表
     */
    List<Project> findAll();

    /**
     * 按编号查询项目
     *
     * @param id 项目编号
     * @return 项目实体
     */
    Project findById(Long id);

    /**
     * 统计项目总数
     *
     * @return 项目数量
     */
    int count();

    /**
     * 新增项目
     *
     * @param project 项目实体
     * @return 影响行数
     */
    int insert(Project project);

    /**
     * 更新项目
     *
     * @param project 项目实体
     * @return 影响行数
     */
    int update(Project project);

    /**
     * 删除项目
     *
     * @param id 项目编号
     * @return 影响行数
     */
    int deleteById(Long id);
}
