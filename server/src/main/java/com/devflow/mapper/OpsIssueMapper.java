package com.devflow.mapper;

import com.devflow.model.entity.OpsIssue;
import com.devflow.model.vo.OpsIssueVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 运维问题数据访问
 */
@Mapper
public interface OpsIssueMapper {

    /**
     * 查询全部运维问题含项目名称
     *
     * @return 运维问题视图列表
     */
    List<OpsIssueVo> findAll();

    /**
     * 按项目查询运维问题
     *
     * @param projectId 项目编号
     * @return 运维问题视图列表
     */
    List<OpsIssueVo> findByProject(Long projectId);

    /**
     * 统计运维问题总数
     *
     * @return 问题数量
     */
    int count();

    /**
     * 按状态统计运维问题数
     *
     * @param status 状态值
     * @return 问题数量
     */
    int countByStatus(String status);

    /**
     * 新增运维问题
     *
     * @param issue 运维问题实体
     * @return 影响行数
     */
    int insert(OpsIssue issue);

    /**
     * 更新运维问题
     *
     * @param issue 运维问题实体
     * @return 影响行数
     */
    int update(OpsIssue issue);

    /**
     * 删除运维问题
     *
     * @param id 问题编号
     * @return 影响行数
     */
    int deleteById(Long id);
}
