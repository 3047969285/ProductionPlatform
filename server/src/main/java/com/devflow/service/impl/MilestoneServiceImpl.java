package com.devflow.service.impl;

import com.devflow.model.dto.MilestoneDto;
import com.devflow.mapper.MilestoneMapper;
import com.devflow.model.entity.Milestone;
import com.devflow.service.MilestoneService;
import com.devflow.model.vo.MilestoneVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 里程碑服务实现
 */
@Service
@RequiredArgsConstructor
public class MilestoneServiceImpl implements MilestoneService {

    private final MilestoneMapper mapper;

    /**
     * 按项目查询里程碑
     *
     * @param projectId 项目编号
     * @return 里程碑视图列表
     */
    @Override
    public List<MilestoneVo> list(Long projectId) {
        return mapper.findByProject(projectId);
    }

    /**
     * 统计里程碑总数
     *
     * @return 里程碑数量
     */
    @Override
    public int count() {
        return mapper.count();
    }

    /**
     * 新增或更新里程碑
     *
     * @param dto 里程碑信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    @Override
    public boolean save(MilestoneDto dto, boolean isNew) {
        Milestone m = new Milestone();
        m.setId(dto.getId());
        m.setProjectId(dto.getProjectId());
        m.setName(dto.getName());
        m.setDescription(dto.getDescription());
        m.setDueDate(dto.getDueDate());
        m.setStatus(dto.getStatus() == null ? "pending" : dto.getStatus());
        if (isNew) {
            m.setCreatedAt(LocalDateTime.now());
            return mapper.insert(m) > 0;
        }
        return mapper.update(m) > 0;
    }

    /**
     * 删除里程碑
     *
     * @param id 里程碑编号
     * @return 是否成功
     */
    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
