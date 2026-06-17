package com.devflow.service.impl;

import com.devflow.common.BeanConvert;
import com.devflow.model.dto.RequirementDto;
import com.devflow.mapper.RequirementMapper;
import com.devflow.model.entity.Requirement;
import com.devflow.service.RequirementService;
import com.devflow.model.vo.RequirementVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 需求管理服务实现
 */
@Service
@RequiredArgsConstructor
public class RequirementServiceImpl implements RequirementService {

    private final RequirementMapper mapper;

    /**
     * 查询全部需求
     *
     * @return 需求视图列表
     */
    @Override
    public List<RequirementVo> list() {
        // Mapper 已联表查出项目名、目录名
        return mapper.findAll();
    }

    /**
     * 按项目或目录查询需求
     *
     * @param projectId 项目编号
     * @param folderId 目录编号可为空
     * @return 需求视图列表
     */
    @Override
    public List<RequirementVo> listByProject(Long projectId, Long folderId) {
        // 指定了目录则按目录筛，否则按项目筛
        if (folderId != null) {
            return mapper.findByFolder(projectId, folderId);
        }
        return mapper.findByProject(projectId);
    }

    /**
     * 查询最近更新的需求
     *
     * @param limit 条数上限
     * @return 需求视图列表
     */
    @Override
    public List<RequirementVo> recent(int limit) {
        return mapper.findRecent(limit);
    }

    /**
     * 统计各状态需求数量
     *
     * @return 状态与数量映射
     */
    @Override
    public Map<String, Integer> stats() {
        Map<String, Integer> s = new LinkedHashMap<>(); // 保持插入顺序，方便前端展示
        s.put("total", mapper.count());
        s.put("review", mapper.countByStatus("review"));
        s.put("developing", mapper.countByStatus("developing"));
        s.put("done", mapper.countByStatus("done"));
        return s;
    }

    /**
     * 新增或更新需求
     *
     * @param dto 需求信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    @Override
    public boolean save(RequirementDto dto, boolean isNew) {
        Requirement r = BeanConvert.toModel(dto);
        LocalDateTime now = LocalDateTime.now();
        if (isNew) {
            // 自动生成需求编号
            if (r.getReqNo() == null || r.getReqNo().isBlank()) {
                r.setReqNo("REQ-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            }
            // 填充默认状态和优先级
            if (r.getStatus() == null) {
                r.setStatus("draft");
            }
            if (r.getPriority() == null) {
                r.setPriority("medium");
            }
            r.setCreatedAt(now);
            r.setUpdatedAt(now);
            return mapper.insert(r) > 0;
        }
        // 更新时只刷新修改时间
        r.setUpdatedAt(now);
        return mapper.update(r) > 0;
    }

    /**
     * 更新需求状态
     *
     * @param id 需求编号
     * @param status 目标状态
     * @return 是否成功
     */
    @Override
    public boolean updateStatus(Long id, String status) {
        return mapper.updateStatus(id, status) > 0;
    }

    /**
     * 删除需求
     *
     * @param id 需求编号
     * @return 是否成功
     */
    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
