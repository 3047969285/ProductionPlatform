package com.example.productionplatform.admin.service;

import com.example.productionplatform.admin.mapper.LineMapper;
import com.example.productionplatform.admin.model.ProductionLine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LineService {

    private final LineMapper mapper;

    public List<ProductionLine> listAll() {
        return mapper.findAll();
    }

    public int count() {
        return mapper.countAll();
    }

    public int countActive() {
        return mapper.countActive();
    }

    public boolean add(ProductionLine line) {
        line.setCreateTime(LocalDateTime.now());
        if (line.getStatus() == null || line.getStatus().isBlank()) {
            line.setStatus("active");
        }
        return mapper.insert(line) > 0;
    }

    public boolean update(ProductionLine line) {
        return mapper.update(line) > 0;
    }

    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
