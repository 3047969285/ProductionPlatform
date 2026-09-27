package com.devflow.mapper;

import com.devflow.model.entity.ActivityLog;
import com.devflow.model.vo.ActivityLogVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 操作历史数据访问
 */
@Mapper
public interface ActivityLogMapper {

    /**
     * 按工作项查询操作历史
     *
     * @param workType 工作类型
     * @param workId 工作项编号
     * @return 历史视图列表
     */
    List<ActivityLogVo> findByWork(String workType, Long workId);

    /**
     * 记录操作历史
     *
     * @param entity 历史实体
     * @return 影响行数
     */
    int insert(ActivityLog entity);
}
