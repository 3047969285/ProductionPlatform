package com.example.productionplatform.admin.controller;

import com.example.productionplatform.admin.common.ApiResult;
import com.example.productionplatform.admin.model.Requirement;
import com.example.productionplatform.admin.service.RequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/requirement")
@RequiredArgsConstructor
public class RequirementController {

    private static final Set<String> VALID_STATUS = Set.of(
            "draft", "review", "approved", "developing", "done", "rejected");
    private static final Set<String> VALID_PRIORITY = Set.of("high", "medium", "low");

    private final RequirementService requirementService;

    @GetMapping("/list")
    public ApiResult<List<Requirement>> list() {
        return ApiResult.ok(requirementService.listAll());
    }

    @PostMapping("/add")
    public ApiResult<Void> add(@RequestBody Requirement req) {
        if (req.getTitle() == null || req.getTitle().isBlank()) return ApiResult.badRequest("title required");
        if (req.getProductId() == null) return ApiResult.badRequest("product required");
        if (req.getPriority() != null && !VALID_PRIORITY.contains(req.getPriority())) {
            return ApiResult.badRequest("invalid priority");
        }
        return requirementService.add(req) ? ApiResult.ok() : ApiResult.fail("add failed");
    }

    @PutMapping("/update")
    public ApiResult<Void> update(@RequestBody Requirement req) {
        if (req.getId() == null) return ApiResult.badRequest("id required");
        if (req.getPriority() != null && !VALID_PRIORITY.contains(req.getPriority())) {
            return ApiResult.badRequest("invalid priority");
        }
        return requirementService.update(req) ? ApiResult.ok() : ApiResult.fail("update failed");
    }

    @PutMapping("/status/{id}")
    public ApiResult<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        if (!VALID_STATUS.contains(status)) return ApiResult.badRequest("invalid status");
        return requirementService.updateStatus(id, status) ? ApiResult.ok() : ApiResult.fail("status update failed");
    }

    @DeleteMapping("/delete/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        return requirementService.delete(id) ? ApiResult.ok() : ApiResult.fail("delete failed");
    }
}
