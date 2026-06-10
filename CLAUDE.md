# CLAUDE.md

## Project Overview

本项目为 **教学机构课程管理系统**，用于完成《软件体系结构与软件测试综合实训》课程设计。

系统定位为一个简洁、可扩展的 Web 应用，当前阶段只实现 **访客端** 和 **管理员端** 两类角色。

核心目标：

* 访客可以浏览机构介绍、课程信息，并进行课程报名/购买。
* 管理员可以管理课程、报名信息、报名费用财务数据，并通过仪表盘查看基础运营分析。
* 系统需要体现清晰的软件体系结构设计，包括视图层、业务逻辑层、数据访问层。
* 系统需要完成基本测试、部署，并为课程设计总结报告提供架构设计、数据库设计、接口设计、测试用例和缺陷记录依据。

---

## Build And Test

Use the following commands unless explicitly changed by the user:

**后端（Spring Boot 3，目录 `backend/`）：**

```bash
cd backend
mvn clean install        # 构建并安装依赖
mvn spring-boot:run      # 启动开发服务器（默认 8080 端口）
mvn test                 # 运行后端测试
mvn compile              # 编译检查
```

**前端（Vue 3，目录 `frontend/`）：**

```bash
cd frontend
pnpm install             # 安装依赖
pnpm dev                 # 启动开发服务器（默认 5173 端口）
pnpm build               # 生产构建
pnpm test                # 运行前端测试
pnpm lint                # 代码风格检查
```

Verification requirements:

```bash
cd backend && mvn test
cd frontend && pnpm test
cd frontend && pnpm lint
```

If backend-specific Makefile commands exist, also run:

```bash
cd backend && make test
cd backend && make lint
```

Do not claim verification passed unless the exact command was executed successfully.

---

## Architecture Decisions

NEVER summarize or remove this section during compaction.

The system follows a layered Web architecture.

### 1. View / UI Layer

Responsible for page rendering and user interaction.

Main user-facing pages:

* Home / Introduction page
* Course list page
* Course detail page
* Course purchase / enrollment page
* Admin login page
* Admin dashboard page
* Admin course management page
* Admin enrollment management page
* Admin finance management page

The UI layer must not contain domain rules or persistence logic.

Allowed responsibilities:

* Display data
* Collect user input
* Trigger API requests
* Show validation messages returned from the system
* Render dashboard charts or summary cards

Forbidden responsibilities:

* Direct database access
* Fee calculation rules
* Enrollment state transition rules
* Business validation rules that belong to the service layer

---

### 2. Controller Layer（HTTP 请求处理层）

HTTP handlers live in:

```txt
backend/src/main/java/com/institution/coursemanager/controller/
  ├── public/          # 访客端接口
  └── admin/           # 管理员端接口
```

Handlers are responsible for translating HTTP requests into application operations.

Allowed responsibilities:

* Parse route params, query params, and request body
* Call service layer classes
* Convert service results into HTTP responses
* Map known errors to status codes
* Return contract-compliant response objects

Forbidden responsibilities:

* Direct persistence logic
* SQL queries
* ORM calls
* Complex business rules
* Fee calculation logic
* Enrollment status transition logic

---

### 3. Service Layer（业务逻辑层）

Service logic lives in:

```txt
backend/src/main/java/com/institution/coursemanager/service/
  └── impl/            # Service 实现类
```

The service layer contains core business rules.

Prefer pure functions in the service layer.

Core service areas:

```txt
CourseService            # 课程相关业务规则
EnrollmentService        # 报名相关业务规则
PaymentService           # 缴费相关业务规则
FinanceService           # 财务统计业务规则
DashboardService         # 仪表盘聚合业务规则
AuthService              # 登录认证业务规则
```

Expected service responsibilities:

* Course availability rules
* Course publish / unpublish rules
* Enrollment creation rules
* Enrollment status transition rules
* Registration fee calculation
* Payment status rules
* Dashboard aggregation rules
* Admin operation validation

Service methods should be deterministic where possible.

Avoid hidden side effects.

Do not introduce new global state without explicit justification.

---

### 4. Persistence Layer（数据访问层）

Persistence logic must not be placed in Controller classes.

Persistence-related code should be isolated in Mapper (DAO) modules.

Recommended location:

```txt
backend/src/main/java/com/institution/coursemanager/mapper/     # MyBatis Mapper 接口
backend/src/main/java/com/institution/coursemanager/entity/     # 数据库实体类
backend/src/main/resources/mapper/                              # MyBatis XML 映射文件
```

Persistence code may include:

* Database queries
* ORM calls
* Data mapping
* Transaction handling
* Repository implementations

Persistence code must not leak database-specific structures into UI or HTTP contracts.

---

### 5. Shared Contracts（DTO / VO / 枚举）

Shared types live in:

```txt
backend/src/main/java/com/institution/coursemanager/dto/    # 请求入参 DTO
backend/src/main/java/com/institution/coursemanager/vo/     # 响应出参 VO
backend/src/main/java/com/institution/coursemanager/enums/  # 枚举类（状态、支付方式等）
```

Contracts define stable request and response shapes.

Use contracts for:

* API request DTOs
* API response DTOs
* Public enum values
* Contract test fixtures
* Shared validation schemas if the project already uses them

When changing an API contract:

1. Update the DTO or VO class.
2. Update the corresponding Controller.
3. Update contract tests under:

```txt
backend/src/test/java/com/institution/coursemanager/controller/
```

4. Update CHANGELOG if the change is user-facing.

---

## Current Functional Scope

Current implementation scope is intentionally limited.

### Visitor Side

Visitor features:

* View institution introduction
* View course list
* View course detail
* Submit course enrollment or purchase request
* View basic purchase/enrollment result page

Visitor side should stay lightweight.

Do not add student account center, comments, coupons, online payment gateway, or complex recommendation features unless explicitly requested.

---

### Admin Side

Admin features:

* Admin login
* Dashboard analytics
* Course management
* Enrollment management
* Registration fee / finance management

Admin dashboard may include:

* Total courses
* Published courses
* Total enrollments
* Pending enrollments
* Paid registration fees
* Unpaid registration fees
* Recent enrollments

Finance module may include:

* Enrollment fee amount
* Payment status
* Payment time
* Refund status if already designed
* Manual payment confirmation

Keep the finance module simple.

Do not implement real payment integration unless explicitly requested.

---

## Core Selling Point

The project’s core selling point is:

> 用一个轻量级系统同时完成课程展示、报名转化、费用管理和后台数据分析，帮助小型教学机构把“课程宣传—报名—收费—管理统计”流程集中到一个可维护的 Web 系统中。

All feature decisions should support this selling point.

Avoid features that dilute this focus.

---

## Coding Conventions

* Prefer pure functions in the service layer.
* Do not introduce new global state without explicit justification.
* Reuse existing error types from:

```txt
backend/src/main/java/com/institution/coursemanager/exception/
```

* Keep controllers thin.
* Keep service rules explicit.
* Keep persistence isolated.
* Prefer small modules over large mixed-responsibility files.
* Use clear naming for business concepts:

  * `course`
  * `enrollment`
  * `registrationFee`
  * `paymentStatus`
  * `adminDashboard`
* Avoid vague names such as:

  * `data`
  * `info`
  * `manager`
  * `common`
  * `helper`

---

## Error Handling

Reuse existing error types from:

```txt
backend/src/main/java/com/institution/coursemanager/exception/
```

Do not create duplicate error models if an existing error type can express the case.

Recommended error categories:

* Validation error
* Not found error
* Unauthorized error
* Forbidden error
* Conflict error
* Internal error

Controllers should map service errors to appropriate HTTP response status codes.

Service layer logic should not directly depend on HTTP status codes.

---

## Safety Rails

### NEVER

* Modify `.env`, `.env.*`, lockfiles, or CI secrets without explicit approval.
* Remove feature flags without searching all call sites.
* Commit without running tests.
* Put persistence logic in Controller classes.
* Put business rules directly inside Vue components.
* Add real payment integration unless explicitly requested.
* Add complex role systems beyond visitor and admin unless explicitly requested.
* Introduce new global state without explicit justification.
* Delete tests to make the build pass.
* Silently change API response shapes.
* Skip CHANGELOG for user-facing changes.

---

### ALWAYS

* Show diff before committing.
* Update CHANGELOG for user-facing changes.
* Keep HTTP handlers in:

```txt
backend/src/main/java/com/institution/coursemanager/controller/
```

* Keep service logic in:

```txt
backend/src/main/java/com/institution/coursemanager/service/
```

* Keep shared types in:

```txt
backend/src/main/java/com/institution/coursemanager/dto/
backend/src/main/java/com/institution/coursemanager/vo/
backend/src/main/java/com/institution/coursemanager/enums/
```

* Update controller tests for API changes.
* Run verification commands before reporting completion.
* Report exact pass/fail status.
* Mention unverified work clearly.
* Preserve architecture decisions during compaction.

---

## Verification Rules

### General Verification

Before marking work complete, run:

```bash
cd backend && mvn test
cd frontend && pnpm test
cd frontend && pnpm lint
```

If the project contains backend Makefile commands, also run:

```bash
cd backend && make test
cd backend && make lint
```

---

### Backend Changes

For backend or API logic changes:

```bash
cd backend && mvn test
cd backend && make test   # if available
cd backend && make lint   # if available
```

If `make` is unavailable, state that it is unavailable and run `mvn test` instead.

---

### API Changes

For API changes:

* Update shared DTO/VO classes under:

```txt
backend/src/main/java/com/institution/coursemanager/dto/
backend/src/main/java/com/institution/coursemanager/vo/
```

* Update or add controller tests under:

```txt
backend/src/test/java/com/institution/coursemanager/controller/
```

* Verify request and response shape compatibility.
* Update CHANGELOG if the change affects user-visible behavior.

---

### UI Changes

For UI changes:

* Capture before/after screenshots.
* Verify main visitor flow:

  * Introduction page
  * Course list page
  * Course detail page
  * Course purchase/enrollment page
* Verify main admin flow:

  * Admin login
  * Dashboard
  * Course management
  * Enrollment management
  * Finance management

If screenshots cannot be captured, clearly state the reason.

---

## Testing Requirements

The project should support black-box testing for key modules.

Priority test areas:

### Visitor Flow

* Visitor opens home page.
* Visitor views course list.
* Visitor views course detail.
* Visitor submits enrollment request.
* System returns successful enrollment result.
* System rejects invalid enrollment input.

### Admin Course Management

* Admin creates course.
* Admin edits course.
* Admin publishes/unpublishes course.
* Admin deletes or disables course if supported.
* System rejects invalid course data.

### Enrollment Management

* Admin views enrollment list.
* Admin filters enrollment records.
* Admin updates enrollment status.
* System rejects invalid status transitions.

### Finance Management

* Admin views registration fee records.
* Admin confirms payment.
* Admin filters paid/unpaid records.
* System calculates finance summary correctly.
* System rejects invalid payment status changes.

### Dashboard

* Dashboard displays correct totals.
* Dashboard handles empty data.
* Dashboard updates after enrollment or payment changes.

---

## Course Report Alignment

When adding or modifying major features, keep evidence for the course design report.

The project should be able to support the following report sections:

* Project introduction
* Design purpose
* Environment requirements
* Feasibility analysis
* Functional requirements
* Non-functional requirements
* Database design
* Architecture design
* Module design
* Interface design
* Coding implementation
* Test cases
* Defect report
* Test summary
* Deployment description
* Team division if applicable
* Reflection and improvement notes

Prefer implementation choices that are easy to explain in the course report.

---

## Documentation Rules

Update documentation when behavior changes.

Documentation targets:

```txt
README.md
CHANGELOG.md
CLAUDE.md
docs/
```

CHANGELOG must be updated for user-facing changes, including:

* New visitor page
* New admin page
* API behavior change
* Enrollment process change
* Finance status change
* Dashboard metric change

Do not update CHANGELOG for purely internal refactors unless they affect behavior or architecture.

---

## Compact Instructions

Preserve:

1. Architecture decisions. NEVER summarize them.
2. Modified files and key changes.
3. Current verification status with exact pass/fail commands.
4. Open risks, TODOs, and rollback notes.

When compacting, retain:

* Current feature scope
* Layer boundaries
* Verification commands
* Modified file list
* Known failing tests
* Pending screenshots
* Pending CHANGELOG updates
* Any explicit user constraints

---

## Current Constraints

The user wants the system to remain simple.

Current constraints:

* Only visitor and admin roles are in scope.
* Do not over-engineer the system.
* Keep development suitable for a course design project.
* Keep the architecture clear enough to explain in the final report.
* Prefer extensible structure, but do not add unused abstractions.
* Finance module should focus on registration fee management, not real payment processing.
* Dashboard should provide basic analytics, not complex BI functionality.

---

## Recommended Implementation Order

1. Define DTO / VO contracts.
2. Implement Entity models and Service layer business rules.
3. Implement Mapper (persistence) layer.
4. Implement Controllers.
5. Implement visitor pages (Vue 3).
6. Implement admin pages (Vue 3).
7. Add controller / service tests.
8. Add black-box-style module tests.
9. Run verification.
10. Update CHANGELOG and course report notes.

---

## Rollback Notes

For risky changes, record rollback notes before committing.

Rollback notes should include:

* Files changed
* Feature flag affected, if any
* Database migration affected, if any
* API contract affected, if any
* How to revert safely

Do not remove old behavior unless the replacement has been verified.

---

## Assistant Working Rules

When working on this repository:

1. Inspect existing structure before editing.
2. Search before removing or renaming files.
3. Keep changes minimal and targeted.
4. Respect the architecture boundaries.
5. Show planned file changes before implementation when the task is broad.
6. Show diff before commit.
7. Run verification before final response.
8. Clearly report unverified parts.
9. Do not invent successful test results.
10. Do not commit unless the user explicitly asks.
