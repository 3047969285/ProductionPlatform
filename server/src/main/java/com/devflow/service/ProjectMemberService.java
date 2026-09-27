package com.devflow.service;

import com.devflow.model.dto.ProjectMemberDto;
import com.devflow.model.vo.ProjectMemberVo;

import java.util.List;

/**
 * 项目成员服务
 */
public interface ProjectMemberService {

    /**
     * 查询项目成员
     *
     * @param projectId 项目编号
     * @return 成员视图列表
     */
    List<ProjectMemberVo> list(Long projectId);

    /**
     * 新增成员
     *
     * @param dto 成员信息
     * @return 是否成功
     */
    boolean add(ProjectMemberDto dto);

    /**
     * 更新成员角色
     *
     * @param dto 成员信息
     * @return 是否成功
     */
    boolean update(ProjectMemberDto dto);

    /**
     * 删除成员
     *
     * @param id 成员编号
     * @return 是否成功
     */
    boolean delete(Long id);
}
