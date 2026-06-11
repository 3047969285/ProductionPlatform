package com.example.productionplatform.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductionService productionService;
    private final ProductService productService;
    private final LineService lineService;
    private final QualityService qualityService;
    private final RequirementService requirementService;

    public Map<String, Object> getOverview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orders", productionService.getStats());
        data.put("requirements", requirementService.getStats());
        data.put("products", productService.count());
        data.put("lines", Map.of("total", lineService.count(), "active", lineService.countActive()));
        data.put("quality", qualityService.getStats());
        data.put("recentOrders", productionService.listRecent(5));
        data.put("recentRequirements", requirementService.listRecent(5));
        return data;
    }
}
