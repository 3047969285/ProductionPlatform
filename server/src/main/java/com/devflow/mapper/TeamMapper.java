package com.devflow.mapper;

import com.devflow.model.entity.DevTeam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 研发团队数据访问
 */
@Mapper
public interface TeamMapper {

    /**
     * 查询全部团队
     *
     * @return 团队列表
     */
    List<DevTeam> findAll();

    /**
     * 统计团队总数
     *
     * @return 团队数量
     */
    int count();

    /**
     * 统计活跃团队数
     *
     * @return 活跃团队数量
     */
    int countActive();

    /**
     * 新增团队
     *
     * @param team 团队实体
     * @return 影响行数
     */
    int insert(DevTeam team);

    /**
     * 更新团队
     *
     * @param team 团队实体
     * @return 影响行数
     */
    int update(DevTeam team);

    /**
     * 删除团队
     *
     * @param id 团队编号
     * @return 影响行数
     */
    int deleteById(Long id);
}
