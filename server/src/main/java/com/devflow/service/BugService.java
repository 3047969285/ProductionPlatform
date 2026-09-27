package com.devflow.service;

import com.devflow.model.dto.BugDto;
import com.devflow.model.vo.BugVo;

import java.util.List;
import java.util.Map;

/**
 * 缺陷服务
 */
public interface BugService {

    /**
     * 查询全部缺陷
     *
     * @return 缺陷视图列表
     */
    List<BugVo> list();

    /**
     * 按项目查询缺陷
     *
     * @param projectId 项目编号
     * @return 缺陷视图列表
     */
    List<BugVo> listByProject(Long projectId);

    /**
     * 统计缺陷总数
     *
     * @return 缺陷数量
     */
    int count();

    /**
     * 统计各状态缺陷数量
     *
     * @return 状态与数量映射
     */
    Map<String, Integer> stats();

    /**
     * 按严重程度统计缺陷数量
     *
     * @return 严重程度与数量映射
     */
    Map<String, Integer> severityStats();

    /**
     * 新增或更新缺陷
     *
     * @param dto 缺陷信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    boolean save(BugDto dto, boolean isNew);

    /**
     * 更新缺陷状态
     *
     * @param id 缺陷编号
     * @param status 目标状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, String status);

    /**
     * 删除缺陷
     *
     * @param id 缺陷编号
     * @return 是否成功
     */
    boolean delete(Long id);
}
