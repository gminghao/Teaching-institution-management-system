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

---

## 9. DTO/VO 契约定义

### 命名规范

- 请求入参类使用 `DTO` 后缀，存放在 `com.institution.coursemanager.dto`
- 响应出参类使用 `VO` 后缀，存放在 `com.institution.coursemanager.vo`
- 校验注解使用 `jakarta.validation.constraints`

### 9.1 公共响应包装

| 类名 | 类型 | 字段 | 说明 |
|------|------|------|------|
| `Result<T>` | 泛型响应包装 | `code: Integer`, `message: String`, `data: T` | 所有 API 统一返回此结构 |
| `PageResult<T>` | 泛型分页包装 | `total: Long`, `pageNum: Integer`, `pageSize: Integer`, `list: List<T>` | 作为分页接口的 `data` 字段 |

### 9.2 访客端 DTO/VO

| 类名 | 类型 | 字段 | 使用接口 | 校验规则 |
|------|------|------|----------|----------|
| `EnrollmentSubmitDTO` | 请求 DTO | `courseId: Long`, `studentName: String`, `studentPhone: String`, `studentEmail: String`, `remark: String` | POST /api/public/enrollments | courseId @NotNull; studentName @NotBlank @Size(2-50); studentPhone @NotBlank @Pattern(11位手机号); studentEmail @Email(optional); remark @Size(max=500)(optional) |
| `PublicCourseVO` | 响应 VO | `id`, `categoryId`, `categoryName`, `title`, `subtitle`, `coverImage`, `teacherName`, `price`, `registrationFee` | GET /api/public/courses | -- |
| `PublicCourseDetailVO` | 响应 VO | `id`, `categoryId`, `categoryName`, `title`, `subtitle`, `coverImage`, `description`, `teacherName`, `price`, `registrationFee` | GET /api/public/courses/{id} | 比列表 VO 多 `description` 字段 |
| `EnrollmentSubmitVO` | 响应 VO | `orderNo`, `courseTitle`, `registrationFee`, `message` | POST /api/public/enrollments | -- |

### 9.3 管理员认证 DTO/VO

| 类名 | 类型 | 字段 | 使用接口 | 校验规则 |
|------|------|------|----------|----------|
| `AdminLoginDTO` | 请求 DTO | `username: String`, `password: String` | POST /api/admin/auth/login | @NotBlank |
| `AdminLoginVO` | 响应 VO | `token`, `username`, `realName` | POST /api/admin/auth/login | -- |

### 9.4 仪表盘 VO

| 类名 | 类型 | 字段 | 使用接口 |
|------|------|------|----------|
| `DashboardOverviewVO` | 响应 VO | `totalCourses`, `onlineCourses`, `totalEnrollments`, `pendingEnrollments`, `paidCount`, `unpaidCount`, `partialCount`, `refundedCount`, `recentEnrollments: List<RecentEnrollmentVO>` | GET /api/admin/dashboard/overview |
| `RecentEnrollmentVO` | 响应 VO（嵌套） | `orderNo`, `courseTitle`, `studentName`, `enrollmentStatus`, `createTime` | 嵌入 DashboardOverviewVO |

### 9.5 管理员课程 DTO/VO

| 类名 | 类型 | 字段 | 使用接口 | 校验规则 |
|------|------|------|----------|----------|
| `CourseCreateDTO` | 请求 DTO | `categoryId`, `title`, `subtitle`, `coverImage`, `description`, `teacherName`, `price`, `registrationFee` | POST /api/admin/courses | categoryId @NotNull; title @NotBlank @Size(1-200); price @NotNull @Min(0); registrationFee @NotNull @Min(0) |
| `CourseUpdateDTO` | 请求 DTO | 同 CreateDTO，所有字段可选 | PUT /api/admin/courses/{id} | 仅校验传入的字段 |
| `AdminCourseVO` | 响应 VO | `id`, `categoryId`, `categoryName`, `title`, `subtitle`, `coverImage`, `description`, `teacherName`, `price`, `registrationFee`, `status`, `createTime`, `updateTime` | GET/POST/PUT 课程接口 | -- |

### 9.6 管理员报名 DTO/VO

| 类名 | 类型 | 字段 | 使用接口 | 校验规则 |
|------|------|------|----------|----------|
| `EnrollmentStatusDTO` | 请求 DTO | `enrollmentStatus: String` | PUT /api/admin/enrollments/{id}/status | @NotNull, 枚举值: PENDING/CONTACTED/ENROLLED/CANCELLED |
| `AdminEnrollmentVO` | 响应 VO | `id`, `orderNo`, `courseId`, `courseTitle`, `studentName`, `studentPhone`, `studentEmail`, `registrationFee`, `paidAmount`, `paymentStatus`, `enrollmentStatus`, `createTime` | GET /api/admin/enrollments | -- |

### 9.7 管理员缴费 DTO/VO

| 类名 | 类型 | 字段 | 使用接口 | 校验规则 |
|------|------|------|----------|----------|
| `PaymentCreateDTO` | 请求 DTO | `orderId`, `amount`, `paymentMethod`, `paymentTime`, `operatorName`, `remark` | POST /api/admin/payments | orderId @NotNull; amount @NotNull @DecimalMin(0.01); paymentMethod @NotNull (CASH/WECHAT/ALIPAY/BANK) |
| `PaymentRecordVO` | 响应 VO | `id`, `orderId`, `amount`, `paymentMethod`, `paymentTime`, `operatorName`, `remark` | POST /api/admin/payments | -- |

### 9.8 财务汇总 VO

| 类名 | 类型 | 字段 | 使用接口 |
|------|------|------|----------|
| `FinanceSummaryVO` | 响应 VO | `totalRegistrationFee`, `totalPaidAmount`, `totalUnpaidAmount`, `totalPartialAmount`, `totalRefundedAmount`, `orderCount`, `paidCount`, `unpaidCount`, `partialCount`, `refundedCount` | GET /api/admin/finance/summary |

---

## 10. 前端路由设计

### 10.1 路由表

| 路由路径 | 组件 | 布局 | 需要鉴权 | 说明 |
|----------|------|------|----------|------|
| `/` | `HomePage.vue` | PublicLayout | 否 | 机构介绍首页 |
| `/courses` | `CourseListPage.vue` | PublicLayout | 否 | 课程列表（支持筛选） |
| `/courses/:id` | `CourseDetailPage.vue` | PublicLayout | 否 | 课程详情 |
| `/enroll/:courseId` | `EnrollmentPage.vue` | PublicLayout | 否 | 报名表单 |
| `/admin/login` | `LoginPage.vue` | 无（独立页面） | 否 | 管理员登录 |
| `/admin/dashboard` | `DashboardPage.vue` | AdminLayout | 是 | 仪表盘概览 |
| `/admin/courses` | `CourseManagePage.vue` | AdminLayout | 是 | 课程管理 |
| `/admin/enrollments` | `EnrollmentManagePage.vue` | AdminLayout | 是 | 报名管理 |
| `/admin/finance` | `FinancePage.vue` | AdminLayout | 是 | 财务管理 |

### 10.2 路由架构说明

- 使用 Vue Router 的 `createRouter` + `createWebHistory`
- 全局导航守卫 `beforeEach` 检查 `/admin/**` 路由（除 `/admin/login`）的 `localStorage` token
- 未登录访问 `/admin/**` → 重定向到 `/admin/login`
- 已登录访问 `/admin/login` → 重定向到 `/admin/dashboard`
- `PublicLayout` 包裹所有访客端路由，提供顶部导航栏和页脚
- `AdminLayout` 包裹所有管理员端路由（除 login），提供侧边栏和顶栏

---

## 11. 前端页面布局描述

### 11.1 HomePage.vue（机构介绍首页）

- **布局**：PublicLayout（顶部导航 + 内容区 + 页脚）
- **顶部导航栏**：Logo、首页链接、课程列表链接、管理员登录链接
- **Hero 区域**：机构名称、宣传语、背景图
- **优势展示**：4 张卡片（师资专业、课程系统、价格透明、报名便捷）
- **热门课程**：水平卡片列表，展示最多 6 门 ONLINE 课程（调用 GET /api/public/courses?pageSize=6）
- **行动引导**："浏览全部课程"按钮，链接到 `/courses`
- **页脚**：版权信息

### 11.2 CourseListPage.vue（课程列表）

- **布局**：PublicLayout
- **筛选栏**：课程分类列表（可点击筛选）、关键词搜索框
- **课程卡片网格**：封面图、标题、副标题、教师、价格，点击跳转 `/courses/:id`
- **分页组件**：Element Plus `el-pagination`
- **数据源**：GET /api/public/courses（支持 categoryId、keyword、pageNum、pageSize）

### 11.3 CourseDetailPage.vue（课程详情）

- **布局**：PublicLayout
- **面包屑**：首页 > 课程列表 > [课程标题]
- **左侧**：封面大图
- **右侧**：标题、副标题、分类标签、教师姓名、价格（突出显示）、报名费、"立即报名"按钮
- **下方**：课程详细介绍（富文本区域）
- **数据源**：GET /api/public/courses/{id}

### 11.4 EnrollmentPage.vue（报名表单）

- **布局**：PublicLayout
- **课程摘要卡片**（只读）：课程标题、价格、报名费
- **表单**（Element Plus `el-form`）：姓名（必填）、手机号（必填，11位校验）、邮箱（选填）、备注（选填 textarea）
- **提交**：调用 POST /api/public/enrollments
- **成功状态**：替换表单为成功提示，展示 orderNo、courseTitle、registrationFee、友好提示信息
- **错误处理**：校验错误行内展示，业务错误（404/409）通过 `ElMessage` 提示

### 11.5 LoginPage.vue（管理员登录）

- **布局**：独立页面（无 PublicLayout/AdminLayout）
- **居中卡片设计**
- **表单**：用户名输入框、密码输入框、登录按钮
- **提交**：调用 POST /api/admin/auth/login
- **成功**：将 token + username + realName 存入 localStorage，跳转 `/admin/dashboard`
- **失败**：通过 `ElMessage` 显示错误信息

### 11.6 DashboardPage.vue（仪表盘）

- **布局**：AdminLayout（侧边栏：仪表盘、课程管理、报名管理、财务管理）
- **第一行**：4 个统计卡片（课程总数、已上架课程、报名总数、待处理报名）
- **第二行**：4 个缴费统计卡片（已缴费、未缴费、部分缴费、已退款）
- **下方**：最近报名表格（10 行，展示订单号、课程、学员姓名、报名状态标签、创建时间）
- **数据源**：GET /api/admin/dashboard/overview

### 11.7 CourseManagePage.vue（课程管理）

- **布局**：AdminLayout
- **顶部操作栏**："新增课程"按钮、搜索框、分类筛选下拉框
- **数据表格**（`el-table`）：标题、分类、教师、价格、报名费、状态（标签）、操作
- **行操作**：编辑、上架/下架切换（根据状态条件显示）、逻辑删除
- **新增/编辑弹窗**（`el-dialog`）：包含所有课程字段的表单，分类下拉框，价格输入
- **数据源**：GET /api/admin/courses（列表）、POST/PUT /api/admin/courses（新增/修改）、PUT /api/admin/courses/{id}/online|offline

### 11.8 EnrollmentManagePage.vue（报名管理）

- **布局**：AdminLayout
- **顶部筛选栏**：关键词搜索、报名状态下拉筛选、缴费状态下拉筛选
- **数据表格**：订单号、课程、学员姓名、手机号、报名费、已缴金额、缴费状态（标签）、报名状态（标签）、创建时间、操作
- **行操作**：修改状态（下拉/按钮，根据当前状态显示允许的目标状态）、登记缴费（打开缴费弹窗）
- **缴费弹窗**：金额输入、支付方式下拉（现金/微信/支付宝/银行转账）、缴费时间（日期时间选择器，默认当前）、经办人、备注
- **状态流转**：遵循 api-design.md 3.8 节的状态机规则
- **数据源**：GET /api/admin/enrollments、PUT /api/admin/enrollments/{id}/status、POST /api/admin/payments

### 11.9 FinancePage.vue（财务管理）

- **布局**：AdminLayout
- **顶部**：财务汇总卡片（报名费总额、已缴费总额、未缴费总额、部分缴费总额、已退款总额）
- **中部**：订单数量统计（总数、已缴费、未缴费、部分缴费、已退款）
- **下方**：缴费记录表格（可从报名列表按缴费状态筛选获取）
- **数据源**：GET /api/admin/finance/summary
