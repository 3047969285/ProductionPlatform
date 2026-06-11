-- DevFlow 一键初始化
DROP DATABASE IF EXISTS devflow;
CREATE DATABASE devflow DEFAULT CHARSET utf8mb4;
USE devflow;

CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    nickname VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'developer',
    created_at DATETIME NOT NULL
);

CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    tech_stack VARCHAR(200),
    delivery_type VARCHAR(20) DEFAULT 'SaaS',
    created_at DATETIME NOT NULL
);

CREATE TABLE dev_team (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    capacity INT DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at DATETIME NOT NULL
);

CREATE TABLE delivery_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_no VARCHAR(32) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    team_id BIGINT,
    story_points INT NOT NULL DEFAULT 0,
    done_points INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id),
    FOREIGN KEY (team_id) REFERENCES dev_team(id)
);

CREATE TABLE requirement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    req_no VARCHAR(32) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    product_id BIGINT NOT NULL,
    task_id BIGINT,
    priority VARCHAR(20) NOT NULL DEFAULT 'medium',
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    proposer VARCHAR(50),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id),
    FOREIGN KEY (task_id) REFERENCES delivery_task(id)
);

CREATE TABLE qa_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    result VARCHAR(20) NOT NULL,
    bug_count INT NOT NULL DEFAULT 0,
    inspector VARCHAR(50) NOT NULL,
    remark VARCHAR(500),
    tested_at DATETIME NOT NULL,
    FOREIGN KEY (task_id) REFERENCES delivery_task(id)
);

-- admin / admin123
INSERT INTO user (username, password, nickname, role, created_at) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74afeee', '技术总监', 'admin', NOW()),
('dev', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74afeee', '开发工程师', 'developer', NOW());

INSERT INTO product (code, name, tech_stack, delivery_type, created_at) VALUES
('SW-CRM', '客户关系系统', 'Vue3 + Spring Boot', 'SaaS', NOW()),
('SW-ERP', '企业资源平台', 'React + Node.js', '定制', NOW()),
('SW-MOB', '移动办公 App', 'Flutter + API', 'App', NOW());

INSERT INTO dev_team (code, name, capacity, status, created_at) VALUES
('FE', '前端组', 5, 'active', NOW()),
('BE', '后端组', 6, 'active', NOW()),
('QA', '测试组', 4, 'active', NOW());

INSERT INTO delivery_task (task_no, product_id, team_id, story_points, done_points, status, created_at, updated_at) VALUES
('DEV-2026001', 1, 1, 21, 13, 'running', NOW(), NOW()),
('DEV-2026002', 2, 2, 34, 0, 'pending', NOW(), NOW()),
('DEV-2026003', 3, 1, 18, 18, 'done', NOW(), NOW());

INSERT INTO requirement (req_no, title, description, product_id, task_id, priority, status, proposer, created_at, updated_at) VALUES
('REQ-001', '客户列表批量导出', 'CRM Excel 导出', 1, 1, 'high', 'developing', '产品经理', NOW(), NOW()),
('REQ-002', '库存预警通知', '低库存钉钉推送', 2, 2, 'medium', 'review', '业务方', NOW(), NOW()),
('REQ-003', '离线消息缓存', '弱网本地暂存', 3, NULL, 'low', 'approved', '产品经理', NOW(), NOW());

INSERT INTO qa_record (task_id, result, bug_count, inspector, remark, tested_at) VALUES
(3, 'pass', 0, '测试工程师', 'v2.1 回归通过', NOW());
