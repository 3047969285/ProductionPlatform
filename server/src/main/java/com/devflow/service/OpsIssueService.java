package com.devflow.service;

import com.devflow.model.dto.OpsIssueDto;
import com.devflow.model.vo.OpsIssueVo;

import java.util.List;
import java.util.Map;

/**
 * 运维问题服务
 */
public interface OpsIssueService {

    /**
     * 查询全部运维问题
     *
     * @return 运维问题视图列表
     */
    List<OpsIssueVo> list();

    /**
     * 按项目查询运维问题
     *
     * @param projectId 项目编号
     * @return 运维问题视图列表
     */
    List<OpsIssueVo> listByProject(Long projectId);

    /**
     * 统计各状态运维问题数量
     *
     * @return 状态与数量映射
     */
    Map<String, Integer> stats();

    /**
     * 新增或更新运维问题
     *
     * @param dto 运维问题信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    boolean save(OpsIssueDto dto, boolean isNew);

    /**
     * 删除运维问题
     *
     * @param id 问题编号
     * @return 是否成功
     */
    boolean delete(Long id);
}
