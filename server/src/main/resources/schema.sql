-- DevFlow 初始化
CREATE DATABASE IF NOT EXISTS devflow DEFAULT CHARSET utf8mb4;
USE devflow;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS qa_record;
DROP TABLE IF EXISTS delivery_task;
DROP TABLE IF EXISTS requirement;
DROP TABLE IF EXISTS api_doc;
DROP TABLE IF EXISTS test_item;
DROP TABLE IF EXISTS ops_issue;
DROP TABLE IF EXISTS doc_folder;
DROP TABLE IF EXISTS test_plan_case;
DROP TABLE IF EXISTS test_plan;
DROP TABLE IF EXISTS test_case;
DROP TABLE IF EXISTS work_comment;
DROP TABLE IF EXISTS activity_log;
DROP TABLE IF EXISTS release_record;
DROP TABLE IF EXISTS milestone;
DROP TABLE IF EXISTS bug;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS sprint;
DROP TABLE IF EXISTS project_member;
DROP TABLE IF EXISTS product;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'developer',
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    tech_stack VARCHAR(200),
    delivery_type VARCHAR(20) DEFAULT 'SaaS',
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS dev_team (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    capacity INT DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS doc_folder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    module_type VARCHAR(20) NOT NULL,
    parent_id BIGINT,
    name VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id)
);

CREATE TABLE IF NOT EXISTS requirement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    req_no VARCHAR(32) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT,
    project_id BIGINT NOT NULL,
    folder_id BIGINT,
    priority VARCHAR(20) NOT NULL DEFAULT 'medium',
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    proposer VARCHAR(50),
    owner VARCHAR(50),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id),
    FOREIGN KEY (folder_id) REFERENCES doc_folder(id)
);

CREATE TABLE IF NOT EXISTS api_doc (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    api_no VARCHAR(32) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT,
    method VARCHAR(10) DEFAULT 'GET',
    path VARCHAR(200),
    project_id BIGINT NOT NULL,
    folder_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    proposer VARCHAR(50),
    owner VARCHAR(50),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id),
    FOREIGN KEY (folder_id) REFERENCES doc_folder(id)
);

CREATE TABLE IF NOT EXISTS test_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    progress INT NOT NULL DEFAULT 0,
    owner VARCHAR(50),
    proposer VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id)
);

CREATE TABLE IF NOT EXISTS ops_issue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT,
    severity VARCHAR(20) NOT NULL DEFAULT 'medium',
    status VARCHAR(20) NOT NULL DEFAULT 'open',
    owner VARCHAR(50),
    reporter VARCHAR(50),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id)
);

INSERT INTO user (id, username, password, nickname, role, created_at) VALUES
(1, 'admin', '$2b$10$fxRsAFMopRsaL/PDCS.Hxuzf38bepCNeI28ASPZ1ZvVtCYbxpTZZm', '技术总监', 'admin', NOW()),
(2, 'dev', '$2b$10$fxRsAFMopRsaL/PDCS.Hxuzf38bepCNeI28ASPZ1ZvVtCYbxpTZZm', '开发工程师', 'developer', NOW())
ON DUPLICATE KEY UPDATE password = VALUES(password), nickname = VALUES(nickname), role = VALUES(role);

INSERT INTO project (id, code, name, description, tech_stack, delivery_type, created_at) VALUES
(1, 'SW-CRM', '客户关系系统', '面向销售团队的 CRM 平台', 'Vue3 + Spring Boot', 'SaaS', NOW()),
(2, 'SW-ERP', '企业资源平台', '企业内部 ERP 系统', 'React + Node.js', '定制', NOW()),
(3, 'SW-MOB', '移动办公 App', '移动端协同办公', 'Flutter + API', 'App', NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO dev_team (id, code, name, capacity, status, created_at) VALUES
(1, 'FE', '前端组', 5, 'active', NOW()),
(2, 'BE', '后端组', 6, 'active', NOW()),
(3, 'QA', '测试组', 4, 'active', NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO doc_folder (id, project_id, module_type, parent_id, name, sort_order, created_at) VALUES
(1, 1, 'requirement', NULL, '功能需求', 1, NOW()),
(2, 1, 'requirement', 1, '客户管理', 1, NOW()),
(3, 1, 'api', NULL, '接口文档', 1, NOW()),
(4, 1, 'api', 3, '客户模块', 1, NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO requirement (id, req_no, title, content, project_id, folder_id, priority, status, proposer, owner, created_at, updated_at) VALUES
(1, 'REQ-001', '客户列表批量导出', '<p>支持按筛选条件导出 Excel，字段可配置。</p>', 1, 2, 'high', 'developing', '产品经理', '张三', NOW(), NOW()),
(2, 'REQ-002', '客户详情页优化', '<p>详情页增加跟进记录时间线展示。</p>', 1, 2, 'medium', 'review', '产品经理', '李四', NOW(), NOW())
ON DUPLICATE KEY UPDATE title = VALUES(title), content = VALUES(content);

INSERT INTO api_doc (id, api_no, title, content, method, path, project_id, folder_id, status, proposer, owner, created_at, updated_at) VALUES
(1, 'API-001', '获取客户列表', '<p>分页查询客户，支持关键词搜索。</p>', 'GET', '/api/customers', 1, 4, 'published', '后端负责人', '王五', NOW(), NOW()),
(2, 'API-002', '创建客户', '<p>新增客户信息，校验手机号唯一。</p>', 'POST', '/api/customers', 1, 4, 'draft', '后端负责人', '王五', NOW(), NOW())
ON DUPLICATE KEY UPDATE title = VALUES(title), content = VALUES(content);

INSERT INTO test_item (id, project_id, title, description, progress, owner, proposer, status, created_at, updated_at) VALUES
(1, 1, '客户列表功能测试', '覆盖导出、筛选、分页', 75, '测试工程师', '产品经理', 'running', NOW(), NOW()),
(2, 1, '客户详情回归', '详情页与跟进记录', 30, '测试工程师', '产品经理', 'pending', NOW(), NOW())
ON DUPLICATE KEY UPDATE title = VALUES(title), progress = VALUES(progress);

INSERT INTO ops_issue (id, project_id, title, content, severity, status, owner, reporter, created_at, updated_at) VALUES
(1, 1, '导出大文件超时', '<p>超过 1 万条数据导出接口 504，需优化或异步任务。</p>', 'high', 'open', '运维工程师', '测试工程师', NOW(), NOW()),
(2, 1, '生产环境日志磁盘告警', '<p>/var/log 使用率超过 85%。</p>', 'medium', 'processing', '运维工程师', '监控平台', NOW(), NOW())
ON DUPLICATE KEY UPDATE title = VALUES(title), content = VALUES(content);

-- ==================== 迭代 ====================
CREATE TABLE IF NOT EXISTS sprint (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    goal VARCHAR(500),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'planning',
    created_at DATETIME NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id)
);

-- ==================== 任务 ====================
CREATE TABLE IF NOT EXISTS task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    sprint_id BIGINT,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'medium',
    status VARCHAR(20) NOT NULL DEFAULT 'todo',
    assignee VARCHAR(50),
    creator VARCHAR(50),
    estimate_hours INT DEFAULT 0,
    resolved_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id),
    FOREIGN KEY (sprint_id) REFERENCES sprint(id)
);

-- ==================== 缺陷 ====================
CREATE TABLE IF NOT EXISTS bug (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    sprint_id BIGINT,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT,
    severity VARCHAR(20) NOT NULL DEFAULT 'medium',
    priority VARCHAR(20) NOT NULL DEFAULT 'medium',
    status VARCHAR(20) NOT NULL DEFAULT 'open',
    steps TEXT,
    expected_result TEXT,
    actual_result TEXT,
    assignee VARCHAR(50),
    reporter VARCHAR(50),
    fix_version VARCHAR(50),
    resolved_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id),
    FOREIGN KEY (sprint_id) REFERENCES sprint(id)
);

-- ==================== 项目成员 ====================
CREATE TABLE IF NOT EXISTS project_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'developer',
    joined_at DATETIME NOT NULL,
    UNIQUE KEY uk_project_user (project_id, user_id),
    FOREIGN KEY (project_id) REFERENCES project(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- ==================== 工作项评论 ====================
CREATE TABLE IF NOT EXISTS work_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_type VARCHAR(20) NOT NULL,
    work_id BIGINT NOT NULL,
    user_id BIGINT,
    user_name VARCHAR(50),
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    KEY idx_work (work_type, work_id)
);

-- ==================== 操作历史 ====================
CREATE TABLE IF NOT EXISTS activity_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_type VARCHAR(20) NOT NULL,
    work_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    operator VARCHAR(50),
    detail VARCHAR(500),
    created_at DATETIME NOT NULL,
    KEY idx_work (work_type, work_id)
);

-- ==================== 测试用例 ====================
CREATE TABLE IF NOT EXISTS test_case (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    preconditions TEXT,
    steps TEXT,
    expected_result TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'medium',
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    owner VARCHAR(50),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id)
);

-- ==================== 测试计划 ====================
CREATE TABLE IF NOT EXISTS test_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    owner VARCHAR(50),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id)
);

-- ==================== 计划-用例关联 ====================
CREATE TABLE IF NOT EXISTS test_plan_case (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    case_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    actual_result TEXT,
    executor VARCHAR(50),
    executed_at DATETIME,
    UNIQUE KEY uk_plan_case (plan_id, case_id),
    FOREIGN KEY (plan_id) REFERENCES test_plan(id),
    FOREIGN KEY (case_id) REFERENCES test_case(id)
);

-- ==================== 里程碑 ====================
CREATE TABLE IF NOT EXISTS milestone (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    due_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at DATETIME NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id)
);

-- ==================== 发布记录 ====================
CREATE TABLE IF NOT EXISTS release_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    version VARCHAR(50) NOT NULL,
    environment VARCHAR(20) NOT NULL DEFAULT 'prod',
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'planned',
    operator VARCHAR(50),
    released_at DATETIME,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (project_id) REFERENCES project(id)
);

-- ==================== 种子数据：示例迭代/任务/缺陷/用例/计划/里程碑/发布 ====================
INSERT INTO sprint (id, project_id, name, goal, start_date, end_date, status, created_at) VALUES
(1, 1, 'Sprint 1 - CRM 一期', '完成客户模块与导出功能', CURDATE() - INTERVAL 7 DAY, CURDATE() + INTERVAL 7 DAY, 'active', NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO task (id, project_id, sprint_id, title, content, priority, status, assignee, creator, estimate_hours, resolved_at, created_at, updated_at) VALUES
(1, 1, 1, '客户列表接口开发', '<p>分页 + 关键词搜索。</p>', 'high', 'done', '张三', '后端负责人', 16, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 2 DAY),
(2, 1, 1, '批量导出 Excel', '<p>异步任务 + 进度提示。</p>', 'medium', 'doing', '李四', '后端负责人', 12, NULL, NOW() - INTERVAL 4 DAY, NOW()),
(3, 1, 1, '导出功能联调', '<p>前后端联调并回归。</p>', 'low', 'todo', '王五', '前端负责人', 6, NULL, NOW() - INTERVAL 1 DAY, NOW())
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO bug (id, project_id, sprint_id, title, content, severity, priority, status, steps, expected_result, actual_result, assignee, reporter, fix_version, resolved_at, created_at, updated_at) VALUES
(1, 1, 1, '导出 1 万条数据超时', '<p>大文件导出 504。</p>', 'high', 'high', 'fixing', '1.选择全部客户 2.点击导出', '异步返回任务号', '同步请求超时', '李四', '测试工程师', 'v1.1.0', NULL, NOW() - INTERVAL 3 DAY, NOW()),
(2, 1, NULL, '登录后偶发跳回登录页', '<p>token 校验偶发失败。</p>', 'medium', 'medium', 'open', '1.登录 2.刷新页面', '保持登录', '偶发 401', '王五', '前端工程师', NULL, NULL, NOW() - INTERVAL 1 DAY, NOW())
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO project_member (id, project_id, user_id, role, joined_at) VALUES
(1, 1, 1, 'admin', NOW()),
(2, 1, 2, 'developer', NOW())
ON DUPLICATE KEY UPDATE role = VALUES(role);

INSERT INTO test_case (id, project_id, title, preconditions, steps, expected_result, priority, status, owner, created_at, updated_at) VALUES
(1, 1, '客户列表分页查询', '已登录且有客户数据', '1.进入客户列表 2.翻页', '分页正常、无重复数据', 'high', 'active', '测试工程师', NOW(), NOW()),
(2, 1, '批量导出 Excel', '有 100 条客户数据', '1.全选 2.导出', '下载文件内容正确', 'medium', 'active', '测试工程师', NOW(), NOW())
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO test_plan (id, project_id, name, description, start_date, end_date, status, owner, created_at, updated_at) VALUES
(1, 1, 'CRM 一期回归计划', '覆盖客户模块核心链路', CURDATE(), CURDATE() + INTERVAL 3 DAY, 'running', '测试工程师', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO test_plan_case (id, plan_id, case_id, status, actual_result, executor, executed_at) VALUES
(1, 1, 1, 'pass', '分页正常', '测试工程师', NOW())
ON DUPLICATE KEY UPDATE status = VALUES(status);

INSERT INTO milestone (id, project_id, name, description, due_date, status, created_at) VALUES
(1, 1, 'CRM v1.0 发布', '一期上线目标', CURDATE() + INTERVAL 14 DAY, 'in_progress', NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO release_record (id, project_id, version, environment, description, status, operator, released_at, created_at) VALUES
(1, 1, 'v0.9.0', 'test', '测试环境首轮部署', 'done', '运维工程师', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(2, 1, 'v1.0.0-rc1', 'staging', '预发验证', 'deploying', '运维工程师', NULL, NOW())
ON DUPLICATE KEY UPDATE version = VALUES(version);
