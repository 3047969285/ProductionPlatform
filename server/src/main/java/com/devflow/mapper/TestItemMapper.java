package com.devflow.mapper;

import com.devflow.model.entity.TestItem;
import com.devflow.model.vo.TestItemVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 测试项数据访问
 */
@Mapper
public interface TestItemMapper {

    /**
     * 查询全部测试项含项目名称
     *
     * @return 测试项视图列表
     */
    List<TestItemVo> findAll();

    /**
     * 按项目查询测试项
     *
     * @param projectId 项目编号
     * @return 测试项视图列表
     */
    List<TestItemVo> findByProject(Long projectId);

    /**
     * 统计测试项总数
     *
     * @return 测试项数量
     */
    int count();

    /**
     * 按状态统计测试项数
     *
     * @param status 状态值
     * @return 测试项数量
     */
    int countByStatus(String status);

    /**
     * 新增测试项
     *
     * @param item 测试项实体
     * @return 影响行数
     */
    int insert(TestItem item);

    /**
     * 更新测试项
     *
     * @param item 测试项实体
     * @return 影响行数
     */
    int update(TestItem item);

    /**
     * 删除测试项
     *
     * @param id 测试项编号
     * @return 影响行数
     */
    int deleteById(Long id);
}
