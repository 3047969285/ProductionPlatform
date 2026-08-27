package com.devflow.service;

import com.devflow.model.dto.ProjectDto;
import com.devflow.model.vo.ProjectOverviewVo;
import com.devflow.model.vo.ProjectVo;

import java.util.List;

/**
 * 项目管理服务
 */
public interface ProjectService {

    /**
     * 查询全部项目
     *
     * @return 项目视图列表
     */
    List<ProjectVo> list();

    /**
     * 根据编号查询项目
     *
     * @param id 项目编号
     * @return 项目视图未找到返回空
     */
    ProjectVo getById(Long id);

    /**
     * 获取单个项目的研发交付概览。
     *
     * @param id 项目编号
     * @return 需求/接口/测试/运维汇总
     */
    ProjectOverviewVo getOverview(Long id);

    /**
     * 统计项目总数
     *
     * @return 项目数量
     */
    int count();

    /**
     * 新增或更新项目
     *
     * @param dto 项目信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    boolean save(ProjectDto dto, boolean isNew);

    /**
     * 删除项目
     *
     * @param id 项目编号
     * @return 是否成功
     */
    boolean delete(Long id);
}
