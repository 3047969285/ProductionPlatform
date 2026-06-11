package com.example.productionplatform.admin.controller;

import com.example.productionplatform.admin.common.ApiResult;
import com.example.productionplatform.admin.model.ProductionOrder;
import com.example.productionplatform.admin.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
public class ProductionController {

    private static final Set<String> VALID_STATUS = Set.of("pending", "running", "done");
    private final ProductionService productionService;

    @GetMapping("/list")
    public ApiResult<List<ProductionOrder>> list() {
        return ApiResult.ok(productionService.listAll());
    }

    @PostMapping("/add")
    public ApiResult<Void> add(@RequestBody ProductionOrder order) {
        if (order.getProductId() == null) return ApiResult.badRequest("product required");
        if (order.getQuantity() == null || order.getQuantity() <= 0) return ApiResult.badRequest("quantity required");
        return productionService.add(order) ? ApiResult.ok() : ApiResult.fail("add failed");
    }

    @PutMapping("/update")
    public ApiResult<Void> update(@RequestBody ProductionOrder order) {
        if (order.getId() == null) return ApiResult.badRequest("id required");
        return productionService.update(order) ? ApiResult.ok() : ApiResult.fail("update failed");
    }

    @PutMapping("/status/{id}")
    public ApiResult<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        if (!VALID_STATUS.contains(status)) return ApiResult.badRequest("invalid status");
        return productionService.updateStatus(id, status) ? ApiResult.ok() : ApiResult.fail("status update failed");
    }

    @DeleteMapping("/delete/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        return productionService.delete(id) ? ApiResult.ok() : ApiResult.fail("delete failed");
    }
}
