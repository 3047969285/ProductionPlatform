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
     * 根据编号查询目录
     *
     * @param id 目录编号
     * @return 目录实体未找到返回空
     */
    DocFolder findById(Long id);

    /**
     * 查询子目录列表
     *
     * @param parentId 父目录编号
     * @return 子目录列表
     */
    List<DocFolder> findByParentId(Long parentId);

    /**
     * 批量删除目录
     *
     * @param ids 目录编号列表
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") List<Long> ids);

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

    /**
     * 按项目删除全部目录
     *
     * @param projectId 项目编号
     * @return 影响行数
     */
    int deleteByProjectId(Long projectId);

    /**
     * 删除项目全部目录
     *
     * @param projectId 项目编号
     * @return 影响行数
     */
    int deleteByProject(Long projectId);
}
