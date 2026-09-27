package com.devflow.service.impl;

import com.devflow.common.BeanConvert;
import com.devflow.mapper.ApiDocMapper;
import com.devflow.mapper.DocFolderMapper;
import com.devflow.mapper.RequirementMapper;
import com.devflow.model.dto.DocFolderDto;
import com.devflow.model.entity.DocFolder;
import com.devflow.model.vo.DocFolderVo;
import com.devflow.service.DocFolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 文档目录服务实现
 */
@Service
@RequiredArgsConstructor
public class DocFolderServiceImpl implements DocFolderService {

    private final DocFolderMapper mapper;
    private final RequirementMapper requirementMapper;
    private final ApiDocMapper apiDocMapper;

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
     * 删除目录并级联处理子目录与文档
     *
     * @param id 目录编号
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        DocFolder folder = mapper.findById(id);
        if (folder == null) {
            return false;
        }
        // 收集该目录及全部后代目录
        List<DocFolder> all = mapper.findByProject(folder.getProjectId(), folder.getModuleType());
        Set<Long> ids = new HashSet<>();
        collectSubtree(folder.getId(), all, ids);
        if (ids.isEmpty()) {
            return false;
        }
        // 先解绑目录下所有需求与接口文档
        List<Long> idList = new ArrayList<>(ids);
        requirementMapper.clearFolderId(idList);
        apiDocMapper.clearFolderId(idList);
        return mapper.deleteByIds(idList) > 0;
    }

    /**
     * 收集目录子树编号
     *
     * @param parentId 父目录编号
     * @param all 全部目录
     * @param ids 收集结果
     */
    private void collectSubtree(Long parentId, List<DocFolder> all, Set<Long> ids) {
        ids.add(parentId);
        for (DocFolder folder : all) {
            if (parentId.equals(folder.getParentId()) && !ids.contains(folder.getId())) {
                collectSubtree(folder.getId(), all, ids);
            }
        }
    }
}
