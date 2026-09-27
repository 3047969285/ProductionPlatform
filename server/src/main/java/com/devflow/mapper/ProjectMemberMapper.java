package com.devflow.mapper;

import com.devflow.model.vo.ProjectMemberVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 项目成员数据访问
 */
@Mapper
public interface ProjectMemberMapper {

    /**
     * 查询项目成员列表（联表用户信息）
     *
     * @param projectId 项目编号
     * @return 成员视图列表
     */
    List<ProjectMemberVo> findByProject(Long projectId);

    /**
     * 按项目与用户查询成员
     *
     * @param projectId 项目编号
     * @param userId 用户编号
     * @return 成员视图未找到返回空
     */
    ProjectMemberVo findByProjectAndUser(Long projectId, Long userId);

    /**
     * 新增成员
     *
     * @param entity 成员信息
     * @return 影响行数
     */
    int insert(com.devflow.model.entity.ProjectMember entity);

    /**
     * 更新成员角色
     *
     * @param entity 成员信息
     * @return 影响行数
     */
    int update(com.devflow.model.entity.ProjectMember entity);

    /**
     * 删除成员
     *
     * @param id 成员编号
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 删除项目全部成员（项目删除级联）
     *
     * @param projectId 项目编号
     * @return 影响行数
     */
    int deleteByProject(Long projectId);
}
