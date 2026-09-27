package com.devflow.service.impl;

import com.devflow.model.dto.TestCaseDto;
import com.devflow.mapper.TestCaseMapper;
import com.devflow.model.entity.TestCase;
import com.devflow.service.TestCaseService;
import com.devflow.model.vo.TestCaseVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 测试用例服务实现
 */
@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseMapper mapper;

    /**
     * 按项目查询用例
     *
     * @param projectId 项目编号
     * @return 用例视图列表
     */
    @Override
    public List<TestCaseVo> list(Long projectId) {
        return mapper.findByProject(projectId);
    }

    /**
     * 查询全部用例
     *
     * @return 用例视图列表
     */
    @Override
    public List<TestCaseVo> listAll() {
        return mapper.findAll();
    }

    /**
     * 统计用例总数
     *
     * @return 用例数量
     */
    @Override
    public int count() {
        return mapper.count();
    }

    /**
     * 新增或更新用例
     *
     * @param dto 用例信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    @Override
    public boolean save(TestCaseDto dto, boolean isNew) {
        TestCase c = new TestCase();
        c.setId(dto.getId());
        c.setProjectId(dto.getProjectId());
        c.setTitle(dto.getTitle());
        c.setPreconditions(dto.getPreconditions());
        c.setSteps(dto.getSteps());
        c.setExpectedResult(dto.getExpectedResult());
        c.setPriority(dto.getPriority() == null ? "medium" : dto.getPriority());
        c.setStatus(dto.getStatus() == null ? "active" : dto.getStatus());
        c.setOwner(dto.getOwner());
        LocalDateTime now = LocalDateTime.now();
        if (isNew) {
            c.setCreatedAt(now);
            c.setUpdatedAt(now);
            return mapper.insert(c) > 0;
        }
        c.setUpdatedAt(now);
        return mapper.update(c) > 0;
    }

    /**
     * 删除用例
     *
     * @param id 用例编号
     * @return 是否成功
     */
    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
