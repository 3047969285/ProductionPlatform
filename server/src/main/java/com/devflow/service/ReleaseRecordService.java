package com.devflow.service;

import com.devflow.model.dto.ReleaseRecordDto;
import com.devflow.model.vo.ReleaseRecordVo;

import java.util.List;

/**
 * 发布记录服务
 */
public interface ReleaseRecordService {

    /**
     * 按项目查询发布记录
     *
     * @param projectId 项目编号
     * @return 发布记录视图列表
     */
    List<ReleaseRecordVo> list(Long projectId);

    /**
     * 统计发布记录总数
     *
     * @return 发布记录数量
     */
    int count();

    /**
     * 新增或更新发布记录
     *
     * @param dto 发布记录信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    boolean save(ReleaseRecordDto dto, boolean isNew);

    /**
     * 删除发布记录
     *
     * @param id 发布记录编号
     * @return 是否成功
     */
    boolean delete(Long id);
}
