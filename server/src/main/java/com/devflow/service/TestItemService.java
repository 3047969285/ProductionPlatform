package com.devflow.service;

import com.devflow.model.dto.TestItemDto;
import com.devflow.model.vo.TestItemVo;

import java.util.List;
import java.util.Map;

/**
 * 测试项服务
 */
public interface TestItemService {

    /**
     * 查询全部测试项
     *
     * @return 测试项视图列表
     */
    List<TestItemVo> list();

    /**
     * 按项目查询测试项
     *
     * @param projectId 项目编号
     * @return 测试项视图列表
     */
    List<TestItemVo> listByProject(Long projectId);

    /**
     * 统计各状态测试项数量
     *
     * @return 状态与数量映射
     */
    Map<String, Integer> stats();

    /**
     * 新增或更新测试项
     *
     * @param dto 测试项信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    boolean save(TestItemDto dto, boolean isNew);

    /**
     * 删除测试项
     *
     * @param id 测试项编号
     * @return 是否成功
     */
    boolean delete(Long id);
}
