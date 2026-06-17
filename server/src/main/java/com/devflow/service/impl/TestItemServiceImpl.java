package com.devflow.service.impl;

import com.devflow.common.BeanConvert;
import com.devflow.model.dto.TestItemDto;
import com.devflow.mapper.TestItemMapper;
import com.devflow.model.entity.TestItem;
import com.devflow.service.TestItemService;
import com.devflow.model.vo.TestItemVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试项服务实现
 */
@Service
@RequiredArgsConstructor
public class TestItemServiceImpl implements TestItemService {

    private final TestItemMapper mapper;

    /**
     * 查询全部测试项
     *
     * @return 测试项视图列表
     */
    @Override
    public List<TestItemVo> list() {
        return mapper.findAll();
    }

    /**
     * 按项目查询测试项
     *
     * @param projectId 项目编号
     * @return 测试项视图列表
     */
    @Override
    public List<TestItemVo> listByProject(Long projectId) {
        return mapper.findByProject(projectId);
    }

    /**
     * 统计各状态测试项数量
     *
     * @return 状态与数量映射
     */
    @Override
    public Map<String, Integer> stats() {
        Map<String, Integer> s = new LinkedHashMap<>();
        s.put("total", mapper.count());
        s.put("pending", mapper.countByStatus("pending"));
        s.put("running", mapper.countByStatus("running"));
        s.put("done", mapper.countByStatus("done"));
        return s;
    }

    /**
     * 新增或更新测试项
     *
     * @param dto 测试项信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    @Override
    public boolean save(TestItemDto dto, boolean isNew) {
        TestItem item = BeanConvert.toModel(dto);
        LocalDateTime now = LocalDateTime.now();
        if (isNew) {
            // 默认待执行、进度 0
            if (item.getStatus() == null) {
                item.setStatus("pending");
            }
            if (item.getProgress() == null) {
                item.setProgress(0);
            }
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            return mapper.insert(item) > 0;
        }
        item.setUpdatedAt(now);
        return mapper.update(item) > 0;
    }

    /**
     * 删除测试项
     *
     * @param id 测试项编号
     * @return 是否成功
     */
    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
