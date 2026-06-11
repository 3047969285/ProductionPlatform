-- DevFlow 研发交付平台 · 一键初始化（会清空旧数据）
DROP DATABASE IF EXISTS productionplatform;
CREATE DATABASE productionplatform DEFAULT CHARSET utf8mb4;
USE productionplatform;

-- 用户
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    nickname VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'operator',
    create_time DATETIME NOT NULL
);

-- 软件产品
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    spec VARCHAR(200),
    unit VARCHAR(20) DEFAULT 'SaaS',
    create_time DATETIME NOT NULL
);

-- 研发团队
CREATE TABLE production_line (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    capacity INT DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    create_time DATETIME NOT NULL
);

-- 交付任务
CREATE TABLE production_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    line_id BIGINT,
    quantity INT NOT NULL DEFAULT 0,
    completed_qty INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id),
    FOREIGN KEY (line_id) REFERENCES production_line(id)
);

-- 产品需求
CREATE TABLE requirement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    req_no VARCHAR(32) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    product_id BIGINT NOT NULL,
    order_id BIGINT,
    priority VARCHAR(20) NOT NULL DEFAULT 'medium',
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    proposer VARCHAR(50),
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id),
    FOREIGN KEY (order_id) REFERENCES production_order(id)
);

-- 测试验收
CREATE TABLE quality_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    result VARCHAR(20) NOT NULL,
    defect_count INT NOT NULL DEFAULT 0,
    inspector VARCHAR(50) NOT NULL,
    remark VARCHAR(500),
    inspect_time DATETIME NOT NULL,
    FOREIGN KEY (order_id) REFERENCES production_order(id)
);

-- 示例数据（账号 admin / admin123）
INSERT INTO sys_user (username, password, nickname, role, create_time) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74afeee', '技术总监', 'admin', NOW()),
('operator', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74afeee', '开发工程师', 'operator', NOW());

INSERT INTO product (code, name, spec, unit, create_time) VALUES
('SW-CRM', '客户关系系统', 'Vue3 + Spring Boot', 'SaaS', NOW()),
('SW-ERP', '企业资源平台', 'React + Node.js', '定制', NOW()),
('SW-MOB', '移动办公 App', 'Flutter + API', 'App', NOW());

INSERT INTO production_line (code, name, capacity, status, create_time) VALUES
('TEAM-FE', '前端组', 5, 'active', NOW()),
('TEAM-BE', '后端组', 6, 'active', NOW()),
('TEAM-QA', '测试组', 4, 'active', NOW());

INSERT INTO production_order (order_no, product_id, line_id, quantity, completed_qty, status, create_time, update_time) VALUES
('DEV-2026001', 1, 1, 21, 13, 'running', NOW(), NOW()),
('DEV-2026002', 2, 2, 34, 0, 'pending', NOW(), NOW()),
('DEV-2026003', 3, 1, 18, 18, 'done', NOW(), NOW());

INSERT INTO requirement (req_no, title, description, product_id, order_id, priority, status, proposer, create_time, update_time) VALUES
('REQ-2026001', '客户列表批量导出', 'CRM 支持 Excel 导出', 1, 1, 'high', 'developing', '产品经理', NOW(), NOW()),
('REQ-2026002', '库存预警通知', '低库存推送钉钉', 2, 2, 'medium', 'review', '业务方', NOW(), NOW()),
('REQ-2026003', '离线消息缓存', '弱网本地暂存', 3, NULL, 'low', 'approved', '产品经理', NOW(), NOW());

INSERT INTO quality_record (order_id, result, defect_count, inspector, remark, inspect_time) VALUES
(3, 'pass', 0, '测试工程师', 'v2.1 回归通过', NOW());
