# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/).

## [Unreleased]

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
- 创建占位页面组件（访客端 4 页 + 管理员端 5 页）
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
- 创建 Mapper 接口（`AdminUserMapper`, `CourseCategoryMapper`, `CourseMapper`, `EnrollmentOrderMapper`, `PaymentRecordMapper`）
- 修复前端 404 路由（`/:pathMatch(.*)*`）
- 修复 Axios blob 响应豁免
- 修复 `getUser()` JSON 解析异常处理
- 新增 404 页面（`NotFoundPage.vue`）
- 新增全局样式文件（`assets/styles/main.css`）

### Fixed

- 报名状态转换校验：增加 ENROLLED 终态检查，防止跳级变更和状态回退

### Planned

- 数据库建表脚本（admin_user、course_category、course、enrollment_order、payment_record）
- Mapper 持久层完善（XML 映射）
- Controller 层实现
- 访客页面实现（HomePage, CourseListPage, CourseDetailPage, EnrollmentPage）
- 管理员页面实现（LoginPage, DashboardPage, CourseManagePage, EnrollmentManagePage, FinancePage）
- 后端测试完善
