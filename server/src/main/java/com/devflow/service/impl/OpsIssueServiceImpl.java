package com.devflow.service.impl;

import com.devflow.common.BeanConvert;
import com.devflow.model.dto.OpsIssueDto;
import com.devflow.mapper.OpsIssueMapper;
import com.devflow.model.entity.OpsIssue;
import com.devflow.service.OpsIssueService;
import com.devflow.model.vo.OpsIssueVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 运维问题服务实现
 */
@Service
@RequiredArgsConstructor
public class OpsIssueServiceImpl implements OpsIssueService {

    private final OpsIssueMapper mapper;

    /**
     * 查询全部运维问题
     *
     * @return 运维问题视图列表
     */
    @Override
    public List<OpsIssueVo> list() {
        return mapper.findAll();
    }

    /**
     * 按项目查询运维问题
     *
     * @param projectId 项目编号
     * @return 运维问题视图列表
     */
    @Override
    public List<OpsIssueVo> listByProject(Long projectId) {
        return mapper.findByProject(projectId);
    }

    /**
     * 统计各状态运维问题数量
     *
     * @return 状态与数量映射
     */
    @Override
    public Map<String, Integer> stats() {
        Map<String, Integer> s = new LinkedHashMap<>();
        s.put("total", mapper.count());
        s.put("open", mapper.countByStatus("open"));
        s.put("processing", mapper.countByStatus("processing"));
        s.put("resolved", mapper.countByStatus("resolved"));
        return s;
    }

    /**
     * 新增或更新运维问题
     *
     * @param dto 运维问题信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    @Override
    public boolean save(OpsIssueDto dto, boolean isNew) {
        OpsIssue issue = BeanConvert.toModel(dto);
        LocalDateTime now = LocalDateTime.now();
        if (isNew) {
            // 默认待处理、中等严重度
            if (issue.getStatus() == null) {
                issue.setStatus("open");
            }
            if (issue.getSeverity() == null) {
                issue.setSeverity("medium");
            }
            issue.setCreatedAt(now);
            issue.setUpdatedAt(now);
            return mapper.insert(issue) > 0;
        }
        issue.setUpdatedAt(now);
        return mapper.update(issue) > 0;
    }

    /**
     * 删除运维问题
     *
     * @param id 问题编号
     * @return 是否成功
     */
    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
