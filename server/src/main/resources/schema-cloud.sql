-- DevFlow 云端初始化 不删表 不建库
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
