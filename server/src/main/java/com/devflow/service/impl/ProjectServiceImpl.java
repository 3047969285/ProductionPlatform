package com.devflow.service.impl;

import com.devflow.common.BeanConvert;
import com.devflow.common.exception.BizAssert;
import com.devflow.mapper.ApiDocMapper;
import com.devflow.mapper.DocFolderMapper;
import com.devflow.mapper.OpsIssueMapper;
import com.devflow.mapper.ProjectMapper;
import com.devflow.mapper.RequirementMapper;
import com.devflow.mapper.TestItemMapper;
import com.devflow.model.dto.ProjectDto;
import com.devflow.model.entity.Project;
import com.devflow.model.vo.ProjectOverviewVo;
import com.devflow.model.vo.ProjectVo;
import com.devflow.model.vo.RequirementVo;
import com.devflow.service.ApiDocService;
import com.devflow.service.OpsIssueService;
import com.devflow.service.ProjectService;
import com.devflow.service.RequirementService;
import com.devflow.service.TestItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目管理服务实现
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper mapper;
    private final ApiDocMapper apiDocMapper;
    private final RequirementMapper requirementMapper;
    private final TestItemMapper testItemMapper;
    private final OpsIssueMapper opsIssueMapper;
    private final DocFolderMapper docFolderMapper;
    private final RequirementService requirementService;
    private final ApiDocService apiDocService;
    private final TestItemService testItemService;
    private final OpsIssueService opsIssueService;

    private static final List<String> REQUIREMENT_STATUSES =
            List.of("draft", "review", "approved", "developing", "done", "rejected");
    private static final List<String> TEST_STATUSES = List.of("pending", "running", "done");
    private static final List<String> OPS_STATUSES = List.of("open", "processing", "resolved");
    private static final int RECENT_REQUIREMENT_LIMIT = 5;

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
     * 汇总单个项目的需求、接口、测试与运维数据。
     *
     * @param id 项目编号
     * @return 项目概览视图
     */
    @Override
    public ProjectOverviewVo getOverview(Long id) {
        BizAssert.notNull(mapper.findById(id), "项目不存在");

        ProjectOverviewVo overview = new ProjectOverviewVo();
        List<RequirementVo> requirements = requirementService.listByProject(id, null);
        overview.setRequirements(buildStatusStats(requirements, REQUIREMENT_STATUSES, RequirementVo::getStatus));
        overview.setApis(apiDocService.listByProject(id, null).size());
        overview.setTests(buildStatusStats(
                testItemService.listByProject(id), TEST_STATUSES, item -> item.getStatus()));
        overview.setOps(buildStatusStats(
                opsIssueService.listByProject(id), OPS_STATUSES, item -> item.getStatus()));
        overview.setRecentRequirements(requirements.stream().limit(RECENT_REQUIREMENT_LIMIT).toList());
        return overview;
    }

    /**
     * 按状态统计列表条目数量。
     *
     * @param items 原始列表
     * @param statuses 需要统计的状态键
     * @param statusGetter 状态读取函数
     * @param <T> 列表元素类型
     * @return 状态与数量映射，包含 total
     */
    private <T> Map<String, Integer> buildStatusStats(
            List<T> items, List<String> statuses, java.util.function.Function<T, String> statusGetter) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("total", items.size());
        for (String status : statuses) {
            int count = 0;
            for (T item : items) {
                if (status.equals(statusGetter.apply(item))) {
                    count++;
                }
            }
            stats.put(status, count);
        }
        return stats;
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
     * 删除项目及其关联数据
     *
     * @param id 项目编号
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean delete(Long id) {
        BizAssert.notNull(mapper.findById(id), "项目不存在");
        // 先删子表再删项目避免外键约束失败
        apiDocMapper.deleteByProjectId(id);
        requirementMapper.deleteByProjectId(id);
        testItemMapper.deleteByProjectId(id);
        opsIssueMapper.deleteByProjectId(id);
        docFolderMapper.deleteByProjectId(id);
        return mapper.deleteById(id) > 0;
    }
}
