package com.devflow.service.impl;

import com.devflow.mapper.TestPlanMapper;
import com.devflow.model.dto.TestPlanCaseDto;
import com.devflow.model.dto.TestPlanDto;
import com.devflow.model.entity.TestPlan;
import com.devflow.model.entity.TestPlanCase;
import com.devflow.model.vo.TestPlanCaseVo;
import com.devflow.model.vo.TestPlanVo;
import com.devflow.service.TestPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试计划服务实现
 */
@Service
@RequiredArgsConstructor
public class TestPlanServiceImpl implements TestPlanService {

    private final TestPlanMapper mapper;

    /**
     * 按项目查询计划
     *
     * @param projectId 项目编号
     * @return 计划视图列表
     */
    @Override
    public List<TestPlanVo> list(Long projectId) {
        List<TestPlanVo> list = mapper.findByProject(projectId);
        for (TestPlanVo vo : list) {
            fillStats(vo);
        }
        return list;
    }

    /**
     * 填充计划统计
     *
     * @param vo 计划视图
     */
    private void fillStats(TestPlanVo vo) {
        int total = 0, pass = 0, fail = 0, pending = 0;
        for (Map<String, Object> row : mapper.countCasesByStatus(vo.getId())) {
            int cnt = ((Number) row.get("cnt")).intValue();
            total += cnt;
            String status = (String) row.get("status");
            switch (status == null ? "" : status) {
                case "pass" -> pass = cnt;
                case "fail" -> fail = cnt;
                case "blocked" -> {}
                default -> pending += cnt;
            }
        }
        vo.setCaseTotal(total);
        vo.setCasePass(pass);
        vo.setCaseFail(fail);
        vo.setCasePending(pending);
        vo.setProgress(total == 0 ? 0 : (int) Math.round((pass + fail) * 100.0 / total));
    }

    /**
     * 新增或更新计划
     *
     * @param dto 计划信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    @Override
    public boolean save(TestPlanDto dto, boolean isNew) {
        TestPlan p = new TestPlan();
        p.setId(dto.getId());
        p.setProjectId(dto.getProjectId());
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setStartDate(dto.getStartDate());
        p.setEndDate(dto.getEndDate());
        p.setStatus(dto.getStatus() == null ? "draft" : dto.getStatus());
        p.setOwner(dto.getOwner());
        LocalDateTime now = LocalDateTime.now();
        if (isNew) {
            p.setCreatedAt(now);
            p.setUpdatedAt(now);
            return mapper.insert(p) > 0;
        }
        p.setUpdatedAt(now);
        return mapper.update(p) > 0;
    }

    /**
     * 删除计划
     *
     * @param id 计划编号
     * @return 是否成功
     */
    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }

    /**
     * 向计划加入用例
     *
     * @param planId 计划编号
     * @param caseIds 用例编号列表
     * @return 是否成功
     */
    @Override
    public boolean addCases(Long planId, List<Long> caseIds) {
        if (caseIds == null || caseIds.isEmpty()) {
            return false;
        }
        int ok = 0;
        for (Long caseId : caseIds) {
            ok += mapper.addCase(planId, caseId);
        }
        return ok > 0;
    }

    /**
     * 查询计划内用例
     *
     * @param planId 计划编号
     * @return 用例执行视图列表
     */
    @Override
    public List<TestPlanCaseVo> cases(Long planId) {
        return mapper.findCases(planId);
    }

    /**
     * 更新用例执行结果
     *
     * @param dto 执行信息
     * @return 是否成功
     */
    @Override
    public boolean execute(TestPlanCaseDto dto) {
        TestPlanCase c = new TestPlanCase();
        c.setPlanId(dto.getPlanId());
        c.setCaseId(dto.getCaseId());
        c.setStatus(dto.getStatus() == null ? "pending" : dto.getStatus());
        c.setActualResult(dto.getActualResult());
        c.setExecutor(dto.getExecutor());
        c.setExecutedAt(LocalDateTime.now());
        return mapper.updateCaseResult(c) > 0;
    }

    /**
     * 从计划移除用例
     *
     * @param planId 计划编号
     * @param caseId 用例编号
     * @return 是否成功
     */
    @Override
    public boolean removeCase(Long planId, Long caseId) {
        return mapper.removeCase(planId, caseId) > 0;
    }

    /**
     * 统计计划总数
     *
     * @return 计划数量
     */
    @Override
    public int count() {
        return mapper.count();
    }
}
