package com.devflow.model.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardVo {
    private int projects;
    private Map<String, Integer> requirements;
    private int apis;
    private Map<String, Integer> tests;
    private Map<String, Integer> ops;
    private Map<String, Integer> teams;
    private List<RequirementVo> recentRequirements;
}
