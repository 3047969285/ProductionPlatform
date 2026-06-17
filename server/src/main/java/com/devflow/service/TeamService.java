package com.devflow.service;

import com.devflow.model.dto.DevTeamDto;
import com.devflow.model.vo.DevTeamVo;

import java.util.List;

/**
 * 研发团队服务
 */
public interface TeamService {

    /**
     * 查询全部团队
     *
     * @return 团队视图列表
     */
    List<DevTeamVo> list();

    /**
     * 统计团队总数
     *
     * @return 团队数量
     */
    int count();

    /**
     * 统计活跃团队数
     *
     * @return 活跃团队数量
     */
    int countActive();

    /**
     * 新增或更新团队
     *
     * @param dto 团队信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    boolean save(DevTeamDto dto, boolean isNew);

    /**
     * 删除团队
     *
     * @param id 团队编号
     * @return 是否成功
     */
    boolean delete(Long id);
}
