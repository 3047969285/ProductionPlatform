package com.devflow.service;

import com.devflow.model.dto.TestCaseDto;
import com.devflow.model.vo.TestCaseVo;

import java.util.List;

/**
 * 测试用例服务
 */
public interface TestCaseService {

    /**
     * 按项目查询用例
     *
     * @param projectId 项目编号
     * @return 用例视图列表
     */
    List<TestCaseVo> list(Long projectId);

    /**
     * 查询全部用例
     *
     * @return 用例视图列表
     */
    List<TestCaseVo> listAll();

    /**
     * 统计用例总数
     *
     * @return 用例数量
     */
    int count();

    /**
     * 新增或更新用例
     *
     * @param dto 用例信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    boolean save(TestCaseDto dto, boolean isNew);

    /**
     * 删除用例
     *
     * @param id 用例编号
     * @return 是否成功
     */
    boolean delete(Long id);
}
