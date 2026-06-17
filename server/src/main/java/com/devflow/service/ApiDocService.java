package com.devflow.service;

import com.devflow.model.dto.ApiDocDto;
import com.devflow.model.vo.ApiDocVo;

import java.util.List;

/**
 * 接口文档服务
 */
public interface ApiDocService {

    /**
     * 查询全部接口文档
     *
     * @return 接口文档视图列表
     */
    List<ApiDocVo> list();

    /**
     * 按项目或目录查询接口文档
     *
     * @param projectId 项目编号
     * @param folderId 目录编号可为空
     * @return 接口文档视图列表
     */
    List<ApiDocVo> listByProject(Long projectId, Long folderId);

    /**
     * 统计接口文档总数
     *
     * @return 文档数量
     */
    int count();

    /**
     * 新增或更新接口文档
     *
     * @param dto 接口文档信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    boolean save(ApiDocDto dto, boolean isNew);

    /**
     * 删除接口文档
     *
     * @param id 文档编号
     * @return 是否成功
     */
    boolean delete(Long id);
}
