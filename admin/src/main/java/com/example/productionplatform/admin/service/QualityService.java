package com.example.productionplatform.admin.service;

import com.example.productionplatform.admin.mapper.QualityMapper;
import com.example.productionplatform.admin.model.QualityRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QualityService {

    private final QualityMapper mapper;

    public List<QualityRecord> listAll() {
        return mapper.findAll();
    }

    public Map<String, Integer> getStats() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("total", mapper.countAll());
        stats.put("pass", mapper.countByResult("pass"));
        stats.put("fail", mapper.countByResult("fail"));
        return stats;
    }

    public boolean add(QualityRecord record) {
        record.setInspectTime(LocalDateTime.now());
        if (record.getDefectCount() == null) {
            record.setDefectCount(0);
        }
        return mapper.insert(record) > 0;
    }

    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
