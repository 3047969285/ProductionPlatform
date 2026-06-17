package com.devflow.service.impl;

import com.devflow.service.ApiDocService;
import com.devflow.service.DashboardService;
import com.devflow.service.OpsIssueService;
import com.devflow.service.ProjectService;
import com.devflow.service.RequirementService;
import com.devflow.service.TeamService;
import com.devflow.service.TestItemService;
import com.devflow.model.vo.DashboardVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 仪表盘概览服务实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProjectService projectService;
    private final RequirementService requirementService;
    private final ApiDocService apiDocService;
    private final TestItemService testItemService;
    private final OpsIssueService opsIssueService;
    private final TeamService teamService;

    /**
     * 获取研发概览数据
     *
     * @return 项目需求接口测试运维团队等汇总视图
     */
    @Override
    public DashboardVo overview() {
        DashboardVo vo = new DashboardVo();

        // 汇总各模块数量统计
        vo.setProjects(projectService.count());
        vo.setRequirements(requirementService.stats()); // 按状态分组的需求数
        vo.setApis(apiDocService.count());
        vo.setTests(testItemService.stats());
        vo.setOps(opsIssueService.stats());

        // 团队：总数 + 活跃数
        vo.setTeams(Map.of("total", teamService.count(), "active", teamService.countActive()));

        // 最近更新的需求列表，用于首页动态展示
        vo.setRecentRequirements(requirementService.recent(5));
        return vo;
    }
}
