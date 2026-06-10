# 架构设计文档

## 1. 系统总体架构

本系统采用 **B/S 架构（Browser/Server）**，前后端分离部署，通过 RESTful API 进行数据交互。

```
┌──────────────┐     HTTP/JSON     ┌──────────────────────────────┐
│              │  ◄──────────────► │        Spring Boot 3         │
│   浏览器     │                   │                              │
│  (Vue 3)     │                   │  Controller → Service → Mapper│
│              │                   │              ↓               │
└──────────────┘                   │           MySQL 8.x          │
                                   └──────────────────────────────┘
```

### 架构分层

| 层级 | 职责 | 技术实现 |
|------|------|----------|
| 视图层 (View) | 页面渲染、用户交互、发起 API 请求 | Vue 3 + Element Plus |
| 控制层 (Controller) | 接收 HTTP 请求、参数校验、调用 Service、封装响应 | Spring MVC Controller |
| 业务层 (Service) | 核心业务规则、状态流转、金额计算、事务管理 | Spring Service + @Transactional |
| 持久层 (Mapper) | 数据库访问、SQL 映射 | MyBatis-Plus |
| 数据层 (Database) | 数据存储 | MySQL 8.x |

### 分层调用规则

```
Controller ──调用──► Service ──调用──► Mapper
    │                   │                │
    │                   │                │
 不允许直接访问       不允许直接        不允许泄露
 Mapper 或数据库      访问数据库         数据库结构
```

- Controller 层**禁止**包含 SQL、ORM 操作或复杂业务逻辑
- Service 层**禁止**直接依赖 HTTP 状态码（通过异常类型间接映射）
- Mapper 层**禁止**将数据库特定结构泄露到上层

## 2. 后端模块结构

```
com.institution.coursemanager
├── controller
│   ├── public                    # 访客端（无需鉴权）
│   │   ├── PublicCourseController
│   │   └── PublicEnrollmentController
│   └── admin                     # 管理员端（需 JWT 鉴权）
│       ├── AdminAuthController
│       ├── AdminCourseController
│       ├── AdminEnrollmentController
│       ├── AdminPaymentController
│       ├── AdminFinanceController
│       └── AdminDashboardController
├── service                       # 业务逻辑接口
│   └── impl                      # 业务逻辑实现
├── mapper                        # MyBatis Mapper 接口
├── entity                        # 数据库实体
├── dto                           # 请求入参
├── vo                            # 响应出参
├── enums                         # 枚举类
├── config                        # 配置类
├── interceptor                   # 拦截器
├── exception                     # 异常与全局异常处理
└── util                          # 工具类
```

## 3. 前端模块结构

```
src/
├── views
│   ├── public                    # 访客端页面
│   │   ├── HomePage.vue          # 机构介绍
│   │   ├── CourseListPage.vue    # 课程列表
│   │   ├── CourseDetailPage.vue  # 课程详情
│   │   └── EnrollmentPage.vue    # 报名表单
│   └── admin                     # 管理员端页面
│       ├── LoginPage.vue         # 登录
│       ├── DashboardPage.vue     # 仪表盘
│       ├── CourseManagePage.vue  # 课程管理
│       ├── EnrollmentManagePage.vue  # 报名管理
│       └── FinancePage.vue       # 财务管理
├── api                           # API 封装
│   ├── request.js                # Axios 实例
│   ├── public.js                 # 访客端接口
│   └── admin.js                  # 管理员端接口
├── router                        # 路由配置
├── components                    # 公共组件
│   ├── AdminLayout.vue           # 管理后台布局
│   └── PublicLayout.vue          # 访客端布局
└── utils                         # 工具函数
    ├── auth.js                   # Token 管理
    └── format.js                 # 格式化
```

## 4. 接口鉴权方案

### 访客端

所有 `/api/public/**` 接口无需鉴权，直接访问。

### 管理员端

1. 管理员通过 `POST /api/admin/auth/login` 提交用户名和密码
2. 后端校验通过后，生成 JWT Token（有效期 24 小时）
3. 前端将 Token 存入 localStorage
4. 后续请求通过 Axios 拦截器在 Header 中携带 `Authorization: Bearer {token}`
5. 后端通过 `AdminAuthInterceptor` 拦截 `/api/admin/**` 请求，校验 Token 有效性
6. Token 无效或过期返回 HTTP 401

### 密码安全

管理员密码使用 BCrypt 算法加密存储，数据库中不保存明文密码。

## 5. 统一响应格式

所有 API 接口统一返回以下 JSON 结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未登录 / Token 无效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 业务冲突（如状态不允许变更） |
| 500 | 服务器内部错误 |

分页查询返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "list": [ ... ]
  }
}
```

## 6. 异常处理机制

通过 `@RestControllerAdvice` 实现全局异常捕获：

```
BusinessException（自定义业务异常）
  ├── ValidationException    → 400
  ├── NotFoundException      → 404
  ├── UnauthorizedException  → 401
  ├── ConflictException      → 409
  └── InternalException      → 500

MethodArgumentNotValidException（参数校验）   → 400
HttpMessageNotReadableException（请求体解析） → 400
Exception（未知异常）                         → 500
```

## 7. 事务管理

以下场景使用 `@Transactional` 保证数据一致性：

- **缴费登记**：插入缴费记录 + 更新订单的 `paid_amount` 和 `payment_status` 必须在同一事务中
- **课程逻辑删除**：标记删除时需检查是否存在未完结的报名订单

其他单表操作由 MyBatis-Plus 默认事务管理即可。

## 8. 枚举值约定

| 枚举类型 | 取值 |
|----------|------|
| 课程状态 (CourseStatus) | `DRAFT` / `ONLINE` / `OFFLINE` |
| 报名状态 (EnrollmentStatus) | `PENDING` / `CONTACTED` / `ENROLLED` / `CANCELLED` |
| 缴费状态 (PaymentStatus) | `UNPAID` / `PARTIAL` / `PAID` / `REFUNDED` |
| 支付方式 (PaymentMethod) | `CASH` / `WECHAT` / `ALIPAY` / `BANK` |
