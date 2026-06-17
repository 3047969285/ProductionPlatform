package com.devflow.mapper;

import com.devflow.model.entity.DocFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档目录数据访问
 */
@Mapper
public interface DocFolderMapper {

    /**
     * 按项目与模块类型查询目录
     *
     * @param projectId 项目编号
     * @param moduleType 模块类型
     * @return 目录列表
     */
    List<DocFolder> findByProject(@Param("projectId") Long projectId, @Param("moduleType") String moduleType);

    /**
     * 新增目录
     *
     * @param folder 目录实体
     * @return 影响行数
     */
    int insert(DocFolder folder);

    /**
     * 更新目录
     *
     * @param folder 目录实体
     * @return 影响行数
     */
    int update(DocFolder folder);

    /**
     * 删除目录
     *
     * @param id 目录编号
     * @return 影响行数
     */
    int deleteById(Long id);
}
