package com.devflow.mapper;

import com.devflow.model.entity.TestCase;
import com.devflow.model.vo.TestCaseVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 测试用例数据访问
 */
@Mapper
public interface TestCaseMapper {

    /**
     * 按项目查询用例
     *
     * @param projectId 项目编号
     * @return 用例视图列表
     */
    List<TestCaseVo> findByProject(Long projectId);

    /**
     * 查询全部用例
     *
     * @return 用例视图列表
     */
    List<TestCaseVo> findAll();

    /**
     * 新增用例
     *
     * @param entity 用例实体
     * @return 影响行数
     */
    int insert(TestCase entity);

    /**
     * 更新用例
     *
     * @param entity 用例实体
     * @return 影响行数
     */
    int update(TestCase entity);

    /**
     * 删除用例
     *
     * @param id 用例编号
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 统计用例总数
     *
     * @return 用例数量
     */
    int count();

    /**
     * 删除项目全部用例（项目删除级联）
     *
     * @param projectId 项目编号
     * @return 影响行数
     */
    int deleteByProject(Long projectId);
}