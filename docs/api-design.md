# 接口设计文档

## 1. 接口规范

### 基础信息

- 基础路径：`/api`
- 数据格式：JSON
- 字符编码：UTF-8

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

### 错误码约定

| code | 含义 | 使用场景 |
|------|------|----------|
| 200 | 成功 | 所有成功响应 |
| 400 | 参数校验失败 | 请求参数缺失或格式错误 |
| 401 | 未登录 | Token 缺失、无效或过期 |
| 403 | 权限不足 | 无权执行该操作 |
| 404 | 资源不存在 | ID 对应的记录不存在 |
| 409 | 业务冲突 | 状态不允许变更等业务规则冲突 |
| 500 | 服务器内部错误 | 未预期的异常 |

### 分页参数

所有列表查询接口支持以下分页参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页条数（最大 100） |

分页响应结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "list": []
  }
}
```

---

## 2. 访客端接口（无需鉴权）

### 2.1 查询课程列表

**请求**

```
GET /api/public/courses
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | Long | 否 | 按分类筛选 |
| keyword | String | 否 | 按标题模糊搜索 |
| pageNum | Integer | 否 | 页码（默认 1） |
| pageSize | Integer | 否 | 每页条数（默认 10） |

**响应 data**

```json
{
  "total": 50,
  "pageNum": 1,
  "pageSize": 10,
  "list": [
    {
      "id": 1,
      "categoryId": 1,
      "categoryName": "编程开发",
      "title": "Java 入门到精通",
      "subtitle": "零基础学 Java",
      "coverImage": "https://...",
      "teacherName": "张老师",
      "price": 2999.00,
      "registrationFee": 500.00
    }
  ]
}
```

**业务规则**
- 只返回 `status = 'ONLINE'` 且 `deleted = 0` 的课程
- `keyword` 模糊匹配 `title` 字段

---

### 2.2 查询课程详情

**请求**

```
GET /api/public/courses/{id}
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 课程ID |

**响应 data**

```json
{
  "id": 1,
  "categoryId": 1,
  "categoryName": "编程开发",
  "title": "Java 入门到精通",
  "subtitle": "零基础学 Java",
  "coverImage": "https://...",
  "description": "本课程面向零基础学员...",
  "teacherName": "张老师",
  "price": 2999.00,
  "registrationFee": 500.00
}
```

**错误响应**
- 404：课程不存在或未上架

---

### 2.3 提交报名

**请求**

```
POST /api/public/enrollments
Content-Type: application/json
```

**请求体**

```json
{
  "courseId": 1,
  "studentName": "李同学",
  "studentPhone": "13800138000",
  "studentEmail": "li@example.com",
  "remark": "希望周末班"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseId | Long | 是 | 课程ID |
| studentName | String | 是 | 报名人姓名（2-50 字） |
| studentPhone | String | 是 | 手机号（11 位） |
| studentEmail | String | 否 | 邮箱 |
| remark | String | 否 | 备注（最多 500 字） |

**响应 data**

```json
{
  "orderNo": "EN20260609001",
  "courseTitle": "Java 入门到精通",
  "registrationFee": 500.00,
  "message": "报名成功，我们会尽快与您联系"
}
```

**错误响应**
- 400：参数校验失败（姓名为空、手机号格式错误等）
- 404：课程不存在
- 409：课程未上线，无法报名

---

## 3. 管理员端接口（需 JWT 鉴权）

> 以下所有接口需在 Header 中携带：`Authorization: Bearer {token}`

### 3.1 管理员登录

**请求**

```
POST /api/admin/auth/login
Content-Type: application/json
```

**请求体**

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应 data**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "realName": "系统管理员"
}
```

**错误响应**
- 400：用户名或密码为空
- 401：用户名或密码错误

---

### 3.2 仪表盘统计

**请求**

```
GET /api/admin/dashboard/overview
```

**响应 data**

```json
{
  "totalCourses": 20,
  "onlineCourses": 15,
  "totalEnrollments": 150,
  "pendingEnrollments": 12,
  "paidCount": 100,
  "unpaidCount": 38,
  "partialCount": 10,
  "refundedCount": 2,
  "recentEnrollments": [
    {
      "orderNo": "EN20260609001",
      "courseTitle": "Java 入门到精通",
      "studentName": "李同学",
      "enrollmentStatus": "PENDING",
      "createTime": "2026-06-09 10:30:00"
    }
  ]
}
```

**业务规则**
- `recentEnrollments` 返回最近 10 条报名记录，按 `create_time` 倒序

---

### 3.3 新增课程

**请求**

```
POST /api/admin/courses
Content-Type: application/json
```

**请求体**

```json
{
  "categoryId": 1,
  "title": "Python 数据分析",
  "subtitle": "从零开始学数据分析",
  "coverImage": "https://...",
  "description": "课程详细描述...",
  "teacherName": "王老师",
  "price": 1999.00,
  "registrationFee": 300.00
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | Long | 是 | 分类ID |
| title | String | 是 | 课程标题（1-200 字） |
| subtitle | String | 否 | 副标题 |
| coverImage | String | 否 | 封面图 URL |
| description | String | 否 | 课程介绍 |
| teacherName | String | 否 | 授课教师 |
| price | BigDecimal | 是 | 课程价格（≥ 0） |
| registrationFee | BigDecimal | 是 | 报名费（≥ 0） |

**响应 data**

返回完整的课程信息（同课程详情结构），`status` 初始为 `DRAFT`。

**错误响应**
- 400：参数校验失败
- 404：分类不存在

---

### 3.4 修改课程

**请求**

```
PUT /api/admin/courses/{id}
Content-Type: application/json
```

**请求体**（所有字段可选，只传需要修改的字段）

```json
{
  "title": "Python 数据分析实战",
  "price": 2499.00
}
```

**错误响应**
- 400：参数校验失败
- 404：课程不存在

---

### 3.5 上架课程

**请求**

```
PUT /api/admin/courses/{id}/online
```

**业务规则**
- 只有 `DRAFT` 或 `OFFLINE` 状态的课程可以上架
- 上架后状态变为 `ONLINE`

**错误响应**
- 404：课程不存在
- 409：当前状态不允许上架（如已删除）

---

### 3.6 下架课程

**请求**

```
PUT /api/admin/courses/{id}/offline
```

**业务规则**
- 只有 `ONLINE` 状态的课程可以下架
- 下架后状态变为 `OFFLINE`

**错误响应**
- 404：课程不存在
- 409：当前状态不允许下架

---

### 3.7 查询报名订单列表

**请求**

```
GET /api/admin/enrollments
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| enrollmentStatus | String | 否 | 按报名状态筛选 |
| paymentStatus | String | 否 | 按缴费状态筛选 |
| keyword | String | 否 | 按订单号、学员姓名、手机号模糊搜索 |
| pageNum | Integer | 否 | 页码（默认 1） |
| pageSize | Integer | 否 | 每页条数（默认 10） |

**响应 data (list 项)**

```json
{
  "id": 1,
  "orderNo": "EN20260609001",
  "courseId": 1,
  "courseTitle": "Java 入门到精通",
  "studentName": "李同学",
  "studentPhone": "13800138000",
  "studentEmail": "li@example.com",
  "registrationFee": 500.00,
  "paidAmount": 300.00,
  "paymentStatus": "PARTIAL",
  "enrollmentStatus": "CONTACTED",
  "createTime": "2026-06-09 10:30:00"
}
```

---

### 3.8 修改报名状态

**请求**

```
PUT /api/admin/enrollments/{id}/status
Content-Type: application/json
```

**请求体**

```json
{
  "enrollmentStatus": "CONTACTED"
}
```

**状态流转规则**

| 当前状态 | 允许变更到 |
|----------|-----------|
| PENDING | CONTACTED, CANCELLED |
| CONTACTED | ENROLLED, CANCELLED |
| ENROLLED | （终态，不可变更） |
| CANCELLED | （终态，不可变更） |

**错误响应**
- 404：订单不存在
- 409：不允许的状态变更

---

### 3.9 登记缴费

**请求**

```
POST /api/admin/payments
Content-Type: application/json
```

**请求体**

```json
{
  "orderId": 1,
  "amount": 300.00,
  "paymentMethod": "WECHAT",
  "paymentTime": "2026-06-09 14:00:00",
  "operatorName": "张老师",
  "remark": "微信转账"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | Long | 是 | 报名订单ID |
| amount | BigDecimal | 是 | 缴费金额（必须 > 0） |
| paymentMethod | String | 是 | 支付方式: CASH / WECHAT / ALIPAY / BANK |
| paymentTime | String | 否 | 缴费时间（默认当前时间） |
| operatorName | String | 否 | 经办人 |
| remark | String | 否 | 备注 |

**响应 data**

```json
{
  "id": 1,
  "orderId": 1,
  "amount": 300.00,
  "paymentMethod": "WECHAT",
  "paymentTime": "2026-06-09 14:00:00",
  "operatorName": "张老师",
  "remark": "微信转账"
}
```

**业务规则（事务）**
1. 插入缴费记录
2. 计算该订单累计已缴金额：`SUM(payment_record.amount) WHERE order_id = ?`
3. 更新订单 `paid_amount` 和 `payment_status`：
   - 累计金额 ≥ 应缴金额 → `PAID`
   - 0 < 累计金额 < 应缴金额 → `PARTIAL`

**错误响应**
- 400：金额 ≤ 0
- 404：订单不存在

---

### 3.10 财务汇总

**请求**

```
GET /api/admin/finance/summary
```

**响应 data**

```json
{
  "totalRegistrationFee": 50000.00,
  "totalPaidAmount": 35000.00,
  "totalUnpaidAmount": 12000.00,
  "totalPartialAmount": 3000.00,
  "totalRefundedAmount": 0.00,
  "orderCount": 150,
  "paidCount": 100,
  "unpaidCount": 38,
  "partialCount": 10,
  "refundedCount": 2
}
```
