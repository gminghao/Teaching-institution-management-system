-- H2 schema for test (MySQL compatibility mode)
CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS course_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

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
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

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
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_time DATETIME,
    operator_name VARCHAR(50),
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
