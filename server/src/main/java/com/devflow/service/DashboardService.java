package com.devflow.service;

import com.devflow.model.vo.DashboardVo;

/**
 * 仪表盘概览服务
 */
public interface DashboardService {

    /**
     * 获取研发概览数据
     *
     * @return 项目需求接口测试运维团队等汇总视图
     */
    DashboardVo overview();
}
