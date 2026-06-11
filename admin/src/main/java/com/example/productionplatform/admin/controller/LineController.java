package com.example.productionplatform.admin.controller;

import com.example.productionplatform.admin.model.ProductionLine;
import com.example.productionplatform.admin.service.LineService;
import com.example.productionplatform.admin.common.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/line")
@RequiredArgsConstructor
public class LineController {

    private final LineService lineService;

    @GetMapping("/list")
    public ApiResult<List<ProductionLine>> list() {
        return ApiResult.ok(lineService.listAll());
    }

    @PostMapping("/add")
    public ApiResult<Void> add(@RequestBody ProductionLine line) {
        if (line.getCode() == null || line.getCode().isBlank()) return ApiResult.badRequest("编码不能为空");
        if (line.getName() == null || line.getName().isBlank()) return ApiResult.badRequest("名称不能为空");
        return lineService.add(line) ? ApiResult.ok() : ApiResult.fail("新增失败");
    }

    @PutMapping("/update")
    public ApiResult<Void> update(@RequestBody ProductionLine line) {
        if (line.getId() == null) return ApiResult.badRequest("ID 不能为空");
        return lineService.update(line) ? ApiResult.ok() : ApiResult.fail("更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        return lineService.delete(id) ? ApiResult.ok() : ApiResult.fail("删除失败");
    }
}
