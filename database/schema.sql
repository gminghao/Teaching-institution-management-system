-- ============================================================
-- 教学机构课程管理系统 - 数据库建表脚本
-- 数据库: course_manager
-- 字符集: utf8mb4
-- ============================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS course_manager
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE course_manager;

-- ============================================================
-- 1. 管理员表
-- ============================================================
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(50)  NOT NULL COMMENT '登录用户名',
  `password`    VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密密码',
  `real_name`   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 初始管理员账号: admin / admin123 (BCrypt 加密)
INSERT INTO `admin_user` (`username`, `password`, `real_name`, `status`)
VALUES ('admin', '$2a$10$N.ZOn9G6w3Fz4nFHRXn5GOe9Th2jKZqK7TAKpXv4pG1wFkBmvUYCi', '系统管理员', 1);

-- ============================================================
-- 2. 课程分类表
-- ============================================================
DROP TABLE IF EXISTS `course_category`;
CREATE TABLE `course_category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(100) NOT NULL COMMENT '分类名称',
  `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序权重(数值越小越靠前)',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程分类表';

-- 初始分类数据
INSERT INTO `course_category` (`name`, `sort_order`, `status`) VALUES
('编程开发', 1, 1),
('设计创意', 2, 1),
('语言培训', 3, 1),
('职业技能', 4, 1),
('兴趣爱好', 5, 1);

-- ============================================================
-- 3. 课程表
-- ============================================================
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_id`      BIGINT        NOT NULL COMMENT '所属分类ID',
  `title`            VARCHAR(200)  NOT NULL COMMENT '课程标题',
  `subtitle`         VARCHAR(300)  DEFAULT NULL COMMENT '副标题',
  `cover_image`      VARCHAR(500)  DEFAULT NULL COMMENT '封面图URL',
  `description`      TEXT          DEFAULT NULL COMMENT '课程详细介绍',
  `teacher_name`     VARCHAR(50)   DEFAULT NULL COMMENT '授课教师',
  `price`            DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '课程价格',
  `registration_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '报名费',
  `status`           VARCHAR(20)   NOT NULL DEFAULT 'DRAFT' COMMENT '课程状态: DRAFT=草稿, ONLINE=已上架, OFFLINE=已下架',
  `deleted`          TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
  `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status_deleted` (`status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- ============================================================
-- 4. 报名订单表
-- ============================================================
DROP TABLE IF EXISTS `enrollment_order`;
CREATE TABLE `enrollment_order` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no`         VARCHAR(32)   NOT NULL COMMENT '订单编号(EN+日期+序号)',
  `course_id`        BIGINT        NOT NULL COMMENT '关联课程ID',
  `course_title`     VARCHAR(200)  NOT NULL COMMENT '课程标题(冗余快照)',
  `student_name`     VARCHAR(50)   NOT NULL COMMENT '报名人姓名',
  `student_phone`    VARCHAR(20)   NOT NULL COMMENT '报名人电话',
  `student_email`    VARCHAR(100)  DEFAULT NULL COMMENT '报名人邮箱',
  `remark`           VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  `registration_fee` DECIMAL(10,2) NOT NULL COMMENT '应缴报名费(下单时快照)',
  `paid_amount`      DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '已缴金额(汇总字段)',
  `payment_status`   VARCHAR(20)   NOT NULL DEFAULT 'UNPAID' COMMENT '缴费状态: UNPAID=未缴费, PARTIAL=部分缴费, PAID=已缴费, REFUNDED=已退款',
  `enrollment_status` VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '报名状态: PENDING=待处理, CONTACTED=已联系, ENROLLED=已报名, CANCELLED=已取消',
  `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_enrollment_status` (`enrollment_status`),
  KEY `idx_payment_status` (`payment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名订单表';

-- ============================================================
-- 5. 缴费记录表
-- ============================================================
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id`       BIGINT        NOT NULL COMMENT '关联报名订单ID',
  `amount`         DECIMAL(10,2) NOT NULL COMMENT '本次缴费金额',
  `payment_method` VARCHAR(20)   NOT NULL COMMENT '支付方式: CASH=现金, WECHAT=微信, ALIPAY=支付宝, BANK=银行转账',
  `payment_time`   DATETIME      NOT NULL COMMENT '缴费时间',
  `operator_name`  VARCHAR(50)   DEFAULT NULL COMMENT '经办人',
  `remark`         VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缴费记录表';
