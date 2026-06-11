# 实施计划

> 本文件为动态实施计划，使用 `- [ ]` / `- [x]` 追踪任务完成状态。
> 优先级：P0 = 必须完成，P1 = 应该完成，P2 = 可选。
>
> **使用方法**：完成任务后将 `- [ ]` 改为 `- [x]`，并更新底部进度统计表。

---

## Phase 1: 项目初始化与基础设施

- [x] **P0** 初始化 Spring Boot 3 后端工程骨架 (`backend/`)
  - 依赖：无
  - 产出：pom.xml, application.yml, 主启动类
- [x] **P0** 初始化 Vue 3 前端工程骨架 (`frontend/`)
  - 依赖：无
  - 产出：package.json, vite.config.js, main.js, App.vue
- [x] **P0** 配置数据库连接和 MyBatis-Plus
  - 依赖：Phase 1 初始化完成
  - 产出：application.yml 数据源配置, MyBatis-Plus 配置类
- [x] **P0** 配置 Vite 开发代理 (前端 `/api` -> 后端 8080)
  - 依赖：前端初始化完成
  - 产出：vite.config.js proxy 配置
- [x] **P0** 创建统一响应包装类 `Result<T>` 和 `PageResult<T>`
  - 依赖：后端初始化完成
  - 产出：`Result.java`, `PageResult.java`

---

## Phase 2: DTO/VO 契约定义

- [x] **P0** 定义枚举类
  - 依赖：无
  - 产出：`CourseStatus`, `EnrollmentStatus`, `PaymentStatus`, `PaymentMethod`
- [x] **P0** 定义访客端 DTO/VO
  - 依赖：枚举类完成
  - 产出：`EnrollmentSubmitDTO`, `PublicCourseVO`, `PublicCourseDetailVO`, `EnrollmentSubmitVO`
- [x] **P0** 定义管理员登录 DTO/VO
  - 依赖：无
  - 产出：`AdminLoginDTO`, `AdminLoginVO`
- [x] **P0** 定义管理员课程 DTO/VO
  - 依赖：枚举类完成
  - 产出：`CourseCreateDTO`, `CourseUpdateDTO`, `AdminCourseVO`
- [x] **P0** 定义管理员报名 DTO/VO
  - 依赖：枚举类完成
  - 产出：`EnrollmentStatusDTO`, `AdminEnrollmentVO`
- [x] **P0** 定义管理员缴费 DTO/VO
  - 依赖：枚举类完成
  - 产出：`PaymentCreateDTO`, `PaymentRecordVO`
- [x] **P0** 定义仪表盘和财务 VO
  - 依赖：无
  - 产出：`DashboardOverviewVO`, `RecentEnrollmentVO`, `FinanceSummaryVO`

---

## Phase 3: Entity 模型 + Service 业务逻辑

- [x] **P0** 实现 Entity 模型类
  - 依赖：数据库设计文档
  - 产出：`AdminUser`, `CourseCategory`, `Course`, `EnrollmentOrder`, `PaymentRecord`
- [x] **P0** 实现 AuthService（登录认证 + JWT 生成/校验）
  - 依赖：Entity 完成, AdminLoginDTO/VO 完成
  - 产出：`AuthService`, `AuthServiceImpl`
- [x] **P0** 实现 CourseService（CRUD + 上下架 + 逻辑删除）
  - 依赖：Entity 完成, 课程 DTO/VO 完成
  - 产出：`CourseService`, `CourseServiceImpl`
- [x] **P0** 实现 EnrollmentService（报名提交 + 状态流转）
  - 依赖：Entity 完成, 报名 DTO/VO 完成
  - 产出：`EnrollmentService`, `EnrollmentServiceImpl`
- [x] **P0** 实现 PaymentService（缴费登记 + 事务管理）
  - 依赖：Entity 完成, 缴费 DTO/VO 完成
  - 产出：`PaymentService`, `PaymentServiceImpl`
- [x] **P0** 实现 DashboardService（仪表盘聚合统计）
  - 依赖：Entity 完成
  - 产出：`DashboardService`, `DashboardServiceImpl`
- [x] **P0** 实现 FinanceService（财务汇总）
  - 依赖：Entity 完成
  - 产出：`FinanceService`, `FinanceServiceImpl`

---

## Phase 4: Mapper 持久层

- [ ] **P0** 实现 AdminUserMapper
  - 依赖：Entity 完成
  - 产出：`AdminUserMapper.java`
- [ ] **P0** 实现 CourseCategoryMapper
  - 依赖：Entity 完成
  - 产出：`CourseCategoryMapper.java`
- [ ] **P0** 实现 CourseMapper
  - 依赖：Entity 完成
  - 产出：`CourseMapper.java`, 可能需要 XML 映射（复杂查询）
- [ ] **P0** 实现 EnrollmentOrderMapper
  - 依赖：Entity 完成
  - 产出：`EnrollmentOrderMapper.java`
- [ ] **P0** 实现 PaymentRecordMapper
  - 依赖：Entity 完成
  - 产出：`PaymentRecordMapper.java`

---

## Phase 5: Controller 层

- [ ] **P0** 实现全局异常处理器 `GlobalExceptionHandler`
  - 依赖：异常类定义完成
  - 产出：`GlobalExceptionHandler.java`
- [ ] **P0** 实现 JWT 拦截器 `AdminAuthInterceptor`
  - 依赖：AuthService 完成
  - 产出：`AdminAuthInterceptor.java`, `WebMvcConfig.java`
- [ ] **P0** 实现 PublicCourseController
  - 依赖：CourseService 完成
  - 产出：`PublicCourseController.java`
- [ ] **P0** 实现 PublicEnrollmentController
  - 依赖：EnrollmentService 完成
  - 产出：`PublicEnrollmentController.java`
- [ ] **P0** 实现 AdminAuthController
  - 依赖：AuthService 完成
  - 产出：`AdminAuthController.java`
- [ ] **P0** 实现 AdminDashboardController
  - 依赖：DashboardService 完成
  - 产出：`AdminDashboardController.java`
- [ ] **P0** 实现 AdminCourseController
  - 依赖：CourseService 完成
  - 产出：`AdminCourseController.java`
- [ ] **P0** 实现 AdminEnrollmentController
  - 依赖：EnrollmentService 完成
  - 产出：`AdminEnrollmentController.java`
- [ ] **P0** 实现 AdminPaymentController
  - 依赖：PaymentService 完成
  - 产出：`AdminPaymentController.java`
- [ ] **P0** 实现 AdminFinanceController
  - 依赖：FinanceService 完成
  - 产出：`AdminFinanceController.java`

---

## Phase 6: 前端基础设施

- [x] **P0** 配置 Vue Router 路由表
  - 依赖：前端初始化完成
  - 产出：`router/index.js` (路由定义 + 导航守卫)
- [x] **P0** 实现 Axios 请求封装 + 拦截器
  - 依赖：前端初始化完成
  - 产出：`api/request.js` (token 注入, 响应拦截, 错误处理)
- [x] **P0** 实现 API 接口封装
  - 依赖：request.js 完成
  - 产出：`api/public.js`, `api/admin.js`
- [x] **P0** 实现 Token 管理工具
  - 依赖：无
  - 产出：`utils/auth.js` (getToken, setToken, removeToken)
- [x] **P0** 实现格式化工具
  - 依赖：无
  - 产出：`utils/format.js` (金额格式化, 日期格式化)
- [x] **P0** 实现 PublicLayout 组件
  - 依赖：路由配置完成
  - 产出：`components/PublicLayout.vue` (顶部导航 + 内容区 + 页脚)
- [x] **P0** 实现 AdminLayout 组件
  - 依赖：路由配置完成
  - 产出：`components/AdminLayout.vue` (侧边栏 + 顶栏 + 内容区)

---

## Phase 7: 访客端页面 (Vue 3)

- [ ] **P0** 实现 HomePage.vue
  - 依赖：PublicLayout, api/public.js
  - 产出：`views/public/HomePage.vue`
- [ ] **P0** 实现 CourseListPage.vue
  - 依赖：PublicLayout, api/public.js
  - 产出：`views/public/CourseListPage.vue`
- [ ] **P0** 实现 CourseDetailPage.vue
  - 依赖：PublicLayout, api/public.js
  - 产出：`views/public/CourseDetailPage.vue`
- [ ] **P0** 实现 EnrollmentPage.vue
  - 依赖：PublicLayout, api/public.js
  - 产出：`views/public/EnrollmentPage.vue`

---

## Phase 8: 管理员端页面 (Vue 3)

- [ ] **P0** 实现 LoginPage.vue
  - 依赖：api/admin.js, utils/auth.js
  - 产出：`views/admin/LoginPage.vue`
- [ ] **P0** 实现 DashboardPage.vue
  - 依赖：AdminLayout, api/admin.js
  - 产出：`views/admin/DashboardPage.vue`
- [ ] **P0** 实现 CourseManagePage.vue
  - 依赖：AdminLayout, api/admin.js
  - 产出：`views/admin/CourseManagePage.vue`
- [ ] **P0** 实现 EnrollmentManagePage.vue
  - 依赖：AdminLayout, api/admin.js
  - 产出：`views/admin/EnrollmentManagePage.vue`
- [ ] **P0** 实现 FinancePage.vue
  - 依赖：AdminLayout, api/admin.js
  - 产出：`views/admin/FinancePage.vue`

---

## Phase 9: 后端测试

- [ ] **P0** 管理员登录测试 (TC-LOGIN-01 ~ TC-LOGIN-08)
  - 依赖：AdminAuthController 完成
  - 产出：`AdminAuthControllerTest.java`
- [ ] **P0** 课程 CRUD 测试 (TC-COURSE-01 ~ TC-COURSE-08)
  - 依赖：AdminCourseController, PublicCourseController 完成
  - 产出：`AdminCourseControllerTest.java`, `PublicCourseControllerTest.java`
- [ ] **P0** 课程上下架测试 (TC-COURSE-09 ~ TC-COURSE-14)
  - 依赖：AdminCourseController 完成
  - 产出：包含在 `AdminCourseControllerTest.java`
- [ ] **P0** 访客课程列表测试 (TC-COURSE-15 ~ TC-COURSE-19)
  - 依赖：PublicCourseController 完成
  - 产出：`PublicCourseControllerTest.java`
- [ ] **P0** 报名提交测试 (TC-ENROLL-01 ~ TC-ENROLL-10)
  - 依赖：PublicEnrollmentController 完成
  - 产出：`PublicEnrollmentControllerTest.java`
- [ ] **P0** 报名状态修改测试 (TC-STATUS-01 ~ TC-STATUS-10)
  - 依赖：AdminEnrollmentController 完成
  - 产出：`AdminEnrollmentControllerTest.java`
- [ ] **P0** 缴费登记测试 (TC-PAY-01 ~ TC-PAY-10)
  - 依赖：AdminPaymentController 完成
  - 产出：`AdminPaymentControllerTest.java`
- [ ] **P0** 仪表盘统计测试 (TC-DASH-01 ~ TC-DASH-07)
  - 依赖：AdminDashboardController 完成
  - 产出：`AdminDashboardControllerTest.java`
- [ ] **P0** 财务汇总测试 (TC-FIN-01 ~ TC-FIN-04)
  - 依赖：AdminFinanceController 完成
  - 产出：`AdminFinanceControllerTest.java`
- [ ] **P1** Service 层单元测试
  - 依赖：所有 Service 完成
  - 产出：各 `*ServiceImplTest.java`

---

## Phase 10: 黑盒集成测试

- [ ] **P0** 完整报名-缴费流程集成测试 (Section 8.1 of test-cases.md)
  - 依赖：前后端全部完成
  - 产出：端到端流程验证记录
- [ ] **P1** 访客端 UI 流程验证
  - 依赖：Phase 7 完成
  - 产出：首页->课程列表->课程详情->报名 完整流程截图
- [ ] **P1** 管理员端 UI 流程验证
  - 依赖：Phase 8 完成
  - 产出：登录->仪表盘->课程管理->报名管理->财务管理 完整流程截图

---

## Phase 11: 收尾与文档更新

- [ ] **P0** 运行后端全量测试 (`mvn test`)
  - 依赖：Phase 9 完成
  - 产出：测试通过报告
- [ ] **P0** 运行前端测试 (`pnpm test`) 和 lint (`pnpm lint`)
  - 依赖：Phase 7, 8 完成
  - 产出：测试和 lint 通过报告
- [ ] **P0** 更新 CHANGELOG.md
  - 依赖：所有功能完成
  - 产出：CHANGELOG 中记录所有已实现功能
- [ ] **P1** 更新 README.md（如有需要）
  - 依赖：全部完成
  - 产出：README 反映实际项目状态
- [ ] **P2** 部署验证（按 deployment.md 操作）
  - 依赖：全部完成
  - 产出：部署成功记录

---

## 依赖关系总览

```
Phase 1 (初始化)
    ├── Phase 2 (DTO/VO) ──► Phase 3 (Entity + Service)
    │                            ├── Phase 4 (Mapper)
    │                            └── Phase 5 (Controller)
    │                                   ├── Phase 9 (后端测试)
    │                                   └── Phase 10 (集成测试)
    └── Phase 6 (前端基础) ──► Phase 7 (访客页面)
                                  └── Phase 8 (管理员页面)
                                         └── Phase 10 (集成测试)
                                                  └── Phase 11 (收尾)
```

> **并行提示**：Phase 2 完成后，前端 (Phase 6/7/8) 可与后端 (Phase 3/4/5) 并行开发，
> 因为前端只需要 DTO/VO 契约定义，不需要实际后端实现。

---

## 注意事项（Code Review 记录）

> 以下为 Phase 1 Code Review 发现的问题，需在后续阶段修复。

### 安全问题

- [ ] **JWT Secret 硬编码**：`application.yml` 中 `jwt.secret` 应改为环境变量注入（Phase 3 AuthService 时处理）
  - 当前值：`course-manager-jwt-secret-key-2026-must-be-at-least-256-bits`
  - 目标：`${JWT_SECRET:default-dev-only-key}`
- [ ] **数据库密码硬编码**：`application.yml` 中 `spring.datasource.password: root` 应改为环境变量
  - 目标：`${DB_PASSWORD:root}`

### 代码健壮性

- [ ] **前端 `getUser()` 缺少异常处理**：`localStorage` 中若存储了非法 JSON，`JSON.parse` 会抛异常
  - 文件：`frontend/src/utils/auth.js`
  - 修复：添加 try-catch，解析失败返回 null
- [ ] **前端 `request.js` 响应拦截器**：假设所有响应都是 `{code, message, data}` 格式
  - 风险：文件下载、健康检查等非标准响应会误判为错误
  - 修复：对 `responseType === 'blob'` 等场景做豁免

### 待补充

- [ ] **404 路由**：`router/index.js` 缺少 catch-all 路由（`/:pathMatch(.*)*`），需添加 404 页面
- [ ] **生产日志级别**：`application.yml` 中 `log-impl: StdOutImpl` 生产环境应改为 Slf4j

---

## 进度统计

| 阶段 | 总任务数 | 已完成 | 进度 |
|------|----------|--------|------|
| Phase 1: 项目初始化 | 5 | 5 | 100% |
| Phase 2: DTO/VO 契约 | 7 | 7 | 100% |
| Phase 3: Entity + Service | 7 | 7 | 100% |
| Phase 4: Mapper | 5 | 0 | 0% |
| Phase 5: Controller | 10 | 0 | 0% |
| Phase 6: 前端基础 | 7 | 7 | 100% |
| Phase 7: 访客页面 | 4 | 0 | 0% |
| Phase 8: 管理页面 | 5 | 0 | 0% |
| Phase 9: 后端测试 | 10 | 0 | 0% |
| Phase 10: 集成测试 | 3 | 0 | 0% |
| Phase 11: 收尾 | 5 | 0 | 0% |
| **合计** | **68** | **26** | **38.2%** |
