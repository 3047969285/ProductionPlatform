package com.devflow.service.impl;

import com.devflow.model.dto.TaskDto;
import com.devflow.mapper.TaskMapper;
import com.devflow.model.entity.Task;
import com.devflow.service.TaskService;
import com.devflow.model.vo.TaskVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务服务实现
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper mapper;

    /**
     * 查询全部任务
     *
     * @return 任务视图列表
     */
    @Override
    public List<TaskVo> list() {
        return mapper.findAll();
    }

    /**
     * 按项目查询任务
     *
     * @param projectId 项目编号
     * @return 任务视图列表
     */
    @Override
    public List<TaskVo> listByProject(Long projectId) {
        return mapper.findByProject(projectId);
    }

    /**
     * 按迭代查询任务
     *
     * @param sprintId 迭代编号
     * @return 任务视图列表
     */
    @Override
    public List<TaskVo> listBySprint(Long sprintId) {
        return mapper.findBySprint(sprintId);
    }

    /**
     * 统计任务总数
     *
     * @return 任务数量
     */
    @Override
    public int count() {
        return mapper.count();
    }

    /**
     * 统计各状态任务数量
     *
     * @return 状态与数量映射
     */
    @Override
    public Map<String, Integer> stats() {
        Map<String, Integer> s = new LinkedHashMap<>();
        s.put("total", mapper.count());
        s.put("todo", mapper.countByStatus("todo"));
        s.put("doing", mapper.countByStatus("doing"));
        s.put("done", mapper.countByStatus("done"));
        return s;
    }

    /**
     * 新增或更新任务
     *
     * @param dto 任务信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    @Override
    public boolean save(TaskDto dto, boolean isNew) {
        Task t = new Task();
        t.setId(dto.getId());
        t.setProjectId(dto.getProjectId());
        t.setSprintId(dto.getSprintId());
        t.setTitle(dto.getTitle());
        t.setContent(dto.getContent());
        t.setPriority(dto.getPriority() == null ? "medium" : dto.getPriority());
        t.setStatus(dto.getStatus() == null ? "todo" : dto.getStatus());
        t.setAssignee(dto.getAssignee());
        t.setCreator(dto.getCreator());
        t.setEstimateHours(dto.getEstimateHours());
        LocalDateTime now = LocalDateTime.now();
        if (isNew) {
            t.setCreatedAt(now);
            t.setUpdatedAt(now);
            // 新建即完成则记录完成时间
            if ("done".equals(t.getStatus())) {
                t.setResolvedAt(now);
            }
            return mapper.insert(t) > 0;
        }
        t.setUpdatedAt(now);
        // 更新时根据状态刷新完成时间
        t.setResolvedAt("done".equals(t.getStatus()) ? now : null);
        return mapper.update(t) > 0;
    }

    /**
     * 更新任务状态
     *
     * @param id 任务编号
     * @param status 目标状态
     * @return 是否成功
     */
    @Override
    public boolean updateStatus(Long id, String status) {
        LocalDateTime resolvedAt = "done".equals(status) ? LocalDateTime.now() : null;
        return mapper.updateStatus(id, status, resolvedAt) > 0;
    }

    /**
     * 删除任务
     *
     * @param id 任务编号
     * @return 是否成功
     */
    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}