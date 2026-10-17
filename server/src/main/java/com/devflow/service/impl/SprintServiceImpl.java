package com.devflow.service.impl;

import com.devflow.model.dto.SprintDto;
import com.devflow.mapper.SprintMapper;
import com.devflow.model.entity.Sprint;
import com.devflow.service.SprintService;
import com.devflow.model.vo.SprintVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 迭代服务实现
 */
@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintMapper mapper;

    /**
     * 按项目查询迭代
     *
     * @param projectId 项目编号
     * @return 迭代视图列表
     */
    @Override
    public List<SprintVo> list(Long projectId) {
        List<SprintVo> list = mapper.findByProject(projectId);
        // 填充统计字段
        for (SprintVo vo : list) {
            fillStats(vo);
        }
        return list;
    }

    /**
     * 根据编号查询迭代
     *
     * @param id 迭代编号
     * @return 迭代视图未找到返回空
     */
    @Override
    public SprintVo getById(Long id) {
        SprintVo vo = mapper.findById(id);
        if (vo != null) {
            fillStats(vo);
        }
        return vo;
    }

    /**
     * 填充迭代统计
     *
     * @param vo 迭代视图
     */
    private void fillStats(SprintVo vo) {
        int taskTotal = 0, taskDone = 0, bugTotal = 0, bugClosed = 0;
        for (Map<String, Object> row : mapper.countTasksByStatus(vo.getId())) {
            int cnt = ((Number) row.get("cnt")).intValue();
            taskTotal += cnt;
            if ("done".equals(row.get("status"))) {
                taskDone = cnt;
            }
        }
        for (Map<String, Object> row : mapper.countBugsByStatus(vo.getId())) {
            int cnt = ((Number) row.get("cnt")).intValue();
            bugTotal += cnt;
            String status = (String) row.get("status");
            if ("resolved".equals(status) || "closed".equals(status)) {
                bugClosed += cnt;
            }
        }
        vo.setTaskTotal(taskTotal);
        vo.setTaskDone(taskDone);
        vo.setBugTotal(bugTotal);
        vo.setBugClosed(bugClosed);
        vo.setWorkTotal(taskTotal + bugTotal);
        vo.setWorkDone(taskDone + bugClosed);
        vo.setProgress(vo.getWorkTotal() == 0 ? 0 : (int) Math.round(vo.getWorkDone() * 100.0 / vo.getWorkTotal()));
    }

    /**
     * 新增或更新迭代
     *
     * @param dto 迭代信息
     * @param isNew 是否新增
     * @return 是否成功
     */
    @Override
    public boolean save(SprintDto dto, boolean isNew) {
        if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        Sprint s = new Sprint();
        s.setId(dto.getId());
        s.setProjectId(dto.getProjectId());
        s.setName(dto.getName());
        s.setGoal(dto.getGoal());
        s.setStartDate(dto.getStartDate());
        s.setEndDate(dto.getEndDate());
        s.setStatus(dto.getStatus() == null ? "planning" : dto.getStatus());
        if (isNew) {
            s.setCreatedAt(LocalDateTime.now());
            return mapper.insert(s) > 0;
        }
        return mapper.update(s) > 0;
    }

    /**
     * 删除迭代（先解除任务/缺陷对迭代的引用）
     *
     * @param id 迭代编号
     * @return 是否成功
     */
    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        mapper.unlinkTasks(id);
        mapper.unlinkBugs(id);
        return mapper.deleteById(id) > 0;
    }

    /**
     * 生成燃尽图数据（含任务与缺陷的每日剩余数）
     *
     * @param id 迭代编号
     * @return 燃尽图数据
     */
    @Override
    public Map<String, Object> burndown(Long id) {
        SprintVo vo = mapper.findById(id);
        if (vo != null) {
            fillStats(vo); // 填充统计字段，避免 workTotal 为 null
        }
        Map<String, Object> result = new HashMap<>();
        if (vo == null || vo.getStartDate() == null || vo.getEndDate() == null) {
            result.put("labels", new ArrayList<String>());
            result.put("ideal", new ArrayList<Integer>());
            result.put("actual", new ArrayList<Integer>());
            return result;
        }

        long days = ChronoUnit.DAYS.between(vo.getStartDate(), vo.getEndDate()) + 1;
        List<String> labels = new ArrayList<>();
        List<Integer> ideal = new ArrayList<>();
        List<Integer> actual = new ArrayList<>(); // 允许 null：今天之后没有实际值

        int total = vo.getWorkTotal();
        int done = vo.getWorkDone();

        // 实际线：每天剩余数 = 总数 - 截至当天已完成的工作项数，按每个任务/缺陷真实的完成日期统计
        List<LocalDate> doneDates = new ArrayList<>(mapper.doneTaskDates(id));
        doneDates.addAll(mapper.doneBugDates(id));
        LocalDate today = LocalDate.now();

        for (long i = 0; i < days; i++) {
            LocalDate day = vo.getStartDate().plusDays(i);
            labels.add(day.toString());
            // 理想线：从 total 线性降到 0
            double ratio = days <= 1 ? 1.0 : (double) i / (days - 1);
            ideal.add((int) Math.round(total * (1.0 - ratio)));
            // 未来的日期没有实际数据，用 null 让前端在今天处截断折线
            if (day.isAfter(today)) {
                actual.add(null);
                continue;
            }
            long doneByDay = doneDates.stream().filter(d -> d != null && !d.isAfter(day)).count();
            actual.add((int) (total - doneByDay));
        }
        result.put("labels", labels);
        result.put("ideal", ideal);
        result.put("actual", actual);
        result.put("total", total);
        result.put("done", done);
        result.put("progress", vo.getProgress());
        return result;
    }
}