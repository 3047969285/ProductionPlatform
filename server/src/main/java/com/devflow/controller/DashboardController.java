package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.service.DashboardService;
import com.devflow.model.vo.DashboardVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 仪表盘接口
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取研发概览数据
     *
     * @return 项目需求接口测试运维及团队等汇总信息
     */
    @GetMapping
    public ApiResult<DashboardVo> overview() {
        // 聚合各模块统计数据，一次返回给首页
        return ApiResult.ok(dashboardService.overview());
    }
}
