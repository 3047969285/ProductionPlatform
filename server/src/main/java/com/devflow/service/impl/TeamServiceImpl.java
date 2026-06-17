package com.devflow.service.impl;

import com.devflow.common.BeanConvert;
import com.devflow.model.dto.DevTeamDto;
import com.devflow.mapper.TeamMapper;
import com.devflow.model.entity.DevTeam;
import com.devflow.service.TeamService;
import com.devflow.model.vo.DevTeamVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 研发团队服务实现
 */
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamMapper mapper;

    /**
     * 查询全部团队
     *
     * @return 团队视图列表
     */
    @Override
    public List<DevTeamVo> list() {
        return BeanConvert.toDevTeamVoList(mapper.findAll());
    }

    /**
     * 统计团队总数
     *
     * @return 团队数量
     */
    @Override
    public int count() {
        return mapper.count();
    }

    /**
     * 统计活跃团队数
     *
     * @return 活跃团队数量
     */
    @Override
    public int countActive() {
        return mapper.countActive();
    }

    /**
     * 新增或更新团队
     *
     * @param dto 团队信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    @Override
    public boolean save(DevTeamDto dto, boolean isNew) {
        DevTeam team = BeanConvert.toModel(dto);
        if (isNew) {
            team.setCreatedAt(LocalDateTime.now());
            // 未指定状态时默认活跃
            if (team.getStatus() == null) {
                team.setStatus("active");
            }
            return mapper.insert(team) > 0;
        }
        return mapper.update(team) > 0;
    }

    /**
     * 删除团队
     *
     * @param id 团队编号
     * @return 是否成功
     */
    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
