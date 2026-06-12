# 管理员模块完整修复计划

> 编制日期: 2026-06-12
> 依据: admin-module-audit-report.md + frontend-admin-fix-plan.md
> 范围: 前端管理员页面 + 后端管理员 API 全链路

---

## 一、修复总览

| 阶段 | 优先级 | 内容 | 任务数 | 预计工时 |
|------|--------|------|--------|---------|
| 阶段 1 | 高 | 前端核心交互修复 | 12 | 2-3h |
| 阶段 2 | 高 | 后端 API 补全 | 6 | 1-2h |
| 阶段 3 | 中 | 前端操作功能补全 | 10 | 3-4h |
| 阶段 4 | 低 | 代码质量改进 | 5 | 1-2h |
| **合计** | — | — | **33** | **7-11h** |

---

## 二、阶段 1：前端核心交互修复（高优先级）

> 目标：让已有的数据展示真正可交互

### 1.1 AdminLayout 全局修复

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 1.1.1 | 移除 mock.js 依赖 | `AdminLayout.vue` | 删除 `import { referenceImages } from '@/data/mock'`，头像改为 CSS 字母头像 `<span>{{ username[0] }}</span>` + 背景色 |
| 1.1.2 | 搜索框改为真实输入 | `AdminLayout.vue` | `<div class="header-search">` → `<input>` + `v-model` + `@keyup.enter`，回车跳转到当前子页面并携带 `?keyword=xxx` |

#### 验收标准

- [ ] 头像显示管理员姓名首字母，不依赖静态图片
- [ ] 搜索框可输入，回车后 URL 带 `?keyword=xxx`
- [ ] mock.js 不再被 AdminLayout 引用

#### 边界控制

- 若 mock.js 被其他模块引用，仅删除 AdminLayout 的 import，不删除文件
- 搜索跳转仅限当前子路由，不实现全局搜索

---

### 1.2 DashboardPage 修复

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 1.2.1 | 重命名误导变量 | `DashboardPage.vue` | `mockEnrollments` → `recentEnrollments` |
| 1.2.2 | 修复字段映射 | `DashboardPage.vue` | `item.date` → `item.createTime` |
| 1.2.3 | "View All" 绑定跳转 | `DashboardPage.vue` | `@click="$router.push('/admin/enrollments')"` |
| 1.2.4 | 移除硬编码 trend 文案 | `DashboardPage.vue` | `trend` 统一为空字符串 `''`，不显示 "Requires attention" 等误导文案 |
| 1.2.5 | 趋势图改为 CSS 柱状图 | `DashboardPage.vue` | 用 `paidCount / unpaidCount / partialCount` 驱动简单 CSS 柱状图，不依赖图表库 |

#### 验收标准

- [ ] 指标卡片数据全部来自 API，无硬编码文案
- [ ] 最近报名列表正确显示订单号、学生、课程、日期、状态
- [ ] "View All" 可点击跳转到报名管理页
- [ ] 无 mock 命名残留

#### 边界控制

- CSS 柱状图仅做简单可视化，不引入 ECharts 等重依赖
- trend 字段如后端未返回，前端不自行计算

---

### 1.3 搜索/筛选输入框绑定

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 1.3.1 | 课程搜索绑定 | `CourseManagePage.vue` | `v-model="searchKeyword"` 绑定到 `<input>`，`watch(searchKeyword)` + 300ms debounce → `loadCourses()` |
| 1.3.2 | 课程状态筛选绑定 | `CourseManagePage.vue` | `v-model="filterStatus"` 绑定到 `<select>`，`@change="loadCourses()"` |
| 1.3.3 | 课程分类筛选绑定 | `CourseManagePage.vue` | `v-model="filterCategory"` 绑定到 `<select>`，`onMounted` 调用分类 API 加载选项 |
| 1.3.4 | 报名搜索绑定 | `EnrollmentManagePage.vue` | `v-model="searchKeyword"` 绑定，新增 `searchStudentName` ref |
| 1.3.5 | 报名状态筛选绑定 | `EnrollmentManagePage.vue` | `v-model="filterEnrollmentStatus"` 绑定，选项使用后端枚举值 |
| 1.3.6 | 新增缴费状态筛选 | `EnrollmentManagePage.vue` | 下拉：全部 / 未缴费(UNPAID) / 部分缴费(PARTIAL) / 已缴费(PAID) / 已退款(REFUNDED) |
| 1.3.7 | 财务搜索绑定 | `FinancePage.vue` | 新增搜索 ref 并接入 `loadRecords` |

#### 验收标准

- [ ] 输入关键词后 300ms 自动搜索，列表更新
- [ ] 选择状态下拉后立即筛选，列表更新
- [ ] 分类下拉从后端加载，选择后筛选生效
- [ ] 报名状态/缴费状态筛选选项与后端枚举一致

#### 边界控制

- debounce 仅用于搜索框，下拉筛选使用 `@change` 立即触发
- 分类 API 失败时下拉显示空，不阻塞页面加载

---

### 1.4 分页实现

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 1.4.1 | 课程管理分页 | `CourseManagePage.vue` | 新增 `<el-pagination>`，绑定 `currentPage` / `total`，`@current-change` / `@size-change` 触发 `loadCourses()` |
| 1.4.2 | 报名管理分页 | `EnrollmentManagePage.vue` | 替换静态分页为 `<el-pagination>`，footer 显示 API 返回的 `total` 而非 `rows.length` |
| 1.4.3 | 财务管理分页 | `FinancePage.vue` | 替换静态分页为 `<el-pagination>`，footer 显示 `共 {{ total }} 条` |

#### 验收标准

- [ ] 三个页面均可翻页，每页默认 10 条
- [ ] 翻页后列表数据正确刷新
- [ ] 记录数显示真实 total，无硬编码

#### 边界控制

- 分页参数通过 API 传递，前端不缓存全量数据
- pageSize 可选值限制为 [10, 20, 50]

---

### 1.5 FinancePage 硬编码修复

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 1.5.1 | 移除/重写趋势图 | `FinancePage.vue` | 删除 `bars` 硬编码数据和 CSS 图表，改为从 `summary` API 数据驱动的 CSS 柱状图（paid/unpaid/partial 计数对比） |
| 1.5.2 | 修复记录数硬编码 | `FinancePage.vue` | `共 1,248 条` → `共 {{ total }} 条` |
| 1.5.3 | 修复日期硬编码 | `FinancePage.vue` | `2026年06月11日` → 动态 `new Date().toLocaleDateString('zh-CN')` |
| 1.5.4 | 删除经办人列 | `FinancePage.vue` | 后端不返回 `tx.adminUsername`，删除"经办人"列 |

#### 验收标准

- [ ] 统计卡片数据来自 API，无硬编码
- [ ] 日期显示为当天日期
- [ ] 记录数显示真实 total
- [ ] 无未定义变量引用（bars）

#### 边界控制

- 趋势图如后端无数据支持，仅展示静态对比柱状图，不自行生成时序数据
- 日期格式使用 `zh-CN` locale，不硬编码格式字符串

---

## 三、阶段 2：后端 API 补全（高优先级）

> 目标：暴露已实现但未连接的 Service 方法

### 2.1 新增缺失端点

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 2.1.1 | 课程详情端点 | `AdminCourseController.java` | 添加 `GET /{id}`，调用 `courseService.getAdminCourseDetail(id)`，返回 `Result<AdminCourseVO>` |
| 2.1.2 | 课程删除端点 | `AdminCourseController.java` | 添加 `DELETE /{id}`，调用 `courseService.deleteCourse(id)`，返回 `Result<Void>` |
| 2.1.3 | 缴费记录查询端点 | `AdminPaymentController.java` | 添加 `GET ?orderId={orderId}`，调用 `paymentService.getPaymentRecords(orderId)`，返回 `Result<List<PaymentRecordVO>>` |
| 2.1.4 | 课程分类列表端点 | `AdminCourseController.java` | 添加 `GET /categories`，返回全部启用分类列表（id, name），供前端筛选下拉使用 |

#### 验收标准

- [ ] `GET /api/admin/courses/{id}` 返回课程详情
- [ ] `DELETE /api/admin/courses/{id}` 删除课程并返回成功
- [ ] `GET /api/admin/payments?orderId={id}` 返回缴费记录列表
- [ ] `GET /api/admin/courses/categories` 返回分类列表
- [ ] 所有端点返回统一 `Result<T>` 格式

#### 边界控制

- DELETE 端点需检查课程是否有关联报名，有则拒绝删除（返回 409 Conflict）
- 分类端点仅返回 `enabled = 1` 的分类

---

### 2.2 修复分类筛选参数

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 2.2.1 | 传递 categoryId 参数 | `CourseServiceImpl.java` | `getAdminCoursePage` 方法增加 `categoryId` 参数，传递给 `courseMapper.selectAdminPage()` 而非硬编码 `null` |

#### 验收标准

- [ ] 管理端课程列表支持按分类筛选
- [ ] `categoryId` 为 null 时返回全部分类的课程

#### 边界控制

- categoryId 无效时返回空列表，不抛异常

---

### 2.3 清理死代码

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 2.3.1 | 删除未使用 Mapper 方法 | `AdminUserMapper.java` | 删除 `selectByUsername()`（AuthService 用 LambdaQueryWrapper 替代） |
| 2.3.2 | 删除未使用 Mapper 方法 | `PaymentRecordMapper.java` | 删除 `selectByOrderId()`、`sumByOrderId()` |
| 2.3.3 | 删除未使用 Mapper 方法 | `EnrollmentOrderMapper.java` | 删除 `sumPaidAmount()` |

#### 验收标准

- [ ] 删除后 `mvn compile` 通过
- [ ] 无其他代码引用被删除的方法

#### 边界控制

- 删除前全局搜索确认无调用方
- 仅删除 Mapper 接口方法，不删除对应的 XML 映射（避免遗漏）

---

### 2.4 整合 CourseCategoryService

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 2.4.1 | Service 层整合 | `CourseServiceImpl.java` | 改用 `CourseCategoryService` 而非直接调 `CourseCategoryMapper`，或标记 Service 为 `@Deprecated` |

#### 验收标准

- [ ] CourseCategoryService 被正确使用或明确标记弃用
- [ ] 不绕过 Service 层直接调 Mapper

#### 边界控制

- 如不新增端点，可保留直接调 Mapper，但需添加注释说明

---

## 四、阶段 3：前端操作功能补全（中优先级）

> 目标：让所有按钮和操作列真正可用

### 4.1 课程新增/编辑

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 4.1.1 | 新增课程弹窗 | `CourseManagePage.vue` | el-dialog 表单，字段：categoryId, title, subtitle, teacherName, price, registrationFee, description |
| 4.1.2 | 表单校验 | `CourseManagePage.vue` | title 必填，price ≥ 0，registrationFee ≥ 0，categoryId 必选 |
| 4.1.3 | 提交调用 API | `CourseManagePage.vue` | 新增调用 `createCourse()`，编辑调用 `updateCourse(id, data)`，成功后 `loadCourses()` 刷新 |
| 4.1.4 | 编辑按钮 | `CourseManagePage.vue` | 操作列增加"编辑"按钮，复用新增弹窗组件，预填数据 |
| 4.1.5 | 删除按钮（可选） | `CourseManagePage.vue` | 操作列增加"删除"按钮，需后端先有 DELETE 端点 |

#### 验收标准

- [ ] "新增课程"弹窗可填写并提交，成功后列表刷新
- [ ] 点击"编辑"弹出预填表单，修改后提交成功
- [ ] 表单校验生效，非法输入有错误提示
- [ ] 讲师列正确显示 teacherName，无空列

#### 边界控制

- 弹窗关闭时重置表单状态
- 提交中按钮 loading，防止重复提交
- 编辑时锁定课程 ID 不可修改

---

### 4.2 报名状态修改

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 4.2.1 | 状态修改弹窗 | `EnrollmentManagePage.vue` | el-dialog 弹窗，显示当前状态，下拉选择目标状态（只展示合法转换） |
| 4.2.2 | 状态转换校验 | `EnrollmentManagePage.vue` | PENDING → CONTACTED/CANCELLED；CONTACTED → ENROLLED/CANCELLED；ENROLLED/CANCELLED → 终态不可修改 |
| 4.2.3 | 提交调用 API | `EnrollmentManagePage.vue` | 调用 `updateEnrollmentStatus(id, { enrollmentStatus })`，成功后刷新列表 |
| 4.2.4 | 操作列修正 | `EnrollmentManagePage.vue` | 操作文本不再按 index 判断，终态不显示"修改状态" |

#### 验收标准

- [ ] "修改状态"弹窗可选择合法目标状态，提交后列表刷新
- [ ] 非法状态转换被后端拒绝时有错误提示
- [ ] 终态（ENROLLED/CANCELLED）不显示"修改状态"操作
- [ ] 学生邮箱列正确显示 `studentEmail`

#### 边界控制

- 状态枚举值硬编码与后端 `EnrollmentStatus` 枚举保持一致
- 修改失败时保留弹窗，显示后端错误信息

---

### 4.3 缴费登记

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 4.3.1 | 登记缴费弹窗 | `FinancePage.vue` | el-dialog 表单：orderId（下拉选择未缴/部分缴订单）、amount、paymentMethod、operatorName |
| 4.3.2 | 表单校验 | `FinancePage.vue` | amount > 0，paymentMethod 必选 |
| 4.3.3 | 提交调用 API | `FinancePage.vue` | 调用 `createPayment()` API，成功后刷新列表和统计 |

#### 验收标准

- [ ] "登记缴费"弹窗可选择订单、填写金额和支付方式
- [ ] 提交成功后列表和统计卡片均刷新
- [ ] 表单校验生效

#### 边界控制

- 订单下拉仅显示 UNPAID/PARTIAL 状态的订单
- 金额上限为订单未缴金额，前端校验

---

### 4.4 其他按钮修复

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 4.4.1 | "View All" 跳转 | `DashboardPage.vue` | `@click="$router.push('/admin/enrollments')"` |
| 4.4.2 | "查看全部"跳转 | `FinancePage.vue` | `<a>` 改为 `@click="$router.push('/admin/enrollments')"` |
| 4.4.3 | "导出报表"处理 | `FinancePage.vue` | 标记为 `disabled` + tooltip "暂不支持" |
| 4.4.4 | 移除"新增报名"按钮 | `EnrollmentManagePage.vue` | 报名由访客提交，管理员端移除此按钮 |

#### 验收标准

- [ ] 所有链接/按钮可点击，有实际效果或明确禁用提示
- [ ] 无死链接（href="#"）

#### 边界控制

- 禁用按钮需有 tooltip 说明原因
- 移除按钮前确认无其他功能依赖

---

## 五、阶段 4：代码质量改进（低优先级）
r
### 5.1 统一 HTTP 客户端

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 5.1.1 | AdminLayout 登出 | `AdminLayout.vue` | `fetch('/api/admin/auth/logout', ...)` → `request.post('/admin/auth/logout')` |
| 5.1.2 | 空闲登出 | `useIdleTimeout.js` | 同上，改用 axios 实例 |

#### 验收标准

- [ ] 登出请求经过 axios 拦截器，token 正确携带
- [ ] 登出后正确跳转到登录页

#### 边界控制

- 改动仅涉及 HTTP 客户端，不改变登出逻辑

---

### 5.2 修正 Service 继承

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 5.2.1 | DashboardService | `DashboardService.java` + `DashboardServiceImpl.java` | 移除 `extends IService<EnrollmentOrder>`，改用直接注入 Mapper |
| 5.2.2 | FinanceService | `FinanceService.java` + `FinanceServiceImpl.java` | 同上 |

#### 验收标准

- [ ] `mvn compile` 通过
- [ ] Dashboard 和 Finance 功能正常

#### 边界控制

- 改动前确认 `count()` 和 `list()` 的调用方，替换为 Mapper 等价方法

---

### 5.3 新增生产 DDL 脚本

#### Action

| 编号 | 任务 | 文件 | 具体操作 |
|------|------|------|----------|
| 5.3.1 | MySQL DDL | `backend/src/main/resources/db/schema.sql` | 基于 `schema-h2.sql` 转写为 MySQL 语法，包含 5 张表 + seed data |

#### 验收标准

- [ ] DDL 脚本可在 MySQL 8.0 执行成功
- [ ] 包含初始管理员账号和课程分类

#### 边界控制

- 仅提供 DDL，不自动执行，需人工确认

---

## 六、需后端配合的前端任务

| 前端任务 | 依赖的后端端点 | 状态 |
|---------|---------------|------|
| 课程分类下拉筛选 | `GET /api/admin/courses/categories` | 待实现 |
| 课程新增/编辑 | `POST /api/admin/courses`、`PUT /api/admin/courses/{id}` | 已有 |
| 课程删除 | `DELETE /api/admin/courses/{id}` | 待实现 |
| 报名状态修改 | `PUT /api/admin/enrollments/{id}/status` | 已有 |
| 缴费登记 | `POST /api/admin/payments` | 已有 |
| 缴费记录查询 | `GET /api/admin/payments?orderId={id}` | 待实现 |

---

## 七、已确认正常功能（不需修复）

以下功能经审计确认已正确实现：

- [x] 管理员登录/登出（JWT 认证流程完整）
- [x] 仪表盘数据统计（6 个指标卡片均从 API 获取真实数据）
- [x] 课程列表加载（支持分页参数，真实查询数据库）
- [x] 课程上架/下架（有 click handler，调用真实 API）
- [x] 报名列表加载（支持分页和状态筛选参数）
- [x] 财务统计卡片（4 个指标均从 API 获取真实数据）
- [x] 缴费明细列表（从 API 获取真实数据）
- [x] 路由守卫（JWT 过期检测、未登录重定向）
- [x] 空闲自动登出（15 分钟无操作）
- [x] API 端点前后端路径完全匹配（11 个端点无一错位）

---

## 八、总体验收检查表

全部修复完成后执行端到端验证：

### 访客流程

- [ ] 首页正常加载
- [ ] 课程列表页正常加载
- [ ] 课程详情页正常加载
- [ ] 课程报名提交成功

### 管理员流程

- [ ] 登录 → 控制台 → 指标数据正确 → "View All" 跳转报名管理
- [ ] 课程管理 → 搜索 → 筛选 → 新增 → 上架 → 编辑 → 下架 → 分页
- [ ] 报名管理 → 筛选 → 修改状态 → 合法/非法转换校验 → 分页
- [ ] 财务管理 → 统计卡片 → 登记缴费 → 提交 → 统计刷新 → 分页

### 全局检查

- [ ] 无 mock 残留（import、变量名、硬编码数据）
- [ ] 无硬编码假数据（总数、日期、文案）
- [ ] 所有按钮可交互或有明确禁用提示
- [ ] 分页正常工作
- [ ] 无 console.error / 未捕获异常

---

## 九、风险与回滚

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 分类 API 未及时实现 | 课程管理页分类筛选不可用 | 前端降级为手动输入或暂不筛选 |
| DELETE 端点未实现 | 课程删除按钮不可用 | 按钮标记 disabled |
| 状态枚举前后端不一致 | 状态修改失败 | 硬编码与后端枚举对齐，添加注释 |
| axios 拦截器改动影响其他请求 | 登出/空闲超时异常 | 改动前备份，改动后手动测试 |

---

## 十、进度跟踪

| 批次 | 任务数 | 已完成 | 进度 |
|------|--------|--------|------|
| 阶段 1: 前端核心交互 | 12 | 12 | 100% |
| 阶段 2: 后端 API 补全 | 6 | 6 | 100% |
| 阶段 3: 前端操作功能 | 10 | 10 | 100% |
| 阶段 4: 代码质量改进 | 5 | 5 | 100% |
| **合计** | **33** | **33** | **100%** |

---

## 十一、建议执行顺序

```
阶段 1 (前端核心)  ←──── 可与阶段 2 并行 ────→  阶段 2 (后端 API)
        │                                              │
        └──────────────┐    ┌──────────────────────────┘
                       ▼    ▼
                  阶段 3 (前端操作)  ←── 依赖阶段 2 完成
                       │
                       ▼
                  阶段 4 (代码质量)  ←── 随时可做
```

**关键路径**: 阶段 2.1（后端新增端点）→ 阶段 3.1/3.3（前端弹窗调用新端点）
