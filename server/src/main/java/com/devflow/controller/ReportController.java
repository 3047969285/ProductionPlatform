package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 报表中心接口
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 工作项状态分布
     *
     * @return 需求/任务/缺陷状态统计
     */
    @GetMapping("/status")
    public ApiResult<Map<String, Object>> status() {
        return ApiResult.ok(reportService.statusOverview());
    }

    /**
     * 缺陷严重程度分布
     *
     * @return 严重程度统计
     */
    @GetMapping("/bug-severity")
    public ApiResult<List<Map<String, Object>>> bugSeverity() {
        return ApiResult.ok(reportService.bugSeverity());
    }

    /**
     * 各项目工作项数量
     *
     * @return 项目工作项统计
     */
    @GetMapping("/projects")
    public ApiResult<List<Map<String, Object>>> projects() {
        return ApiResult.ok(reportService.workByProject());
    }

    /**
     * 各迭代工作项数量
     *
     * @return 迭代工作项统计
     */
    @GetMapping("/sprints")
    public ApiResult<List<Map<String, Object>>> sprints() {
        return ApiResult.ok(reportService.workBySprint());
    }

    /**
     * 成员负载
     *
     * @return 成员负载统计
     */
    @GetMapping("/assignee")
    public ApiResult<List<Map<String, Object>>> assignee() {
        return ApiResult.ok(reportService.assigneeLoad());
    }

    /**
     * 最近 30 天新增趋势
     *
     * @return 趋势统计
     */
    @GetMapping("/trend")
    public ApiResult<Map<String, Object>> trend() {
        return ApiResult.ok(reportService.trend());
    }

    /**
     * 项目完成率
     *
     * @return 完成率统计
     */
    @GetMapping("/completion")
    public ApiResult<List<Map<String, Object>>> completion() {
        return ApiResult.ok(reportService.projectCompletion());
    }
}
