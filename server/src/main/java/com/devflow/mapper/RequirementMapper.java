package com.devflow.mapper;

import com.devflow.model.entity.Requirement;
import com.devflow.model.vo.RequirementVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 需求数据访问
 */
@Mapper
public interface RequirementMapper {

    /**
     * 查询全部需求含关联名称
     *
     * @return 需求视图列表
     */
    List<RequirementVo> findAll();

    /**
     * 按项目查询需求
     *
     * @param projectId 项目编号
     * @return 需求视图列表
     */
    List<RequirementVo> findByProject(Long projectId);

    /**
     * 按项目与目录查询需求
     *
     * @param projectId 项目编号
     * @param folderId 目录编号
     * @return 需求视图列表
     */
    List<RequirementVo> findByFolder(@Param("projectId") Long projectId, @Param("folderId") Long folderId);

    /**
     * 查询最近更新的需求
     *
     * @param n 条数上限
     * @return 需求视图列表
     */
    List<RequirementVo> findRecent(int n);

    /**
     * 统计需求总数
     *
     * @return 需求数量
     */
    int count();

    /**
     * 按状态统计需求数
     *
     * @param status 状态值
     * @return 需求数量
     */
    int countByStatus(String status);

    /**
     * 新增需求
     *
     * @param requirement 需求实体
     * @return 影响行数
     */
    int insert(Requirement requirement);

    /**
     * 更新需求
     *
     * @param requirement 需求实体
     * @return 影响行数
     */
    int update(Requirement requirement);

    /**
     * 更新需求状态
     *
     * @param id 需求编号
     * @param status 目标状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 删除需求
     *
     * @param id 需求编号
     * @return 影响行数
     */
    int deleteById(Long id);
}
