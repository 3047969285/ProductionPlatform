package com.devflow.service.impl;

import com.devflow.mapper.ReportMapper;
import com.devflow.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表服务实现
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper mapper;

    /**
     * 工作项状态分布（需求/任务/缺陷）
     *
     * @return 各类状态统计
     */
    @Override
    public Map<String, Object> statusOverview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("requirement", group(mapper.requirementStatus()));
        result.put("task", group(mapper.taskStatus()));
        result.put("bug", group(mapper.bugStatus()));
        return result;
    }

    /**
     * 缺陷严重程度分布
     *
     * @return 严重程度统计
     */
    @Override
    public List<Map<String, Object>> bugSeverity() {
        return mapper.bugSeverity();
    }

    /**
     * 各项目工作项数量
     *
     * @return 项目工作项统计
     */
    @Override
    public List<Map<String, Object>> workByProject() {
        return mapper.workByProject();
    }

    /**
     * 各迭代工作项数量
     *
     * @return 迭代工作项统计
     */
    @Override
    public List<Map<String, Object>> workBySprint() {
        return mapper.workBySprint();
    }

    /**
     * 成员负载（任务+缺陷按负责人）
     *
     * @return 成员负载统计
     */
    @Override
    public List<Map<String, Object>> assigneeLoad() {
        // 合并任务与缺陷的负责人统计
        Map<String, Integer> merged = new LinkedHashMap<>();
        for (Map<String, Object> row : mapper.taskByAssignee()) {
            String name = String.valueOf(row.get("assignee"));
            merged.merge(name, ((Number) row.get("cnt")).intValue(), Integer::sum);
        }
        for (Map<String, Object> row : mapper.bugByAssignee()) {
            String name = String.valueOf(row.get("assignee"));
            merged.merge(name, ((Number) row.get("cnt")).intValue(), Integer::sum);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        merged.forEach((name, cnt) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("assignee", name);
            m.put("cnt", cnt);
            list.add(m);
        });
        list.sort((a, b) -> ((Number) b.get("cnt")).intValue() - ((Number) a.get("cnt")).intValue());
        return list;
    }

    /**
     * 最近 30 天新增趋势（需求/缺陷）
     *
     * @return 趋势统计
     */
    @Override
    public Map<String, Object> trend() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("requirement", mapper.requirementTrend());
        result.put("bug", mapper.bugTrend());
        return result;
    }

    /**
     * 项目完成率
     *
     * @return 项目完成率统计
     */
    @Override
    public List<Map<String, Object>> projectCompletion() {
        return mapper.projectCompletion();
    }

    /**
     * 把 status/cnt 行列表转成 {status: cnt} 映射
     *
     * @param rows 行列表
     * @return 状态映射
     */
    private Map<String, Object> group(List<Map<String, Object>> rows) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            map.put(String.valueOf(row.get("status")), row.get("cnt"));
        }
        return map;
    }
}
