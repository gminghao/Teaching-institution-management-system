# Phase 3 并行开发计划

> 生成于 2026-06-11
> 当前进度：Phase 1-2 已完成（17.6%）
> 目标：前后端并行开发，完成 Phase 3-8

---

## 1. 开发策略

### 并行分工

| 角色 | 负责模块 | 执行者 |
|------|----------|--------|
| 后端 | Phase 3 (Entity + Service) → Phase 4 (Mapper) → Phase 5 (Controller) → Phase 9 (测试) | Codex |
| 前端 | Phase 6 (基础设施) → Phase 7 (访客页面) → Phase 8 (管理页面) | Gemini |

### 依赖关系

```
Phase 2 (DTO/VO) ✅ 已完成
    ├── 后端: Phase 3 → Phase 4 → Phase 5 → Phase 9
    └── 前端: Phase 6 → Phase 7 → Phase 8
                                    ↓
                            Phase 10 (集成联调)
```

**关键约束**：
- 前端开发使用 DTO/VO 契约定义的接口格式，可用 Mock 数据
- 后端完成 Controller 后，前端进行接口联调
- 每阶段结束后提交报告，确认无阻塞问题再进入下一阶段

---

## 2. 后端开发计划（Codex）

### Phase 3: Entity 模型 + Service 业务逻辑

**目标**：实现核心业务层，包含实体模型和业务规则

#### 3.1 Entity 模型类（5 个文件）

| 文件 | 说明 | 关键字段 |
|------|------|----------|
| `entity/AdminUser.java` | 管理员实体 | id, username, password, realName, status |
| `entity/CourseCategory.java` | 课程分类实体 | id, name, sortOrder, status |
| `entity/Course.java` | 课程实体 | id, categoryId, title, subtitle, coverImage, description, teacherName, price, registrationFee, status, deleted |
| `entity/EnrollmentOrder.java` | 报名订单实体 | id, orderNo, courseId, courseTitle, studentName, studentPhone, studentEmail, remark, registrationFee, paidAmount, paymentStatus, enrollmentStatus |
| `entity/PaymentRecord.java` | 缴费记录实体 | id, orderId, amount, paymentMethod, paymentTime, operatorName, remark |

**技术要求**：
- 使用 `@TableName`、`@TableId`、`@TableField` 注解
- 逻辑删除字段使用 `@TableLogic`
- 时间字段使用 `@TableField(fill = FieldFill.INSERT)` 等自动填充

#### 3.2 AuthService（登录认证）

| 文件 | 说明 |
|------|------|
| `service/AuthService.java` | 接口定义 |
| `service/impl/AuthServiceImpl.java` | 实现类 |

**业务规则**：
- `login(AdminLoginDTO)`: 验证用户名密码，生成 JWT Token
- `validateToken(String)`: 校验 Token 有效性，返回用户名
- 使用 BCrypt 加密密码比对
- JWT Secret 从配置读取，支持环境变量注入

#### 3.3 CourseService（课程管理）

| 文件 | 说明 |
|------|------|
| `service/CourseService.java` | 接口定义 |
| `service/impl/CourseServiceImpl.java` | 实现类 |

**业务规则**：
- `createCourse(CourseCreateDTO)`: 新增课程，状态默认 DRAFT
- `updateCourse(Long, CourseUpdateDTO)`: 修改课程，只更新非空字段
- `deleteCourse(Long)`: 逻辑删除，需检查是否有未完结报名
- `onlineCourse(Long)`: 上架，DRAFT/OFFLINE → ONLINE
- `offlineCourse(Long)`: 下架，ONLINE → OFFLINE
- `getPublicCourseList(...)`: 访客课程列表（ONLINE + 未删除）
- `getPublicCourseDetail(Long)`: 访客课程详情
- `getAdminCourseList(...)`: 管理员课程列表（含所有状态）

#### 3.4 EnrollmentService（报名管理）

| 文件 | 说明 |
|------|------|
| `service/EnrollmentService.java` | 接口定义 |
| `service/impl/EnrollmentServiceImpl.java` | 实现类 |

**业务规则**：
- `submitEnrollment(EnrollmentSubmitDTO)`: 访客报名
  - 生成订单号：EN + yyyyMMdd + 3位序号
  - 快照课程标题和报名费
  - 初始状态：PENDING，缴费状态：UNPAID
- `updateEnrollmentStatus(Long, EnrollmentStatusDTO)`: 修改报名状态
  - 不允许跳级变更（PENDING → ENROLLED 不合法）
  - 终态不可变更（ENROLLED/CANCELLED）
- `getEnrollmentList(...)`: 管理员查询报名列表

#### 3.5 PaymentService（缴费管理）

| 文件 | 说明 |
|------|------|
| `service/PaymentService.java` | 接口定义 |
| `service/impl/PaymentServiceImpl.java` | 实现类 |

**业务规则**：
- `createPayment(PaymentCreateDTO)`: 登记缴费（**事务**）
  1. 插入缴费记录
  2. 计算累计已缴金额
  3. 更新订单 paid_amount 和 payment_status
     - 累计 = 0 → UNPAID
     - 0 < 累计 < 应缴 → PARTIAL
     - 累计 ≥ 应缴 → PAID
- `getPaymentRecords(Long)`: 查询订单的缴费记录

#### 3.6 DashboardService（仪表盘）

| 文件 | 说明 |
|------|------|
| `service/DashboardService.java` | 接口定义 |
| `service/impl/DashboardServiceImpl.java` | 实现类 |

**业务规则**：
- `getOverview()`: 聚合统计
  - 课程总数、上线课程数
  - 报名总数、待处理报名数
  - 各缴费状态数量
  - 最近 10 条报名记录

#### 3.7 FinanceService（财务汇总）

| 文件 | 说明 |
|------|------|
| `service/FinanceService.java` | 接口定义 |
| `service/impl/FinanceServiceImpl.java` | 实现类 |

**业务规则**：
- `getFinanceSummary()`: 财务汇总
  - 应收总额、已收总额、未收总额
  - 各缴费状态订单数

**Phase 3 验收**：
```bash
cd backend && mvn compile
```

**Phase 3 报告模板**：
```markdown
# Phase 3 完成报告

## 完成任务
- [x] Entity 模型（5 个）
- [x] AuthService（登录认证 + JWT）
- [x] CourseService（CRUD + 上下架）
- [x] EnrollmentService（报名 + 状态流转）
- [x] PaymentService（缴费 + 事务）
- [x] DashboardService（聚合统计）
- [x] FinanceService（财务汇总）

## 新增文件
| 文件 | 说明 |
|------|------|
| ... | ... |

## 业务规则实现
- 状态流转：...
- 事务管理：...

## 验证结果
| 命令 | 结果 |
|------|------|
| mvn compile | PASS |

## 已知问题
- （如有）

## 下一步
- Phase 4: Mapper 持久层
```

---

### Phase 4: Mapper 持久层

**目标**：实现数据访问层

#### 4.1 Mapper 接口（5 个文件）

| 文件 | 说明 | 关键方法 |
|------|------|----------|
| `mapper/AdminUserMapper.java` | 管理员 Mapper | `selectByUsername(String)` |
| `mapper/CourseCategoryMapper.java` | 分类 Mapper | 继承 BaseMapper |
| `mapper/CourseMapper.java` | 课程 Mapper | `selectPublicPage(...)`, `selectAdminPage(...)` |
| `mapper/EnrollmentOrderMapper.java` | 订单 Mapper | `selectPage(...)`, `sumPaidAmount(Long)` |
| `mapper/PaymentRecordMapper.java` | 缴费 Mapper | `selectByOrderId(Long)`, `sumByOrderId(Long)` |

#### 4.2 XML 映射文件（如需复杂查询）

| 文件 | 说明 |
|------|------|
| `resources/mapper/CourseMapper.xml` | 联表查询（课程 + 分类） |
| `resources/mapper/EnrollmentOrderMapper.xml` | 联表查询（订单 + 课程） |

**技术要求**：
- 继承 `BaseMapper<T>` 获取基础 CRUD
- 复杂查询使用 XML 映射
- 分页查询使用 MyBatis-Plus `Page<T>`

**Phase 4 验收**：
```bash
cd backend && mvn compile
```

**Phase 4 报告模板**：
```markdown
# Phase 4 完成报告

## 完成任务
- [x] AdminUserMapper
- [x] CourseCategoryMapper
- [x] CourseMapper（含 XML）
- [x] EnrollmentOrderMapper（含 XML）
- [x] PaymentRecordMapper

## 新增文件
| 文件 | 说明 |
|------|------|
| ... | ... |

## 自定义 SQL 说明
- CourseMapper: 联表查询课程 + 分类名称
- EnrollmentOrderMapper: 联表查询订单 + 课程信息

## 验证结果
| 命令 | 结果 |
|------|------|
| mvn compile | PASS |

## 下一步
- Phase 5: Controller 层
```

---

### Phase 5: Controller 层

**目标**：实现 HTTP 接口

#### 5.1 基础设施（2 个文件）

| 文件 | 说明 |
|------|------|
| `exception/GlobalExceptionHandler.java` | 全局异常处理器 |
| `interceptor/AdminAuthInterceptor.java` | JWT 认证拦截器 |

**异常映射**：
| 异常类型 | HTTP 状态码 | 场景 |
|----------|-------------|------|
| ValidationException | 400 | 参数校验失败 |
| UnauthorizedException | 401 | Token 无效/过期 |
| NotFoundException | 404 | 资源不存在 |
| ConflictException | 409 | 状态不允许变更 |
| InternalException | 500 | 未知异常 |

#### 5.2 访客端 Controller（2 个文件）

| 文件 | 接口 |
|------|------|
| `controller/public/PublicCourseController.java` | `GET /api/public/courses`<br>`GET /api/public/courses/{id}` |
| `controller/public/PublicEnrollmentController.java` | `POST /api/public/enrollments` |

#### 5.3 管理员端 Controller（6 个文件）

| 文件 | 接口 |
|------|------|
| `controller/admin/AdminAuthController.java` | `POST /api/admin/auth/login` |
| `controller/admin/AdminDashboardController.java` | `GET /api/admin/dashboard/overview` |
| `controller/admin/AdminCourseController.java` | `POST /api/admin/courses`<br>`PUT /api/admin/courses/{id}`<br>`PUT /api/admin/courses/{id}/online`<br>`PUT /api/admin/courses/{id}/offline` |
| `controller/admin/AdminEnrollmentController.java` | `GET /api/admin/enrollments`<br>`PUT /api/admin/enrollments/{id}/status` |
| `controller/admin/AdminPaymentController.java` | `POST /api/admin/payments` |
| `controller/admin/AdminFinanceController.java` | `GET /api/admin/finance/summary` |

**技术要求**：
- Controller 使用 `@RestController` + `@RequestMapping`
- 参数校验使用 `@Valid` + `@Validated`
- 统一返回 `Result<T>` 格式
- 管理员接口通过拦截器校验 JWT

**Phase 5 验收**：
```bash
cd backend && mvn compile
cd backend && mvn spring-boot:run
# 手动测试接口
curl http://localhost:8080/api/public/courses
curl -X POST http://localhost:8080/api/admin/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'
```

**Phase 5 报告模板**：
```markdown
# Phase 5 完成报告

## 完成任务
- [x] GlobalExceptionHandler
- [x] AdminAuthInterceptor
- [x] PublicCourseController
- [x] PublicEnrollmentController
- [x] AdminAuthController
- [x] AdminDashboardController
- [x] AdminCourseController
- [x] AdminEnrollmentController
- [x] AdminPaymentController
- [x] AdminFinanceController

## 接口清单
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/public/courses | 课程列表 |
| ... | ... | ... |

## 接口测试结果
| 接口 | 测试方法 | 结果 |
|------|----------|------|
| 课程列表 | curl | PASS |
| ... | ... | ... |

## 验证结果
| 命令 | 结果 |
|------|------|
| mvn compile | PASS |
| mvn spring-boot:run | PASS |

## 已知问题
- （如有）

## 下一步
- 前端联调
- Phase 9: 后端测试
```

---

### Phase 9: 后端测试

**目标**：编写单元测试和集成测试

#### 9.1 测试文件清单

| 文件 | 测试用例 |
|------|----------|
| `AdminAuthControllerTest.java` | TC-LOGIN-01 ~ TC-LOGIN-08 |
| `AdminCourseControllerTest.java` | TC-COURSE-01 ~ TC-COURSE-14 |
| `PublicCourseControllerTest.java` | TC-COURSE-15 ~ TC-COURSE-19 |
| `PublicEnrollmentControllerTest.java` | TC-ENROLL-01 ~ TC-ENROLL-10 |
| `AdminEnrollmentControllerTest.java` | TC-STATUS-01 ~ TC-STATUS-10 |
| `AdminPaymentControllerTest.java` | TC-PAY-01 ~ TC-PAY-10 |
| `AdminDashboardControllerTest.java` | TC-DASH-01 ~ TC-DASH-07 |
| `AdminFinanceControllerTest.java` | TC-FIN-01 ~ TC-FIN-04 |

#### 9.2 测试用例示例

**登录测试（TC-LOGIN-01 ~ TC-LOGIN-08）**：
| 编号 | 用例 | 输入 | 预期结果 |
|------|------|------|----------|
| TC-LOGIN-01 | 正常登录 | admin/admin123 | 200, 返回 token |
| TC-LOGIN-02 | 用户名为空 | ""/admin123 | 400 |
| TC-LOGIN-03 | 密码为空 | admin/"" | 400 |
| TC-LOGIN-04 | 用户名不存在 | wrong/admin123 | 401 |
| TC-LOGIN-05 | 密码错误 | admin/wrong | 401 |
| TC-LOGIN-06 | 用户名长度超限 | 51字符/admin123 | 400 |
| TC-LOGIN-07 | 密码长度超限 | admin/101字符 | 400 |
| TC-LOGIN-08 | SQL 注入 | ' OR 1=1 --/admin123 | 401 |

**Phase 9 验收**：
```bash
cd backend && mvn test
```

**Phase 9 报告模板**：
```markdown
# Phase 9 完成报告

## 测试覆盖
| 模块 | 用例数 | 通过 | 失败 | 覆盖率 |
|------|--------|------|------|--------|
| 登录 | 8 | 8 | 0 | 100% |
| 课程 | 19 | 19 | 0 | 100% |
| ... | ... | ... | ... | ... |

## 测试结果
| 命令 | 结果 |
|------|------|
| mvn test | PASS (42 tests, 0 failures) |

## 失败用例
- （如有）

## 已知问题
- （如有）

## 下一步
- Phase 10: 集成联调
```

---

## 3. 前端开发计划（Gemini）

### Phase 6: 前端基础设施完善

**目标**：修复已知问题，完善基础功能

#### 6.1 修复项（3 个文件）

| 文件 | 问题 | 修复方案 |
|------|------|----------|
| `router/index.js` | 缺少 404 路由 | 添加 `/:pathMatch(.*)*` 路由，指向 404 页面 |
| `api/request.js` | blob 响应误判 | 对 `responseType === 'blob'` 做豁免 |
| `utils/auth.js` | getUser() 缺异常处理 | 添加 try-catch，解析失败返回 null |

#### 6.2 新增文件（1 个）

| 文件 | 说明 |
|------|------|
| `views/NotFoundPage.vue` | 404 页面 |

#### 6.3 全局样式（1 个文件）

| 文件 | 说明 |
|------|------|
| `assets/styles/main.css` | 全局变量、通用样式 |

**设计规范**：
- 主色调：`#409EFF`（Element Plus 默认蓝）
- 辅助色：`#67C23A`（成功）、`#E6A23C`（警告）、`#F56C6C`（危险）
- 字体：`-apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif`
- 圆角：`4px`（卡片）、`2px`（按钮）

**Phase 6 验收**：
```bash
cd frontend && pnpm lint
cd frontend && pnpm build
```

**Phase 6 报告模板**：
```markdown
# Phase 6 完成报告

## 完成任务
- [x] 修复 404 路由
- [x] 修复 Axios blob 响应处理
- [x] 修复 getUser() 异常处理
- [x] 新增 404 页面
- [x] 新增全局样式

## 修改文件
| 文件 | 变更 |
|------|------|
| ... | ... |

## 验证结果
| 命令 | 结果 |
|------|------|
| pnpm lint | PASS |
| pnpm build | PASS |

## 下一步
- Phase 7: 访客页面
```

---

### Phase 7: 访客端页面实现

**目标**：实现访客浏览和报名功能

#### 7.1 HomePage.vue（首页）

**布局结构**：
```
┌─────────────────────────────────────┐
│           轮播图 / Banner            │
├─────────────────────────────────────┤
│           机构简介                   │
├─────────────────────────────────────┤
│         热门课程推荐（3-4 个）        │
├─────────────────────────────────────┤
│         为什么选择我们               │
└─────────────────────────────────────┘
```

**功能要求**：
- 轮播图：展示 3-5 张图片，自动轮播
- 机构简介：文字介绍 + 图片
- 热门课程：调用 `GET /api/public/courses?pageSize=4`，展示课程卡片
- 课程卡片：封面图、标题、价格、"查看详情"按钮

**组件拆分**：
- `components/home/BannerCarousel.vue`
- `components/home/CourseCard.vue`

#### 7.2 CourseListPage.vue（课程列表）

**布局结构**：
```
┌─────────────────────────────────────┐
│  搜索框 + 分类筛选                   │
├─────────────────────────────────────┤
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐  │
│  │课程1│ │课程2│ │课程3│ │课程4│  │
│  └─────┘ └─────┘ └─────┘ └─────┘  │
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐  │
│  │课程5│ │课程6│ │课程7│ │课程8│  │
│  └─────┘ └─────┘ └─────┘ └─────┘  │
├─────────────────────────────────────┤
│           分页组件                   │
└─────────────────────────────────────┘
```

**功能要求**：
- 搜索框：按课程标题模糊搜索
- 分类筛选：下拉选择课程分类
- 课程卡片：封面图、标题、副标题、教师、价格、"查看详情"按钮
- 分页：Element Plus Pagination 组件

**API 调用**：
```javascript
// 获取课程列表
GET /api/public/courses?categoryId=&keyword=&pageNum=1&pageSize=12

// 获取分类列表（用于筛选下拉）
// 需要新增接口或从课程列表中提取
```

#### 7.3 CourseDetailPage.vue（课程详情）

**布局结构**：
```
┌─────────────────────────────────────┐
│  面包屑导航：首页 > 课程列表 > 详情   │
├─────────────────────────────────────┤
│  ┌──────────┐  ┌──────────────────┐ │
│  │          │  │ 课程标题          │ │
│  │ 封面图   │  │ 副标题            │ │
│  │          │  │ 教师：xxx         │ │
│  │          │  │ 价格：¥xxxx       │ │
│  │          │  │ 报名费：¥xxx      │ │
│  │          │  │ [立即报名]        │ │
│  └──────────┘  └──────────────────┘ │
├─────────────────────────────────────┤
│  课程介绍（富文本）                   │
└─────────────────────────────────────┘
```

**功能要求**：
- 面包屑导航
- 课程信息展示
- "立即报名"按钮跳转到报名页面
- 课程介绍渲染（支持 HTML）

**API 调用**：
```javascript
GET /api/public/courses/{id}
```

#### 7.4 EnrollmentPage.vue（报名表单）

**布局结构**：
```
┌─────────────────────────────────────┐
│  面包屑导航：首页 > 课程列表 > 报名   │
├─────────────────────────────────────┤
│  课程信息卡片（标题、价格、报名费）    │
├─────────────────────────────────────┤
│  报名表单                            │
│  ├ 姓名 *                            │
│  ├ 手机号 *                          │
│  ├ 邮箱                              │
│  ├ 备注                              │
│  └ [提交报名]                        │
├─────────────────────────────────────┤
│  报名须知                            │
└─────────────────────────────────────┘
```

**功能要求**：
- 表单校验（姓名必填、手机号格式、邮箱格式）
- 提交成功后显示成功提示（订单号、报名费）
- 提交失败显示错误信息

**API 调用**：
```javascript
POST /api/public/enrollments
{
  "courseId": 1,
  "studentName": "李同学",
  "studentPhone": "13800138000",
  "studentEmail": "li@example.com",
  "remark": "希望周末班"
}
```

**Phase 7 验收**：
```bash
cd frontend && pnpm dev
# 手动测试流程：首页 → 课程列表 → 课程详情 → 报名
cd frontend && pnpm lint
```

**Phase 7 报告模板**：
```markdown
# Phase 7 完成报告

## 完成任务
- [x] HomePage（首页）
- [x] CourseListPage（课程列表）
- [x] CourseDetailPage（课程详情）
- [x] EnrollmentPage（报名表单）

## 页面截图
| 页面 | 截图 |
|------|------|
| 首页 | ![首页](screenshots/home.png) |
| 课程列表 | ![课程列表](screenshots/course-list.png) |
| 课程详情 | ![课程详情](screenshots/course-detail.png) |
| 报名表单 | ![报名表单](screenshots/enrollment.png) |

## 交互流程
1. 首页 → 点击"查看全部课程" → 课程列表
2. 课程列表 → 点击课程卡片 → 课程详情
3. 课程详情 → 点击"立即报名" → 报名表单
4. 报名表单 → 填写信息 → 提交 → 成功提示

## 新增组件
| 组件 | 说明 |
|------|------|
| components/home/BannerCarousel.vue | 轮播图 |
| components/home/CourseCard.vue | 课程卡片 |

## 验证结果
| 命令 | 结果 |
|------|------|
| pnpm lint | PASS |
| pnpm build | PASS |

## 已知问题
- （如有）

## 下一步
- Phase 8: 管理员页面
```

---

### Phase 8: 管理员端页面实现

**目标**：实现管理员后台功能

#### 8.1 LoginPage.vue（登录页）

**布局结构**：
```
┌─────────────────────────────────────┐
│           机构 Logo                  │
│         教学机构管理系统              │
├─────────────────────────────────────┤
│  ┌─────────────────────────────┐   │
│  │  用户名                      │   │
│  │  密码                        │   │
│  │  [登录]                      │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

**功能要求**：
- 登录表单（用户名、密码）
- 表单校验
- 登录成功存储 Token，跳转到仪表盘
- 登录失败显示错误信息

**API 调用**：
```javascript
POST /api/admin/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

#### 8.2 DashboardPage.vue（仪表盘）

**布局结构**：
```
┌─────────────────────────────────────┐
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐  │
│  │课程 │ │上线 │ │报名 │ │待处 │  │
│  │总数 │ │课程 │ │总数 │ │理   │  │
│  └─────┘ └─────┘ └─────┘ └─────┘  │
├─────────────────────────────────────┤
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐  │
│  │已缴 │ │未缴 │ │部分 │ │退款 │  │
│  │费   │ │费   │ │缴费 │ │     │  │
│  └─────┘ └─────┘ └─────┘ └─────┘  │
├─────────────────────────────────────┤
│  最近报名列表                        │
│  ┌─────────────────────────────┐   │
│  │ 订单号 | 课程 | 学员 | 状态  │   │
│  │ ...    | ...  | ...  | ...  │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

**功能要求**：
- 统计卡片（8 个）
- 最近报名列表（10 条）
- 自动刷新数据

**API 调用**：
```javascript
GET /api/admin/dashboard/overview
```

#### 8.3 CourseManagePage.vue（课程管理）

**布局结构**：
```
┌─────────────────────────────────────┐
│  [新增课程]  搜索框  分类筛选  状态筛选│
├─────────────────────────────────────┤
│  ┌─────────────────────────────┐   │
│  │ ID | 标题 | 分类 | 价格 | 状态 | 操作 │
│  │ 1  | Java | 编程 | 2999 | 上线 | 编辑 上架/下架 删除 │
│  │ ... | ... | ... | ... | ... | ... │
│  └─────────────────────────────┘   │
├─────────────────────────────────────┤
│           分页组件                   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  新增/编辑课程对话框                  │
│  ├ 分类 *                            │
│  ├ 标题 *                            │
│  ├ 副标题                            │
│  ├ 封面图 URL                        │
│  ├ 课程介绍                          │
│  ├ 授课教师                          │
│  ├ 课程价格 *                        │
│  ├ 报名费 *                          │
│  └ [取消] [确定]                     │
└─────────────────────────────────────┘
```

**功能要求**：
- 课程列表（分页、搜索、筛选）
- 新增课程对话框
- 编辑课程对话框
- 上架/下架操作
- 删除操作（需确认）

**API 调用**：
```javascript
GET /api/admin/courses?pageNum=1&pageSize=10&keyword=&categoryId=&status=
POST /api/admin/courses
PUT /api/admin/courses/{id}
PUT /api/admin/courses/{id}/online
PUT /api/admin/courses/{id}/offline
```

#### 8.4 EnrollmentManagePage.vue（报名管理）

**布局结构**：
```
┌─────────────────────────────────────┐
│  搜索框  报名状态筛选  缴费状态筛选   │
├─────────────────────────────────────┤
│  ┌─────────────────────────────┐   │
│  │ 订单号 | 课程 | 学员 | 手机 | 报名费 | 已缴 | 报名状态 | 缴费状态 | 操作 │
│  │ ...    | ...  | ...  | ...  | ...   | ...  | ...     | ...     | 修改状态 │
│  └─────────────────────────────┘   │
├─────────────────────────────────────┤
│           分页组件                   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  修改报名状态对话框                   │
│  ├ 当前状态：PENDING                 │
│  ├ 新状态：[CONTACTED] [CANCELLED]  │
│  └ [取消] [确定]                     │
└─────────────────────────────────────┘
```

**功能要求**：
- 订单列表（分页、搜索、筛选）
- 修改报名状态（根据当前状态显示可选项）
- 状态流转规则：
  - PENDING → CONTACTED / CANCELLED
  - CONTACTED → ENROLLED / CANCELLED
  - ENROLLED → 终态
  - CANCELLED → 终态

**API 调用**：
```javascript
GET /api/admin/enrollments?pageNum=1&pageSize=10&keyword=&enrollmentStatus=&paymentStatus=
PUT /api/admin/enrollments/{id}/status
```

#### 8.5 FinancePage.vue（财务管理）

**布局结构**：
```
┌─────────────────────────────────────┐
│  财务汇总卡片                        │
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐  │
│  │应收 │ │已收 │ │未收 │ │退款 │  │
│  │总额 │ │总额 │ │总额 │ │总额 │  │
│  └─────┘ └─────┘ └─────┘ └─────┘  │
├─────────────────────────────────────┤
│  搜索框  缴费状态筛选                 │
├─────────────────────────────────────┤
│  缴费记录列表                        │
│  ┌─────────────────────────────┐   │
│  │ 订单号 | 课程 | 学员 | 应缴 | 已缴 | 状态 | 操作 │
│  │ ...    | ...  | ...  | ...  | ...  | ...  | 登记缴费 │
│  └─────────────────────────────┘   │
├─────────────────────────────────────┤
│           分页组件                   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  登记缴费对话框                       │
│  ├ 订单信息（只读）                   │
│  ├ 缴费金额 *                        │
│  ├ 支付方式 *                        │
│  ├ 缴费时间                          │
│  ├ 经办人                            │
│  ├ 备注                              │
│  └ [取消] [确定]                     │
└─────────────────────────────────────┘
```

**功能要求**：
- 财务汇总卡片（4 个）
- 缴费记录列表（分页、筛选）
- 登记缴费对话框
- 缴费状态自动计算

**API 调用**：
```javascript
GET /api/admin/finance/summary
GET /api/admin/enrollments?pageNum=1&pageSize=10&paymentStatus=
POST /api/admin/payments
```

**Phase 8 验收**：
```bash
cd frontend && pnpm dev
# 手动测试流程：登录 → 仪表盘 → 课程管理 → 报名管理 → 财务管理
cd frontend && pnpm lint
```

**Phase 8 报告模板**：
```markdown
# Phase 8 完成报告

## 完成任务
- [x] LoginPage（登录页）
- [x] DashboardPage（仪表盘）
- [x] CourseManagePage（课程管理）
- [x] EnrollmentManagePage（报名管理）
- [x] FinancePage（财务管理）

## 页面截图
| 页面 | 截图 |
|------|------|
| 登录 | ![登录](screenshots/login.png) |
| 仪表盘 | ![仪表盘](screenshots/dashboard.png) |
| 课程管理 | ![课程管理](screenshots/course-manage.png) |
| 报名管理 | ![报名管理](screenshots/enrollment-manage.png) |
| 财务管理 | ![财务管理](screenshots/finance.png) |

## 交互流程
1. 登录页 → 输入账号密码 → 登录成功 → 仪表盘
2. 仪表盘 → 查看统计数据
3. 侧边栏 → 课程管理 → 新增/编辑/上下架/删除
4. 侧边栏 → 报名管理 → 查看列表/修改状态
5. 侧边栏 → 财务管理 → 查看汇总/登记缴费

## 验证结果
| 命令 | 结果 |
|------|------|
| pnpm lint | PASS |
| pnpm build | PASS |

## 已知问题
- （如有）

## 下一步
- 前后端联调
```

---

## 4. 阶段协调与报告

### 报告提交时机

| 阶段 | 提交时间 | 接收方 |
|------|----------|--------|
| Phase 3 | Entity + Service 完成后 | 主线程 |
| Phase 4 | Mapper 完成后 | 主线程 |
| Phase 5 | Controller 完成后 | 主线程 + 前端（开始联调） |
| Phase 6 | 前端基础完善后 | 主线程 |
| Phase 7 | 访客页面完成后 | 主线程 |
| Phase 8 | 管理页面完成后 | 主线程 |
| Phase 9 | 后端测试完成后 | 主线程 |

### 联调时机

- **Phase 5 完成后**：后端接口可用，前端开始联调
- **Phase 8 完成后**：前端页面完成，进行完整联调
- **联调问题**：记录到报告中，由对应端修复

### 进度追踪

更新 `docs/implementation-plan.md` 中的进度统计表：

```markdown
## 进度统计

| 阶段 | 总任务数 | 已完成 | 进度 |
|------|----------|--------|------|
| Phase 1: 项目初始化 | 5 | 5 | 100% |
| Phase 2: DTO/VO 契约 | 7 | 7 | 100% |
| Phase 3: Entity + Service | 7 | 7 | 100% |
| Phase 4: Mapper | 5 | 5 | 100% |
| Phase 5: Controller | 10 | 10 | 100% |
| Phase 6: 前端基础 | 7 | 7 | 100% |
| Phase 7: 访客页面 | 4 | 4 | 100% |
| Phase 8: 管理页面 | 5 | 5 | 100% |
| Phase 9: 后端测试 | 10 | 10 | 100% |
| Phase 10: 集成测试 | 3 | 0 | 0% |
| Phase 11: 收尾 | 5 | 0 | 0% |
| **合计** | **68** | **60** | **88.2%** |
```

---

## 5. 验证命令汇总

### 后端验证

```bash
# 编译检查
cd backend && mvn compile

# 运行测试
cd backend && mvn test

# 启动服务
cd backend && mvn spring-boot:run
```

### 前端验证

```bash
# 安装依赖
cd frontend && pnpm install

# 代码检查
cd frontend && pnpm lint

# 构建检查
cd frontend && pnpm build

# 启动开发服务器
cd frontend && pnpm dev
```

---

## 6. 注意事项

### 后端注意

1. **事务管理**：PaymentService 的缴费登记必须使用 `@Transactional`
2. **状态流转**：EnrollmentService 的状态变更必须校验规则
3. **逻辑删除**：CourseService 的删除必须检查关联订单
4. **异常处理**：使用自定义异常类，不要直接抛 RuntimeException
5. **安全配置**：JWT Secret 使用环境变量，不要硬编码

### 前端注意

1. **API 调用**：使用 `api/public.js` 和 `api/admin.js` 封装
2. **Token 管理**：使用 `utils/auth.js` 工具函数
3. **表单校验**：使用 Element Plus 表单校验规则
4. **错误处理**：统一处理 API 错误响应
5. **响应式布局**：适配不同屏幕尺寸

### 通用注意

1. **命名规范**：遵循项目既有命名风格
2. **代码注释**：关键业务逻辑添加注释
3. **Git 提交**：每阶段完成后提交，提交信息清晰
4. **文档更新**：完成阶段后更新 `CHANGELOG.md`

---

## 7. 风险与应对

| 风险 | 影响 | 应对方案 |
|------|------|----------|
| 前端等待后端接口 | 前端开发阻塞 | 使用 Mock 数据先行开发 |
| 接口契约不一致 | 联调失败 | 严格按 DTO/VO 契约实现 |
| 状态流转规则复杂 | 业务逻辑错误 | 编写单元测试覆盖 |
| 事务管理不当 | 数据不一致 | 使用 @Transactional 注解 |
| 前端样式不统一 | UI 体验差 | 使用 Element Plus 组件库 |

---

> **文档版本**：v1.0
> **生成时间**：2026-06-11
> **维护者**：ABS
