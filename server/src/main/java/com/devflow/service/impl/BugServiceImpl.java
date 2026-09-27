package com.devflow.service.impl;

import com.devflow.model.dto.BugDto;
import com.devflow.mapper.BugMapper;
import com.devflow.model.entity.Bug;
import com.devflow.service.BugService;
import com.devflow.model.vo.BugVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 缺陷服务实现
 */
@Service
@RequiredArgsConstructor
public class BugServiceImpl implements BugService {

    private final BugMapper mapper;

    /**
     * 查询全部缺陷
     *
     * @return 缺陷视图列表
     */
    @Override
    public List<BugVo> list() {
        return mapper.findAll();
    }

    /**
     * 按项目查询缺陷
     *
     * @param projectId 项目编号
     * @return 缺陷视图列表
     */
    @Override
    public List<BugVo> listByProject(Long projectId) {
        return mapper.findByProject(projectId);
    }

    /**
     * 统计缺陷总数
     *
     * @return 缺陷数量
     */
    @Override
    public int count() {
        return mapper.count();
    }

    /**
     * 统计各状态缺陷数量
     *
     * @return 状态与数量映射
     */
    @Override
    public Map<String, Integer> stats() {
        Map<String, Integer> s = new LinkedHashMap<>();
        s.put("total", mapper.count());
        s.put("open", mapper.countByStatus("open"));
        s.put("fixing", mapper.countByStatus("fixing"));
        s.put("resolved", mapper.countByStatus("resolved"));
        s.put("closed", mapper.countByStatus("closed"));
        return s;
    }

    /**
     * 按严重程度统计缺陷数量
     *
     * @return 严重程度与数量映射
     */
    @Override
    public Map<String, Integer> severityStats() {
        Map<String, Integer> s = new LinkedHashMap<>();
        s.put("low", mapper.countBySeverity("low"));
        s.put("medium", mapper.countBySeverity("medium"));
        s.put("high", mapper.countBySeverity("high"));
        s.put("critical", mapper.countBySeverity("critical"));
        return s;
    }

    /**
     * 新增或更新缺陷
     *
     * @param dto 缺陷信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    @Override
    public boolean save(BugDto dto, boolean isNew) {
        Bug b = new Bug();
        b.setId(dto.getId());
        b.setProjectId(dto.getProjectId());
        b.setSprintId(dto.getSprintId());
        b.setTitle(dto.getTitle());
        b.setContent(dto.getContent());
        b.setSeverity(dto.getSeverity() == null ? "medium" : dto.getSeverity());
        b.setPriority(dto.getPriority() == null ? "medium" : dto.getPriority());
        b.setStatus(dto.getStatus() == null ? "open" : dto.getStatus());
        b.setSteps(dto.getSteps());
        b.setExpectedResult(dto.getExpectedResult());
        b.setActualResult(dto.getActualResult());
        b.setAssignee(dto.getAssignee());
        b.setReporter(dto.getReporter());
        b.setFixVersion(dto.getFixVersion());
        LocalDateTime now = LocalDateTime.now();
        boolean resolved = "resolved".equals(b.getStatus()) || "closed".equals(b.getStatus());
        if (isNew) {
            b.setCreatedAt(now);
            b.setUpdatedAt(now);
            if (resolved) {
                b.setResolvedAt(now);
            }
            return mapper.insert(b) > 0;
        }
        b.setUpdatedAt(now);
        b.setResolvedAt(resolved ? now : null);
        return mapper.update(b) > 0;
    }

    /**
     * 更新缺陷状态
     *
     * @param id 缺陷编号
     * @param status 目标状态
     * @return 是否成功
     */
    @Override
    public boolean updateStatus(Long id, String status) {
        boolean resolved = "resolved".equals(status) || "closed".equals(status);
        LocalDateTime resolvedAt = resolved ? LocalDateTime.now() : null;
        return mapper.updateStatus(id, status, resolvedAt) > 0;
    }

    /**
     * 删除缺陷
     *
     * @param id 缺陷编号
     * @return 是否成功
     */
    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
