# 数据库设计文档

## 1. 概述

- 数据库类型：MySQL 8.x
- 字符集：utf8mb4
- 排序规则：utf8mb4_general_ci
- 存储引擎：InnoDB（支持事务和外键）

共 5 张表，覆盖管理员、课程分类、课程、报名订单、缴费记录五个核心业务实体。

## 2. ER 关系图（文字描述）

```
admin_user（管理员表）
    │ 1
    │
    │ N（经办缴费）
    │
payment_record（缴费记录表）  N ──── 1  enrollment_order（报名订单表）
                                                    │ N
                                                    │
                                                    │ 1
                                                    │
                                              course（课程表）
                                                    │ N
                                                    │
                                                    │ 1
                                                    │
                                          course_category（课程分类表）
```

**关系说明：**

- 一个课程分类下有多门课程（1:N）
- 一门课程可以有多条报名订单（1:N）
- 一条报名订单可以有多条缴费记录（1:N）
- 课程删除采用逻辑删除，不影响历史订单数据

## 3. 表结构详细设计

### 3.1 admin_user（管理员表）

| 字段 | 类型 | 约束 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 主键 |
| username | VARCHAR(50) | UNIQUE, NOT NULL | - | 登录用户名 |
| password | VARCHAR(255) | NOT NULL | - | BCrypt 加密密码 |
| real_name | VARCHAR(50) | - | NULL | 真实姓名 |
| status | TINYINT | NOT NULL | 1 | 状态: 1=启用, 0=禁用 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引：**
- `uk_username` (username) — 唯一索引

**初始数据：**
- admin / admin123（BCrypt 加密存储）

---

### 3.2 course_category（课程分类表）

| 字段 | 类型 | 约束 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 主键 |
| name | VARCHAR(100) | NOT NULL | - | 分类名称 |
| sort_order | INT | NOT NULL | 0 | 排序权重（数值越小越靠前） |
| status | TINYINT | NOT NULL | 1 | 状态: 1=启用, 0=禁用 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**初始数据：**
- 编程开发、设计创意、语言培训、职业技能、兴趣爱好

---

### 3.3 course（课程表）

| 字段 | 类型 | 约束 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 主键 |
| category_id | BIGINT | NOT NULL | - | 所属分类ID |
| title | VARCHAR(200) | NOT NULL | - | 课程标题 |
| subtitle | VARCHAR(300) | - | NULL | 副标题 |
| cover_image | VARCHAR(500) | - | NULL | 封面图URL |
| description | TEXT | - | NULL | 课程详细介绍 |
| teacher_name | VARCHAR(50) | - | NULL | 授课教师 |
| price | DECIMAL(10,2) | NOT NULL | 0.00 | 课程价格 |
| registration_fee | DECIMAL(10,2) | NOT NULL | 0.00 | 报名费 |
| status | VARCHAR(20) | NOT NULL | 'DRAFT' | DRAFT / ONLINE / OFFLINE |
| deleted | TINYINT | NOT NULL | 0 | 逻辑删除: 0=否, 1=是 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引：**
- `idx_category_id` (category_id) — 普通索引
- `idx_status_deleted` (status, deleted) — 联合索引

**设计说明：**
- `deleted` 字段实现逻辑删除，避免影响历史报名订单的关联查询
- `registration_fee` 是报名时需要缴纳的费用，与 `price`（课程售价）独立

---

### 3.4 enrollment_order（报名订单表）

| 字段 | 类型 | 约束 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 主键 |
| order_no | VARCHAR(32) | UNIQUE, NOT NULL | - | 订单编号 |
| course_id | BIGINT | NOT NULL | - | 关联课程ID |
| course_title | VARCHAR(200) | NOT NULL | - | 课程标题（冗余快照） |
| student_name | VARCHAR(50) | NOT NULL | - | 报名人姓名 |
| student_phone | VARCHAR(20) | NOT NULL | - | 报名人电话 |
| student_email | VARCHAR(100) | - | NULL | 报名人邮箱 |
| remark | VARCHAR(500) | - | NULL | 备注 |
| registration_fee | DECIMAL(10,2) | NOT NULL | - | 应缴报名费（下单时快照） |
| paid_amount | DECIMAL(10,2) | NOT NULL | 0.00 | 已缴金额（汇总字段） |
| payment_status | VARCHAR(20) | NOT NULL | 'UNPAID' | UNPAID / PARTIAL / PAID / REFUNDED |
| enrollment_status | VARCHAR(20) | NOT NULL | 'PENDING' | PENDING / CONTACTED / ENROLLED / CANCELLED |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引：**
- `uk_order_no` (order_no) — 唯一索引
- `idx_course_id` (course_id) — 普通索引
- `idx_enrollment_status` (enrollment_status) — 普通索引
- `idx_payment_status` (payment_status) — 普通索引

**设计说明：**
- `course_title` 和 `registration_fee` 为下单时从课程表快照的冗余字段，即使课程后续被修改或删除，订单数据仍然完整
- `paid_amount` 为汇总字段，通过缴费记录累加更新，避免每次查询都 SUM 计算
- 订单编号格式：`EN` + 日期 `yyyyMMdd` + 3 位自增序号，如 `EN20260609001`

---

### 3.5 payment_record（缴费记录表）

| 字段 | 类型 | 约束 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 主键 |
| order_id | BIGINT | NOT NULL | - | 关联报名订单ID |
| amount | DECIMAL(10,2) | NOT NULL | - | 本次缴费金额 |
| payment_method | VARCHAR(20) | NOT NULL | - | CASH / WECHAT / ALIPAY / BANK |
| payment_time | DATETIME | NOT NULL | - | 缴费时间 |
| operator_name | VARCHAR(50) | - | NULL | 经办人 |
| remark | VARCHAR(500) | - | NULL | 备注 |
| create_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- `idx_order_id` (order_id) — 普通索引

**设计说明：**
- 缴费记录一旦创建不可修改或删除，保证财务数据完整性
- 缴费记录插入与订单 `paid_amount` / `payment_status` 更新必须在同一事务中

## 4. 状态流转规则

### 4.1 课程状态

```
DRAFT ──上架──► ONLINE
ONLINE ──下架──► OFFLINE
OFFLINE ──上架──► ONLINE
```

- DRAFT 状态不可直接下架
- 逻辑删除（deleted=1）不改变 status 字段

### 4.2 报名状态

```
PENDING ──► CONTACTED ──► ENROLLED（终态）
PENDING ──► CANCELLED（终态）
CONTACTED ──► CANCELLED（终态）
```

- 不允许跳级变更（如 PENDING → ENROLLED）
- ENROLLED 和 CANCELLED 为终态，不可再变更

### 4.3 缴费状态（自动计算）

```
累计缴费 = 0         → UNPAID
0 < 累计 < 应缴金额   → PARTIAL
累计 ≥ 应缴金额       → PAID
手动标记退款          → REFUNDED
```

- 缴费状态由系统根据 `paid_amount` 和 `registration_fee` 自动计算
- `REFUNDED` 需手动设置

## 5. 命名规范

- 表名：小写下划线风格（`enrollment_order`）
- 字段名：小写下划线风格（`create_time`、`payment_status`）
- 主键：统一使用 `id`，BIGINT 自增
- 时间字段：统一使用 `create_time`、`update_time`
- 状态字段：VARCHAR(20)，存储固定枚举字符串
- 金额字段：DECIMAL(10,2)
