package com.devflow.service;

import com.devflow.model.dto.RequirementDto;
import com.devflow.model.vo.RequirementVo;

import java.util.List;
import java.util.Map;

/**
 * 需求管理服务
 */
public interface RequirementService {

    /**
     * 查询全部需求
     *
     * @return 需求视图列表
     */
    List<RequirementVo> list();

    /**
     * 按项目或目录查询需求
     *
     * @param projectId 项目编号
     * @param folderId 目录编号可为空
     * @return 需求视图列表
     */
    List<RequirementVo> listByProject(Long projectId, Long folderId);

    /**
     * 查询最近更新的需求
     *
     * @param limit 条数上限
     * @return 需求视图列表
     */
    List<RequirementVo> recent(int limit);

    /**
     * 统计各状态需求数量
     *
     * @return 状态与数量映射
     */
    Map<String, Integer> stats();

    /**
     * 新增或更新需求
     *
     * @param dto 需求信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    boolean save(RequirementDto dto, boolean isNew);

    /**
     * 更新需求状态
     *
     * @param id 需求编号
     * @param status 目标状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, String status);

    /**
     * 删除需求
     *
     * @param id 需求编号
     * @return 是否成功
     */
    boolean delete(Long id);
}
