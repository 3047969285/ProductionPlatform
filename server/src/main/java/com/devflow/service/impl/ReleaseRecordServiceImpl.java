package com.devflow.service.impl;

import com.devflow.model.dto.ReleaseRecordDto;
import com.devflow.mapper.ReleaseRecordMapper;
import com.devflow.model.entity.ReleaseRecord;
import com.devflow.service.ReleaseRecordService;
import com.devflow.model.vo.ReleaseRecordVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 发布记录服务实现
 */
@Service
@RequiredArgsConstructor
public class ReleaseRecordServiceImpl implements ReleaseRecordService {

    private final ReleaseRecordMapper mapper;

    /**
     * 按项目查询发布记录
     *
     * @param projectId 项目编号
     * @return 发布记录视图列表
     */
    @Override
    public List<ReleaseRecordVo> list(Long projectId) {
        return mapper.findByProject(projectId);
    }

    /**
     * 统计发布记录总数
     *
     * @return 发布记录数量
     */
    @Override
    public int count() {
        return mapper.count();
    }

    /**
     * 新增或更新发布记录
     *
     * @param dto 发布记录信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    @Override
    public boolean save(ReleaseRecordDto dto, boolean isNew) {
        ReleaseRecord r = new ReleaseRecord();
        r.setId(dto.getId());
        r.setProjectId(dto.getProjectId());
        r.setVersion(dto.getVersion());
        r.setEnvironment(dto.getEnvironment() == null ? "prod" : dto.getEnvironment());
        r.setDescription(dto.getDescription());
        r.setStatus(dto.getStatus() == null ? "planned" : dto.getStatus());
        r.setOperator(dto.getOperator());
        LocalDateTime now = LocalDateTime.now();
        if (isNew) {
            r.setCreatedAt(now);
            // 新增即完成则记录发布时间
            if ("done".equals(r.getStatus())) {
                r.setReleasedAt(now);
            }
            return mapper.insert(r) > 0;
        }
        // 状态流转为 done 时刷新发布时间，否则保持原值
        r.setReleasedAt("done".equals(r.getStatus()) ? now : null);
        return mapper.update(r) > 0;
    }

    /**
     * 删除发布记录
     *
     * @param id 发布记录编号
     * @return 是否成功
     */
    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
