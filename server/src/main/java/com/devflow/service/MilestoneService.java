package com.devflow.service;

import com.devflow.model.dto.MilestoneDto;
import com.devflow.model.vo.MilestoneVo;

import java.util.List;

/**
 * 里程碑服务
 */
public interface MilestoneService {

    /**
     * 按项目查询里程碑
     *
     * @param projectId 项目编号
     * @return 里程碑视图列表
     */
    List<MilestoneVo> list(Long projectId);

    /**
     * 统计里程碑总数
     *
     * @return 里程碑数量
     */
    int count();

    /**
     * 新增或更新里程碑
     *
     * @param dto 里程碑信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    boolean save(MilestoneDto dto, boolean isNew);

    /**
     * 删除里程碑
     *
     * @param id 里程碑编号
     * @return 是否成功
     */
    boolean delete(Long id);
}
