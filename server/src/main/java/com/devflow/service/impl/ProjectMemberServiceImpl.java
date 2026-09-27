package com.devflow.service.impl;

import com.devflow.model.dto.ProjectMemberDto;
import com.devflow.mapper.ProjectMemberMapper;
import com.devflow.model.entity.ProjectMember;
import com.devflow.model.vo.ProjectMemberVo;
import com.devflow.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目成员服务实现
 */
@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberMapper mapper;

    /**
     * 查询项目成员
     *
     * @param projectId 项目编号
     * @return 成员视图列表
     */
    @Override
    public List<ProjectMemberVo> list(Long projectId) {
        return mapper.findByProject(projectId);
    }

    /**
     * 新增成员
     *
     * @param dto 成员信息
     * @return 是否成功
     */
    @Override
    public boolean add(ProjectMemberDto dto) {
        // 成员已存在则只更新角色，避免唯一键冲突
        ProjectMemberVo existing = mapper.findByProjectAndUser(dto.getProjectId(), dto.getUserId());
        if (existing != null) {
            ProjectMember upd = new ProjectMember();
            upd.setId(existing.getId());
            upd.setRole(dto.getRole() == null ? existing.getRole() : dto.getRole());
            return mapper.update(upd) > 0;
        }
        ProjectMember m = new ProjectMember();
        m.setProjectId(dto.getProjectId());
        m.setUserId(dto.getUserId());
        m.setRole(dto.getRole() == null ? "developer" : dto.getRole());
        m.setJoinedAt(LocalDateTime.now());
        return mapper.insert(m) > 0;
    }

    /**
     * 更新成员角色
     *
     * @param dto 成员信息
     * @return 是否成功
     */
    @Override
    public boolean update(ProjectMemberDto dto) {
        ProjectMember m = new ProjectMember();
        m.setId(dto.getId());
        m.setRole(dto.getRole());
        return mapper.update(m) > 0;
    }

    /**
     * 删除成员
     *
     * @param id 成员编号
     * @return 是否成功
     */
    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
