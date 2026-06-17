package com.devflow.mapper;

import com.devflow.model.entity.ApiDoc;
import com.devflow.model.vo.ApiDocVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 接口文档数据访问
 */
@Mapper
public interface ApiDocMapper {

    /**
     * 查询全部接口文档含关联名称
     *
     * @return 接口文档视图列表
     */
    List<ApiDocVo> findAll();

    /**
     * 按项目查询接口文档
     *
     * @param projectId 项目编号
     * @return 接口文档视图列表
     */
    List<ApiDocVo> findByProject(Long projectId);

    /**
     * 按项目与目录查询接口文档
     *
     * @param projectId 项目编号
     * @param folderId 目录编号
     * @return 接口文档视图列表
     */
    List<ApiDocVo> findByFolder(@Param("projectId") Long projectId, @Param("folderId") Long folderId);

    /**
     * 统计接口文档总数
     *
     * @return 文档数量
     */
    int count();

    /**
     * 新增接口文档
     *
     * @param doc 接口文档实体
     * @return 影响行数
     */
    int insert(ApiDoc doc);

    /**
     * 更新接口文档
     *
     * @param doc 接口文档实体
     * @return 影响行数
     */
    int update(ApiDoc doc);

    /**
     * 删除接口文档
     *
     * @param id 文档编号
     * @return 影响行数
     */
    int deleteById(Long id);
}
