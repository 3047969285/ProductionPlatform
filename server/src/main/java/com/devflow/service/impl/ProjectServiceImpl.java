package com.devflow.service.impl;

import com.devflow.common.BeanConvert;
import com.devflow.common.exception.BizAssert;
import com.devflow.mapper.ApiDocMapper;
import com.devflow.mapper.BugMapper;
import com.devflow.mapper.DocFolderMapper;
import com.devflow.mapper.MilestoneMapper;
import com.devflow.mapper.OpsIssueMapper;
import com.devflow.mapper.ProjectMapper;
import com.devflow.mapper.ProjectMemberMapper;
import com.devflow.mapper.ReleaseRecordMapper;
import com.devflow.mapper.RequirementMapper;
import com.devflow.mapper.SprintMapper;
import com.devflow.mapper.TaskMapper;
import com.devflow.mapper.TestCaseMapper;
import com.devflow.mapper.TestItemMapper;
import com.devflow.mapper.TestPlanMapper;
import com.devflow.model.dto.ProjectDto;
import com.devflow.model.entity.Project;
import com.devflow.model.vo.ProjectVo;
import com.devflow.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目管理服务实现
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper mapper;
    private final RequirementMapper requirementMapper;
    private final ApiDocMapper apiDocMapper;
    private final TestItemMapper testItemMapper;
    private final OpsIssueMapper opsIssueMapper;
    private final DocFolderMapper docFolderMapper;
    private final TaskMapper taskMapper;
    private final BugMapper bugMapper;
    private final SprintMapper sprintMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final TestCaseMapper testCaseMapper;
    private final TestPlanMapper testPlanMapper;
    private final MilestoneMapper milestoneMapper;
    private final ReleaseRecordMapper releaseRecordMapper;

    /**
     * 查询全部项目
     *
     * @return 项目视图列表
     */
    @Override
    public List<ProjectVo> list() {
        return BeanConvert.toProjectVoList(mapper.findAll());
    }

    /**
     * 根据编号查询项目
     *
     * @param id 项目编号
     * @return 项目视图未找到返回空
     */
    @Override
    public ProjectVo getById(Long id) {
        return BeanConvert.toVo(mapper.findById(id));
    }

    /**
     * 统计项目总数
     *
     * @return 项目数量
     */
    @Override
    public int count() {
        return mapper.count();
    }

    /**
     * 新增或更新项目
     *
     * @param dto 项目信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    @Override
    public boolean save(ProjectDto dto, boolean isNew) {
        Project project = BeanConvert.toModel(dto);
        if (isNew) {
            // 新增时记录创建时间
            project.setCreatedAt(LocalDateTime.now());
            return mapper.insert(project) > 0;
        }
        // 更新已有记录
        return mapper.update(project) > 0;
    }

    /**
     * 删除项目并级联清理全部关联数据
     *
     * @param id 项目编号
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        BizAssert.notNull(mapper.findById(id), "项目不存在");
        // 按外键依赖顺序清理子表
        testPlanMapper.deletePlanCasesByProject(id);
        testPlanMapper.deleteByProject(id);
        testCaseMapper.deleteByProject(id);
        milestoneMapper.deleteByProject(id);
        releaseRecordMapper.deleteByProject(id);
        bugMapper.deleteByProject(id);
        taskMapper.deleteByProject(id);
        sprintMapper.deleteByProject(id);
        projectMemberMapper.deleteByProject(id);
        requirementMapper.deleteByProject(id);
        apiDocMapper.deleteByProject(id);
        testItemMapper.deleteByProject(id);
        opsIssueMapper.deleteByProject(id);
        docFolderMapper.deleteByProject(id);
        return mapper.deleteById(id) > 0;
    }
}
