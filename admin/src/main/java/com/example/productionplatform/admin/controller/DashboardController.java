package com.example.productionplatform.admin.controller;

import com.example.productionplatform.admin.service.DashboardService;
import com.example.productionplatform.admin.common.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ApiResult<Map<String, Object>> overview() {
        return ApiResult.ok(dashboardService.getOverview());
    }
}
