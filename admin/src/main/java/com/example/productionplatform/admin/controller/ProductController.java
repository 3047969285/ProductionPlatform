package com.example.productionplatform.admin.controller;

import com.example.productionplatform.admin.model.Product;
import com.example.productionplatform.admin.service.ProductService;
import com.example.productionplatform.admin.common.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/list")
    public ApiResult<List<Product>> list() {
        return ApiResult.ok(productService.listAll());
    }

    @PostMapping("/add")
    public ApiResult<Void> add(@RequestBody Product product) {
        if (product.getCode() == null || product.getCode().isBlank()) return ApiResult.badRequest("编码不能为空");
        if (product.getName() == null || product.getName().isBlank()) return ApiResult.badRequest("名称不能为空");
        return productService.add(product) ? ApiResult.ok() : ApiResult.fail("新增失败");
    }

    @PutMapping("/update")
    public ApiResult<Void> update(@RequestBody Product product) {
        if (product.getId() == null) return ApiResult.badRequest("ID 不能为空");
        return productService.update(product) ? ApiResult.ok() : ApiResult.fail("更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        return productService.delete(id) ? ApiResult.ok() : ApiResult.fail("删除失败");
    }
}
