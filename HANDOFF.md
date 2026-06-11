# HANDOFF.md

> 项目交接报告 — 生成于 2026-06-11
> 当前分支：`dev` | 基线分支：`main`

---

## 1. Architecture Decisions（NEVER summarize）

### 系统架构

- **B/S 架构**，前后端分离，RESTful API 交互
- **三层架构**：Controller → Service → Mapper
- Controller 层**禁止**包含 SQL、ORM 操作或复杂业务逻辑
- Service 层**禁止**直接依赖 HTTP 状态码（通过异常类型间接映射）
- Mapper 层**禁止**将数据库特定结构泄露到上层

### 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Element Plus + Axios + Vite |
| 后端 | Spring Boot 3.2.5 + MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8.x |
| 鉴权 | JWT (jjwt 0.12.5) + BCrypt |

### 分层职责边界

**Controller 层允许**：解析请求参数、调用 Service、封装响应、映射错误码
**Controller 层禁止**：持久化逻辑、SQL、ORM、业务规则、费用计算

**Service 层允许**：业务规则、状态流转、金额计算、事务管理
**Service 层禁止**：直接依赖 HTTP 状态码

**Mapper 层允许**：数据库查询、ORM、数据映射、事务处理
**Mapper 层禁止**：数据库结构泄露到上层

### 包结构

```
com.institution.coursemanager
├── controller/{public,admin}   # HTTP 处理器
├── service/impl                # 业务逻辑
├── mapper                      # MyBatis Mapper 接口
├── entity                      # 数据库实体
├── dto                         # 请求入参
├── vo                          # 响应出参
├── enums                       # 枚举类
├── config                      # 配置类
├── interceptor                 # 拦截器
├── exception                   # 异常处理
└── util                        # 工具类
```

### 统一响应格式

```json
{ "code": 200, "message": "操作成功", "data": { ... } }
```

分页响应使用 `PageResult<T>` 作为 `data` 字段。

### 枚举值约定

| 枚举 | 取值 |
|------|------|
| CourseStatus | DRAFT / ONLINE / OFFLINE |
| EnrollmentStatus | PENDING / CONTACTED / ENROLLED / CANCELLED |
| PaymentStatus | UNPAID / PARTIAL / PAID / REFUNDED |
| PaymentMethod | CASH / WECHAT / ALIPAY / BANK |

### 前端路由鉴权

- `/api/public/**` 无需鉴权
- `/api/admin/**` 需 JWT Token（`Authorization: Bearer {token}`）
- 前端导航守卫检查 `/admin/**` 路由的 localStorage token

### 异常处理体系

```
BusinessException
  ├── ValidationException    → 400
  ├── NotFoundException      → 404
  ├── UnauthorizedException  → 401
  ├── ConflictException      → 409
  └── InternalException      → 500
```

### 事务管理要求

- 缴费登记：插入缴费记录 + 更新订单 `paid_amount` / `payment_status` 必须在同一事务
- 课程逻辑删除：需检查是否存在未完结报名订单

### 状态流转规则

**课程状态**：DRAFT → ONLINE ↔ OFFLINE（DRAFT 不可直接下架）
**报名状态**：PENDING → CONTACTED → ENROLLED（终态）/ CANCELLED（终态），不允许跳级
**缴费状态**：系统自动计算（累计=0→UNPAID，0<累计<应缴→PARTIAL，累计≥应缴→PAID，手动→REFUNDED）

---

## 2. Modified Files and Key Changes

### Commit History

```
94a14d3 feat: Phase 2 DTO/VO 契约定义
144503d docs: 添加 Phase 1 Code Review 注意事项
94014ee fix: 修复前端 lint 问题
41d5161 feat: Phase 1 项目初始化与基础设施
```

### Phase 1: 项目初始化与基础设施

**后端新增文件**

| 文件 | 说明 |
|------|------|
| `backend/pom.xml` | Maven 配置（Spring Boot 3.2.5, MyBatis-Plus 3.5.5, JWT 0.12.5） |
| `backend/.gitignore` | Git 忽略规则 |
| `backend/src/main/resources/application.yml` | 数据源、MyBatis-Plus、JWT 配置 |
| `backend/src/main/java/.../CourseManagerApplication.java` | 主启动类 |
| `backend/src/main/java/.../config/MybatisPlusConfig.java` | 分页插件配置 |
| `backend/src/main/java/.../config/WebMvcConfig.java` | CORS 跨域配置 |
| `backend/src/main/java/.../vo/Result.java` | 统一响应包装类 |
| `backend/src/main/java/.../vo/PageResult.java` | 分页响应包装类 |

**前端新增文件**

| 文件 | 说明 |
|------|------|
| `frontend/package.json` | 依赖配置 |
| `frontend/vite.config.js` | Vite 配置（含 `/api` 代理到 8080） |
| `frontend/.eslintrc.cjs` | ESLint 配置 |
| `frontend/.gitignore` | Git 忽略规则 |
| `frontend/index.html` | 入口 HTML |
| `frontend/src/main.js` | Vue 应用入口（Element Plus 中文化） |
| `frontend/src/App.vue` | 根组件 |
| `frontend/src/router/index.js` | 路由配置（含导航守卫） |
| `frontend/src/api/request.js` | Axios 封装（Token 注入、错误处理） |
| `frontend/src/api/public.js` | 访客端 API 封装 |
| `frontend/src/api/admin.js` | 管理员端 API 封装 |
| `frontend/src/utils/auth.js` | Token 管理工具 |
| `frontend/src/utils/format.js` | 格式化工具 |
| `frontend/src/components/PublicLayout.vue` | 访客端布局（顶栏+内容+页脚） |
| `frontend/src/components/AdminLayout.vue` | 管理员端布局（侧边栏+顶栏+内容） |
| `frontend/src/views/public/HomePage.vue` | 占位 - 首页 |
| `frontend/src/views/public/CourseListPage.vue` | 占位 - 课程列表 |
| `frontend/src/views/public/CourseDetailPage.vue` | 占位 - 课程详情 |
| `frontend/src/views/public/EnrollmentPage.vue` | 占位 - 报名表单 |
| `frontend/src/views/admin/LoginPage.vue` | 占位 - 管理员登录 |
| `frontend/src/views/admin/DashboardPage.vue` | 占位 - 仪表盘 |
| `frontend/src/views/admin/CourseManagePage.vue` | 占位 - 课程管理 |
| `frontend/src/views/admin/EnrollmentManagePage.vue` | 占位 - 报名管理 |
| `frontend/src/views/admin/FinancePage.vue` | 占位 - 财务管理 |

**文档修改**

| 文件 | 变更 |
|------|------|
| `CHANGELOG.md` | 添加 Phase 1 已完成条目 |
| `docs/implementation-plan.md` | 标记 Phase 1 完成，添加 Code Review 注意事项 |

### Phase 2: DTO/VO 契约定义

**新增文件（20 个）**

| 文件 | 类型 | 说明 |
|------|------|------|
| `enums/CourseStatus.java` | 枚举 | DRAFT/ONLINE/OFFLINE，含 fromCode() |
| `enums/EnrollmentStatus.java` | 枚举 | PENDING/CONTACTED/ENROLLED/CANCELLED |
| `enums/PaymentStatus.java` | 枚举 | UNPAID/PARTIAL/PAID/REFUNDED |
| `enums/PaymentMethod.java` | 枚举 | CASH/WECHAT/ALIPAY/BANK |
| `dto/EnrollmentSubmitDTO.java` | 请求 DTO | 访客报名（含 @NotNull/@NotBlank/@Pattern/@Size/@Email 校验） |
| `dto/AdminLoginDTO.java` | 请求 DTO | 管理员登录 |
| `dto/CourseCreateDTO.java` | 请求 DTO | 新增课程（含校验） |
| `dto/CourseUpdateDTO.java` | 请求 DTO | 修改课程（所有字段选填） |
| `dto/EnrollmentStatusDTO.java` | 请求 DTO | 修改报名状态（String + @Pattern 校验） |
| `dto/PaymentCreateDTO.java` | 请求 DTO | 登记缴费（含校验） |
| `vo/PublicCourseVO.java` | 响应 VO | 访客课程列表 |
| `vo/PublicCourseDetailVO.java` | 响应 VO | 访客课程详情（多 description） |
| `vo/EnrollmentSubmitVO.java` | 响应 VO | 报名提交结果 |
| `vo/AdminLoginVO.java` | 响应 VO | 登录响应 |
| `vo/AdminCourseVO.java` | 响应 VO | 管理员课程 |
| `vo/AdminEnrollmentVO.java` | 响应 VO | 管理员报名 |
| `vo/PaymentRecordVO.java` | 响应 VO | 缴费记录 |
| `vo/DashboardOverviewVO.java` | 响应 VO | 仪表盘概览 |
| `vo/RecentEnrollmentVO.java` | 响应 VO | 最近报名（嵌套） |
| `vo/FinanceSummaryVO.java` | 响应 VO | 财务汇总 |

---

## 3. Current Verification Status

| 命令 | 结果 |
|------|------|
| `cd backend && mvn compile` | ✅ PASS |
| `cd backend && mvn test` | ✅ PASS |
| `cd frontend && pnpm install` | ✅ PASS |
| `cd frontend && pnpm build` | ✅ PASS |

---

## 4. Open TODOs and Rollback Notes

### 安全问题（需后续修复）

- [ ] **JWT Secret 硬编码**：`application.yml` 中 `jwt.secret` 应改为环境变量 `${JWT_SECRET:default-dev-only-key}`
- [ ] **数据库密码硬编码**：`application.yml` 中 `spring.datasource.password: root` 应改为 `${DB_PASSWORD:root}`

### 代码健壮性（需后续修复）

- [ ] **前端 `getUser()` 缺少异常处理**：`JSON.parse` 可能抛异常，需加 try-catch（文件：`frontend/src/utils/auth.js`）
- [ ] **前端 `request.js` 响应拦截器**：对 `responseType === 'blob'` 等场景需做豁免

### 待补充功能

- [ ] **404 路由**：`router/index.js` 缺少 catch-all 路由（`/:pathMatch(.*)*`）
- [ ] **生产日志级别**：`application.yml` 中 `log-impl: StdOutImpl` 生产环境应改为 Slf4j

### Rollback Notes

| 阶段 | 回滚方式 |
|------|----------|
| Phase 2 (DTO/VO) | 删除 `enums/`, `dto/`, `vo/` 下新增的 20 个 Java 文件，不影响其他代码 |
| Phase 1 (骨架) | 删除 `backend/` 和 `frontend/` 目录，恢复 `CHANGELOG.md`、`docs/implementation-plan.md` |

---

## 5. Tool Outputs（Pass/Fail Only）

| 工具/命令 | 结果 |
|-----------|------|
| `mvn compile` (Phase 1) | PASS |
| `mvn test` (Phase 1) | PASS |
| `pnpm build` (Phase 1) | PASS |
| `mvn compile` (Phase 2) | PASS |
| `mvn test` (Phase 2) | PASS |

---

## Progress Summary

| 阶段 | 总任务 | 已完成 | 进度 |
|------|--------|--------|------|
| Phase 1: 项目初始化 | 5 | 5 | 100% |
| Phase 2: DTO/VO 契约 | 7 | 7 | 100% |
| Phase 3: Entity + Service | 7 | 0 | 0% |
| Phase 4: Mapper | 5 | 0 | 0% |
| Phase 5: Controller | 10 | 0 | 0% |
| Phase 6: 前端基础 | 7 | 0 | 0% |
| Phase 7: 访客页面 | 4 | 0 | 0% |
| Phase 8: 管理页面 | 5 | 0 | 0% |
| Phase 9: 后端测试 | 10 | 0 | 0% |
| Phase 10: 集成测试 | 3 | 0 | 0% |
| Phase 11: 收尾 | 5 | 0 | 0% |
| **合计** | **68** | **12** | **17.6%** |
