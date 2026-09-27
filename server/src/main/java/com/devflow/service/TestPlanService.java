package com.devflow.service;

import com.devflow.model.dto.TestPlanCaseDto;
import com.devflow.model.dto.TestPlanDto;
import com.devflow.model.vo.TestPlanCaseVo;
import com.devflow.model.vo.TestPlanVo;

import java.util.List;

/**
 * 测试计划服务
 */
public interface TestPlanService {

    /**
     * 按项目查询计划
     *
     * @param projectId 项目编号
     * @return 计划视图列表
     */
    List<TestPlanVo> list(Long projectId);

    /**
     * 新增或更新计划
     *
     * @param dto 计划信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    boolean save(TestPlanDto dto, boolean isNew);

    /**
     * 删除计划
     *
     * @param id 计划编号
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 向计划加入用例
     *
     * @param planId 计划编号
     * @param caseIds 用例编号列表
     * @return 是否成功
     */
    boolean addCases(Long planId, List<Long> caseIds);

    /**
     * 查询计划内用例
     *
     * @param planId 计划编号
     * @return 用例执行视图列表
     */
    List<TestPlanCaseVo> cases(Long planId);

    /**
     * 更新用例执行结果
     *
     * @param dto 执行信息
     * @return 是否成功
     */
    boolean execute(TestPlanCaseDto dto);

    /**
     * 从计划移除用例
     *
     * @param planId 计划编号
     * @param caseId 用例编号
     * @return 是否成功
     */
    boolean removeCase(Long planId, Long caseId);

    /**
     * 统计计划总数
     *
     * @return 计划数量
     */
    int count();
}
