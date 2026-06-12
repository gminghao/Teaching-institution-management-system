# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/).

## [Unreleased]

### Changed

- 前端页面从 mock 数据切换到真实后端 API 调用（Phase 10 前后端集成联调）
  - DashboardPage 对接 `getDashboardOverview()` API
  - CourseManagePage 对接 `getAdminCourses()` API
  - EnrollmentManagePage 对接 `getEnrollments()` API
  - FinancePage 对接 `getFinanceSummary()` 和 `getEnrollments()` API
  - HomePage 对接 `getCourses()` API
  - CourseListPage 移除 mock 分类引用
- 新增 `courseStatusTone()` 工具函数用于课程状态样式映射

### Added

- 初始化 Spring Boot 3 后端工程骨架（`backend/`），包含 pom.xml、application.yml、主启动类
- 初始化 Vue 3 前端工程骨架（`frontend/`），包含 package.json、vite.config.js、main.js、App.vue
- 配置数据库连接和 MyBatis-Plus（分页插件、驼峰映射、逻辑删除）
- 配置 Vite 开发代理（`/api` → 后端 8080 端口）
- 创建统一响应包装类 `Result<T>` 和 `PageResult<T>`
- 创建前端路由配置（含导航守卫）
- 创建 Axios 请求封装（Token 注入、统一错误处理）
- 创建 API 接口封装（`api/public.js`、`api/admin.js`）
- 创建工具函数（`utils/auth.js`、`utils/format.js`）
- 创建布局组件（`PublicLayout.vue`、`AdminLayout.vue`）
- 创建并实现访客端页面（首页、课程列表、课程详情、课程报名）
- 创建并实现管理员端页面（登录、控制台、课程管理、报名管理、财务管理）
- 定义枚举类（`CourseStatus`, `EnrollmentStatus`, `PaymentStatus`, `PaymentMethod`）
- 定义访客端 DTO/VO（`EnrollmentSubmitDTO`, `PublicCourseVO`, `PublicCourseDetailVO`, `EnrollmentSubmitVO`）
- 定义管理员登录 DTO/VO（`AdminLoginDTO`, `AdminLoginVO`）
- 定义管理员课程 DTO/VO（`CourseCreateDTO`, `CourseUpdateDTO`, `AdminCourseVO`）
- 定义管理员报名 DTO/VO（`EnrollmentStatusDTO`, `AdminEnrollmentVO`）
- 定义管理员缴费 DTO/VO（`PaymentCreateDTO`, `PaymentRecordVO`）
- 定义仪表盘和财务 VO（`DashboardOverviewVO`, `RecentEnrollmentVO`, `FinanceSummaryVO`）
- 实现 Entity 模型类（`AdminUser`, `CourseCategory`, `Course`, `EnrollmentOrder`, `PaymentRecord`）
- 实现 Service 接口和实现类（`AuthService`, `CourseService`, `EnrollmentService`, `PaymentService`, `DashboardService`, `FinanceService`, `CourseCategoryService`）
- 创建异常处理体系（`BusinessException`, `ValidationException`, `NotFoundException`, `UnauthorizedException`, `ConflictException`）
- 创建 JWT 工具类（`JwtUtil`）
- 完善 Mapper 接口（`AdminUserMapper`, `CourseCategoryMapper`, `CourseMapper`, `EnrollmentOrderMapper`, `PaymentRecordMapper`）
- 新增 CourseMapper.xml 联表查询映射
- 实现全局异常处理器（`GlobalExceptionHandler`）
- 实现 JWT 认证拦截器（`AdminAuthInterceptor`）
- 实现访客端 Controller（`PublicCourseController`, `PublicEnrollmentController`）
- 实现管理员端 Controller（`AdminAuthController`, `AdminDashboardController`, `AdminCourseController`, `AdminEnrollmentController`, `AdminPaymentController`, `AdminFinanceController`）
- 修复前端 404 路由（`/:pathMatch(.*)*`）
- 修复 Axios blob 响应豁免
- 修复 `getUser()` JSON 解析异常处理
- 新增 404 页面（`NotFoundPage.vue`）
- 新增全局样式文件（`assets/styles/main.css`）
- 按 `docs/设计参考` 的 Academic Distinction 设计实现 Vue 前端视觉风格
- 新增本地参考图片资源（`frontend/src/assets/reference/`），替换页面热链图片
- 新增前端参考 Mock 数据模块（`frontend/src/data/mock.js`）

### Fixed

- 修复管理员端报名和缴费状态枚举显示，统一中文标签与状态颜色映射。
- 报名状态转换校验：增加 ENROLLED 终态检查，防止跳级变更和状态回退
- 修复不支持的请求 `Content-Type` 被统一异常处理误报为 200/500 的问题，现在返回 HTTP 415。
- 修复财务汇总聚合结果在不同数据库别名大小写下读取为零的问题。

### Planned

- 数据库建表脚本（admin_user、course_category、course、enrollment_order、payment_record）
- Mapper 持久层完善（XML 映射）
- Controller 层实现
- 后端测试完善
