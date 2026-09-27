package com.devflow.mapper;

import com.devflow.model.entity.ReleaseRecord;
import com.devflow.model.vo.ReleaseRecordVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 发布记录数据访问
 */
@Mapper
public interface ReleaseRecordMapper {

    /**
     * 按项目查询发布记录
     *
     * @param projectId 项目编号
     * @return 发布记录视图列表
     */
    List<ReleaseRecordVo> findByProject(Long projectId);

    /**
     * 新增发布记录
     *
     * @param entity 发布记录实体
     * @return 影响行数
     */
    int insert(ReleaseRecord entity);

    /**
     * 更新发布记录
     *
     * @param entity 发布记录实体
     * @return 影响行数
     */
    int update(ReleaseRecord entity);

    /**
     * 删除发布记录
     *
     * @param id 发布记录编号
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 统计发布记录总数
     *
     * @return 发布记录数量
     */
    int count();

    /**
     * 删除项目全部发布记录（项目删除级联）
     *
     * @param projectId 项目编号
     * @return 影响行数
     */
    int deleteByProject(Long projectId);
}