package com.devflow.service;

import com.devflow.model.dto.SprintDto;
import com.devflow.model.vo.SprintVo;

import java.util.List;
import java.util.Map;

/**
 * 迭代服务
 */
public interface SprintService {

    /**
     * 按项目查询迭代
     *
     * @param projectId 项目编号
     * @return 迭代视图列表
     */
    List<SprintVo> list(Long projectId);

    /**
     * 根据编号查询迭代
     *
     * @param id 迭代编号
     * @return 迭代视图未找到返回空
     */
    SprintVo getById(Long id);

    /**
     * 新增或更新迭代
     *
     * @param dto 迭代信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    boolean save(SprintDto dto, boolean isNew);

    /**
     * 删除迭代
     *
     * @param id 迭代编号
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 生成燃尽图数据（含任务与缺陷的每日剩余数）
     *
     * @param id 迭代编号
     * @return 燃尽图数据
     */
    Map<String, Object> burndown(Long id);
}
