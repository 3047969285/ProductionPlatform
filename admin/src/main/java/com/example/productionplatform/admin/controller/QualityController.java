package com.example.productionplatform.admin.controller;

import com.example.productionplatform.admin.common.ApiResult;
import com.example.productionplatform.admin.model.QualityRecord;
import com.example.productionplatform.admin.service.QualityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/quality")
@RequiredArgsConstructor
public class QualityController {

    private static final Set<String> VALID = Set.of("pass", "fail");
    private final QualityService qualityService;

    @GetMapping("/list")
    public ApiResult<List<QualityRecord>> list() {
        return ApiResult.ok(qualityService.listAll());
    }

    @PostMapping("/add")
    public ApiResult<Void> add(@RequestBody QualityRecord record) {
        if (record.getOrderId() == null) return ApiResult.badRequest("order required");
        if (record.getResult() == null || !VALID.contains(record.getResult())) return ApiResult.badRequest("invalid result");
        if (record.getInspector() == null || record.getInspector().isBlank()) return ApiResult.badRequest("inspector required");
        return qualityService.add(record) ? ApiResult.ok() : ApiResult.fail("add failed");
    }

    @DeleteMapping("/delete/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        return qualityService.delete(id) ? ApiResult.ok() : ApiResult.fail("delete failed");
    }
}
