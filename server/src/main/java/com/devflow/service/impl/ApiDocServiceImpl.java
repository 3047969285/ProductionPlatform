package com.devflow.service.impl;

import com.devflow.common.BeanConvert;
import com.devflow.model.dto.ApiDocDto;
import com.devflow.mapper.ApiDocMapper;
import com.devflow.model.entity.ApiDoc;
import com.devflow.service.ApiDocService;
import com.devflow.model.vo.ApiDocVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 接口文档服务实现
 */
@Service
@RequiredArgsConstructor
public class ApiDocServiceImpl implements ApiDocService {

    private final ApiDocMapper mapper;

    /**
     * 查询全部接口文档
     *
     * @return 接口文档视图列表
     */
    @Override
    public List<ApiDocVo> list() {
        return mapper.findAll();
    }

    /**
     * 按项目或目录查询接口文档
     *
     * @param projectId 项目编号
     * @param folderId 目录编号可为空
     * @return 接口文档视图列表
     */
    @Override
    public List<ApiDocVo> listByProject(Long projectId, Long folderId) {
        if (folderId != null) {
            return mapper.findByFolder(projectId, folderId);
        }
        return mapper.findByProject(projectId);
    }

    /**
     * 统计接口文档总数
     *
     * @return 文档数量
     */
    @Override
    public int count() {
        return mapper.count();
    }

    /**
     * 新增或更新接口文档
     *
     * @param dto 接口文档信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    @Override
    public boolean save(ApiDocDto dto, boolean isNew) {
        ApiDoc doc = BeanConvert.toModel(dto);
        LocalDateTime now = LocalDateTime.now();
        if (isNew) {
            // 自动生成接口编号
            if (doc.getApiNo() == null || doc.getApiNo().isBlank()) {
                doc.setApiNo("API-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            }
            // 填充默认状态和方法
            if (doc.getStatus() == null) {
                doc.setStatus("draft");
            }
            if (doc.getMethod() == null) {
                doc.setMethod("GET");
            }
            doc.setCreatedAt(now);
            doc.setUpdatedAt(now);
            return mapper.insert(doc) > 0;
        }
        doc.setUpdatedAt(now);
        return mapper.update(doc) > 0;
    }

    /**
     * 删除接口文档
     *
     * @param id 文档编号
     * @return 是否成功
     */
    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
