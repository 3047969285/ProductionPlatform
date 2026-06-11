package com.example.productionplatform.admin.service;

import com.example.productionplatform.admin.mapper.RequirementMapper;
import com.example.productionplatform.admin.model.Requirement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RequirementService {

    private final RequirementMapper mapper;

    public List<Requirement> listAll() {
        return mapper.findAll();
    }

    public List<Requirement> listRecent(int limit) {
        return mapper.findRecent(limit);
    }

    public Map<String, Integer> getStats() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("total", mapper.countAll());
        stats.put("review", mapper.countByStatus("review"));
        stats.put("approved", mapper.countByStatus("approved"));
        stats.put("developing", mapper.countByStatus("developing"));
        stats.put("done", mapper.countByStatus("done"));
        return stats;
    }

    public boolean add(Requirement req) {
        LocalDateTime now = LocalDateTime.now();
        if (req.getReqNo() == null || req.getReqNo().isBlank()) {
            req.setReqNo("REQ-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        }
        if (req.getStatus() == null || req.getStatus().isBlank()) req.setStatus("draft");
        if (req.getPriority() == null || req.getPriority().isBlank()) req.setPriority("medium");
        req.setCreateTime(now);
        req.setUpdateTime(now);
        return mapper.insert(req) > 0;
    }

    public boolean update(Requirement req) {
        req.setUpdateTime(LocalDateTime.now());
        return mapper.update(req) > 0;
    }

    public boolean updateStatus(Long id, String status) {
        return mapper.updateStatus(id, status) > 0;
    }

    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
