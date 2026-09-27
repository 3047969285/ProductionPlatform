package com.devflow.service;

import java.util.List;
import java.util.Map;

/**
 * 报表服务
 */
public interface ReportService {

    /**
     * 工作项状态分布（需求/任务/缺陷）
     *
     * @return 各类状态统计
     */
    Map<String, Object> statusOverview();

    /**
     * 缺陷严重程度分布
     *
     * @return 严重程度统计
     */
    List<Map<String, Object>> bugSeverity();

    /**
     * 各项目工作项数量
     *
     * @return 项目工作项统计
     */
    List<Map<String, Object>> workByProject();

    /**
     * 各迭代工作项数量
     *
     * @return 迭代工作项统计
     */
    List<Map<String, Object>> workBySprint();

    /**
     * 成员负载（任务+缺陷按负责人）
     *
     * @return 成员负载统计
     */
    List<Map<String, Object>> assigneeLoad();

    /**
     * 最近 30 天新增趋势（需求/缺陷）
     *
     * @return 趋势统计
     */
    Map<String, Object> trend();

    /**
     * 项目完成率
     *
     * @return 项目完成率统计
     */
    List<Map<String, Object>> projectCompletion();
}
