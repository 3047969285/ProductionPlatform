package com.devflow.mapper;

import com.devflow.model.entity.Milestone;
import com.devflow.model.vo.MilestoneVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 里程碑数据访问
 */
@Mapper
public interface MilestoneMapper {

    /**
     * 按项目查询里程碑
     *
     * @param projectId 项目编号
     * @return 里程碑视图列表
     */
    List<MilestoneVo> findByProject(Long projectId);

    /**
     * 新增里程碑
     *
     * @param entity 里程碑实体
     * @return 影响行数
     */
    int insert(Milestone entity);

    /**
     * 更新里程碑
     *
     * @param entity 里程碑实体
     * @return 影响行数
     */
    int update(Milestone entity);

    /**
     * 删除里程碑
     *
     * @param id 里程碑编号
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 统计里程碑总数
     *
     * @return 里程碑数量
     */
    int count();

    /**
     * 删除项目全部里程碑（项目删除级联）
     *
     * @param projectId 项目编号
     * @return 影响行数
     */
    int deleteByProject(Long projectId);
}