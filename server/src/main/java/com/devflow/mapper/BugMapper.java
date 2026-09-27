package com.devflow.mapper;

import com.devflow.model.vo.BugVo;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 缺陷数据访问
 */
@Mapper
public interface BugMapper {

    /**
     * 查询全部缺陷
     *
     * @return 缺陷视图列表
     */
    List<BugVo> findAll();

    /**
     * 按项目查询缺陷
     *
     * @param projectId 项目编号
     * @return 缺陷视图列表
     */
    List<BugVo> findByProject(Long projectId);

    /**
     * 统计缺陷总数
     *
     * @return 缺陷数量
     */
    int count();

    /**
     * 按状态统计缺陷数
     *
     * @param status 状态
     * @return 缺陷数量
     */
    int countByStatus(String status);

    /**
     * 按严重程度统计缺陷数
     *
     * @param severity 严重程度
     * @return 缺陷数量
     */
    int countBySeverity(String severity);

    /**
     * 新增缺陷
     *
     * @param entity 缺陷实体
     * @return 影响行数
     */
    int insert(com.devflow.model.entity.Bug entity);

    /**
     * 更新缺陷
     *
     * @param entity 缺陷实体
     * @return 影响行数
     */
    int update(com.devflow.model.entity.Bug entity);

    /**
     * 更新缺陷状态
     *
     * @param id 缺陷编号
     * @param status 目标状态
     * @param resolvedAt 解决时间可为空
     * @return 影响行数
     */
    int updateStatus(Long id, String status, LocalDateTime resolvedAt);

    /**
     * 删除缺陷
     *
     * @param id 缺陷编号
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 删除项目全部缺陷（项目删除级联）
     *
     * @param projectId 项目编号
     * @return 影响行数
     */
    int deleteByProject(Long projectId);
}
