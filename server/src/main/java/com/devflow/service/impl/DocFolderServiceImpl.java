package com.devflow.service.impl;

import com.devflow.common.BeanConvert;
import com.devflow.model.dto.DocFolderDto;
import com.devflow.mapper.DocFolderMapper;
import com.devflow.model.entity.DocFolder;
import com.devflow.service.DocFolderService;
import com.devflow.model.vo.DocFolderVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档目录服务实现
 */
@Service
@RequiredArgsConstructor
public class DocFolderServiceImpl implements DocFolderService {

    private final DocFolderMapper mapper;

    /**
     * 按项目与模块类型查询目录
     *
     * @param projectId 项目编号
     * @param moduleType 模块类型
     * @return 目录视图列表
     */
    @Override
    public List<DocFolderVo> list(Long projectId, String moduleType) {
        // moduleType 区分需求目录和接口目录
        return BeanConvert.toDocFolderVoList(mapper.findByProject(projectId, moduleType));
    }

    /**
     * 新增或更新目录
     *
     * @param dto 目录信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    @Override
    public boolean save(DocFolderDto dto, boolean isNew) {
        DocFolder folder = BeanConvert.toModel(dto);
        if (isNew) {
            folder.setCreatedAt(LocalDateTime.now());
            // 未传排序号时默认排最前
            if (folder.getSortOrder() == null) {
                folder.setSortOrder(0);
            }
            return mapper.insert(folder) > 0;
        }
        return mapper.update(folder) > 0;
    }

    /**
     * 删除目录
     *
     * @param id 目录编号
     * @return 是否成功
     */
    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
