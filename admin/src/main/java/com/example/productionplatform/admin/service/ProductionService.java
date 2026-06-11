package com.example.productionplatform.admin.service;

import com.example.productionplatform.admin.mapper.ProductionOrderMapper;
import com.example.productionplatform.admin.model.ProductionOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductionService {

    private final ProductionOrderMapper mapper;

    public List<ProductionOrder> listAll() {
        return mapper.findAll();
    }

    public List<ProductionOrder> listRecent(int limit) {
        return mapper.findRecent(limit);
    }

    public Map<String, Integer> getStats() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("total", mapper.countAll());
        stats.put("pending", mapper.countByStatus("pending"));
        stats.put("running", mapper.countByStatus("running"));
        stats.put("done", mapper.countByStatus("done"));
        return stats;
    }

    public boolean add(ProductionOrder order) {
        LocalDateTime now = LocalDateTime.now();
        if (order.getOrderNo() == null || order.getOrderNo().isBlank()) {
            order.setOrderNo("DEV-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        }
        if (order.getStatus() == null || order.getStatus().isBlank()) {
            order.setStatus("pending");
        }
        if (order.getCompletedQty() == null) {
            order.setCompletedQty(0);
        }
        order.setCreateTime(now);
        order.setUpdateTime(now);
        return mapper.insert(order) > 0;
    }

    public boolean update(ProductionOrder order) {
        order.setUpdateTime(LocalDateTime.now());
        if (order.getCompletedQty() == null) {
            order.setCompletedQty(0);
        }
        return mapper.update(order) > 0;
    }

    public boolean updateStatus(Long id, String status) {
        return mapper.updateStatus(id, status) > 0;
    }

    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
