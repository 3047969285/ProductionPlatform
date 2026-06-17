package com.devflow.service;

import com.devflow.model.dto.DocFolderDto;
import com.devflow.model.vo.DocFolderVo;

import java.util.List;

/**
 * 文档目录服务
 */
public interface DocFolderService {

    /**
     * 按项目与模块类型查询目录
     *
     * @param projectId 项目编号
     * @param moduleType 模块类型
     * @return 目录视图列表
     */
    List<DocFolderVo> list(Long projectId, String moduleType);

    /**
     * 新增或更新目录
     *
     * @param dto 目录信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    boolean save(DocFolderDto dto, boolean isNew);

    /**
     * 删除目录
     *
     * @param id 目录编号
     * @return 是否成功
     */
    boolean delete(Long id);
}
