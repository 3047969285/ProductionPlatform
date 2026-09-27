package com.devflow.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 报表数据访问
 */
@Mapper
public interface ReportMapper {

    /**
     * 需求状态分布
     *
     * @return 状态与数量映射列表
     */
    List<Map<String, Object>> requirementStatus();

    /**
     * 任务状态分布
     *
     * @return 状态与数量映射列表
     */
    List<Map<String, Object>> taskStatus();

    /**
     * 缺陷状态分布
     *
     * @return 状态与数量映射列表
     */
    List<Map<String, Object>> bugStatus();

    /**
     * 缺陷严重程度分布
     *
     * @return 严重程度与数量映射列表
     */
    List<Map<String, Object>> bugSeverity();

    /**
     * 各项目工作项数量
     *
     * @return 项目与数量映射列表
     */
    List<Map<String, Object>> workByProject();

    /**
     * 各迭代工作项数量
     *
     * @return 迭代与数量映射列表
     */
    List<Map<String, Object>> workBySprint();

    /**
     * 各成员负责的任务数量
     *
     * @return 成员与数量映射列表
     */
    List<Map<String, Object>> taskByAssignee();

    /**
     * 各成员负责的缺陷数量
     *
     * @return 成员与数量映射列表
     */
    List<Map<String, Object>> bugByAssignee();

    /**
     * 最近 30 天每日新增需求数
     *
     * @return 日期与数量映射列表
     */
    List<Map<String, Object>> requirementTrend();

    /**
     * 最近 30 天每日新增缺陷数
     *
     * @return 日期与数量映射列表
     */
    List<Map<String, Object>> bugTrend();

    /**
     * 项目完成率（需求完成数/总数）
     *
     * @return 项目与完成率映射列表
     */
    List<Map<String, Object>> projectCompletion();
}
