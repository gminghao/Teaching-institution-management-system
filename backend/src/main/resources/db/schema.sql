-- ============================================================
-- Production DDL for MySQL 8.0
-- 教学机构课程管理系统 - 数据库初始化脚本
-- ============================================================

-- 管理员用户表
CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 课程分类表
CREATE TABLE IF NOT EXISTS course_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 课程表
CREATE TABLE IF NOT EXISTS course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT,
    title VARCHAR(200) NOT NULL,
    subtitle VARCHAR(300),
    cover_image VARCHAR(500),
    description TEXT,
    teacher_name VARCHAR(50),
    price DECIMAL(10,2) DEFAULT 0,
    registration_fee DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'DRAFT',
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 报名单/订单表
CREATE TABLE IF NOT EXISTS enrollment_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL,
    CONSTRAINT uk_order_no UNIQUE (order_no),
    course_id BIGINT NOT NULL,
    course_title VARCHAR(200),
    student_name VARCHAR(50) NOT NULL,
    student_phone VARCHAR(20) NOT NULL,
    student_email VARCHAR(100),
    remark VARCHAR(500),
    registration_fee DECIMAL(10,2) DEFAULT 0,
    paid_amount DECIMAL(10,2) DEFAULT 0,
    payment_status VARCHAR(20) DEFAULT 'UNPAID',
    enrollment_status VARCHAR(20) DEFAULT 'PENDING',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 缴费记录表
CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_time DATETIME,
    operator_name VARCHAR(50),
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================================
-- Seed Data
-- ============================================================

-- 初始管理员账号 (密码为 BCrypt 加密的 'admin123')
INSERT INTO admin_user (username, password, real_name, status)
SELECT 'admin', '$2a$10$CBr8M3z3oPFt2kD2D1BDG.WY5eJ.0PJcdJ6fLsAPrLv4K/0./QWmW', 'Administrator', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM admin_user WHERE username = 'admin');

-- 初始课程分类
INSERT INTO course_category (id, name, sort_order, status)
SELECT 1, 'programming', 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_category WHERE id = 1);

INSERT INTO course_category (id, name, sort_order, status)
SELECT 2, 'design', 2, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM course_category WHERE id = 2);
