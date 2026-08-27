package com.devflow.model.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 单个项目的研发交付概览。
 */
@Data
public class ProjectOverviewVo {

    private Map<String, Integer> requirements;
    private int apis;
    private Map<String, Integer> tests;
    private Map<String, Integer> ops;
    private List<RequirementVo> recentRequirements;
}
