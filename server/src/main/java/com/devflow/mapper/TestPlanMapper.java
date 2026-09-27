package com.devflow.mapper;

import com.devflow.model.entity.TestPlan;
import com.devflow.model.vo.TestPlanCaseVo;
import com.devflow.model.vo.TestPlanVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 测试计划数据访问
 */
@Mapper
public interface TestPlanMapper {

    /**
     * 按项目查询计划
     *
     * @param projectId 项目编号
     * @return 计划视图列表
     */
    List<TestPlanVo> findByProject(Long projectId);

    /**
     * 新增计划
     *
     * @param entity 计划实体
     * @return 影响行数
     */
    int insert(TestPlan entity);

    /**
     * 更新计划
     *
     * @param entity 计划实体
     * @return 影响行数
     */
    int update(TestPlan entity);

    /**
     * 删除计划
     *
     * @param id 计划编号
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 向计划加入用例
     *
     * @param planId 计划编号
     * @param caseId 用例编号
     * @return 影响行数
     */
    int addCase(Long planId, Long caseId);

    /**
     * 查询计划内用例
     *
     * @param planId 计划编号
     * @return 用例执行视图列表
     */
    List<TestPlanCaseVo> findCases(Long planId);

    /**
     * 统计计划内用例状态
     *
     * @param planId 计划编号
     * @return 状态与数量映射
     */
    List<java.util.Map<String, Object>> countCasesByStatus(Long planId);

    /**
     * 更新用例执行结果
     *
     * @param entity 执行信息
     * @return 影响行数
     */
    int updateCaseResult(com.devflow.model.entity.TestPlanCase entity);

    /**
     * 从计划移除用例
     *
     * @param planId 计划编号
     * @param caseId 用例编号
     * @return 影响行数
     */
    int removeCase(Long planId, Long caseId);

    /**
     * 统计计划总数
     *
     * @return 计划数量
     */
    int count();

    /**
     * 删除项目全部计划及其用例关联（项目删除级联）
     *
     * @param projectId 项目编号
     * @return 影响行数
     */
    int deletePlanCasesByProject(Long projectId);

    /**
     * 删除项目全部计划（项目删除级联）
     *
     * @param projectId 项目编号
     * @return 影响行数
     */
    int deleteByProject(Long projectId);
}