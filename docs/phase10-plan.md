# Phase 10: 前后端集成联调计划

> 生成于 2026-06-12
> 更新于 2026-06-12
> 当前进度：74/78 (94.9%)
> 目标：前端页面对接后端 API，完成端到端集成测试

---

## 1. 任务概览

### 核心目标

前端页面从 mock 数据切换到真实后端 API 调用，完成前后端联调。

### 任务分解

| 序号 | 任务 | 优先级 | 状态 |
|------|------|--------|------|
| 10.1 | LoginPage 对接登录 API | P0 | ✅ 已完成 |
| 10.2 | DashboardPage 对接仪表盘 API | P0 | ✅ 已完成 |
| 10.3 | CourseManagePage 对接课程 CRUD API | P0 | ✅ 已完成 |
| 10.4 | EnrollmentManagePage 对接报名 API | P0 | ✅ 已完成 |
| 10.5 | FinancePage 对接财务 API | P0 | ✅ 已完成 |
| 10.6 | 访客页面对接公开 API | P1 | ✅ 已完成 |
| 10.7 | 端到端流程验证 | P0 | ⏳ 待执行 |

---

## 2. 详细任务说明

### 10.1 LoginPage 对接登录 API

**目标**：替换 mock token，调用真实登录接口

**修改文件**：`frontend/src/views/admin/LoginPage.vue`

**当前代码**：
```javascript
const handleLogin = () => {
  setToken('mock-admin-token')
  setUser({ username: form.username, realName: '陈管理员' })
  router.push(redirect)
}
```

**目标代码**：
```javascript
import { login } from '@/api/admin'
import { ElMessage } from 'element-plus'

const handleLogin = async () => {
  try {
    const res = await login({ username: form.username, password: form.password })
    if (res.code === 200) {
      setToken(res.data.token)
      setUser({ username: res.data.username, realName: res.data.realName })
      ElMessage.success('登录成功')
      router.push(redirect)
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  }
}
```

**验收标准**：
- 输入 admin/admin123 登录成功
- 输入错误密码显示错误提示
- 登录后跳转到仪表盘

---

### 10.2 DashboardPage 对接仪表盘 API

**目标**：替换 mock 数据，调用 `getDashboardOverview()`

**修改文件**：`frontend/src/views/admin/DashboardPage.vue`

**关键变更**：
1. 移除 mock 数据导入
2. 添加 `onMounted` 调用 API
3. 用 API 返回数据填充统计卡片和最近报名列表

**API 调用**：
```javascript
import { getDashboardOverview } from '@/api/admin'

const overview = ref({
  totalCourses: 0,
  onlineCourses: 0,
  totalEnrollments: 0,
  pendingEnrollments: 0,
  paidCount: 0,
  unpaidCount: 0,
  partialCount: 0,
  recentEnrollments: []
})

onMounted(async () => {
  const res = await getDashboardOverview()
  if (res.code === 200) {
    overview.value = res.data
  }
})
```

**验收标准**：
- 页面加载后显示真实统计数据
- 最近报名列表显示真实数据
- 空数据时显示 0 和空列表

---

### 10.3 CourseManagePage 对接课程 CRUD API

**目标**：替换 mock 数据，调用课程管理 API

**修改文件**：`frontend/src/views/admin/CourseManagePage.vue`

**API 调用**：
```javascript
import { getAdminCourses, createCourse, updateCourse, onlineCourse, offlineCourse } from '@/api/admin'

// 加载课程列表
const loadCourses = async () => {
  const res = await getAdminCourses({
    pageNum: currentPage.value,
    pageSize: 10,
    keyword: searchKeyword.value,
    categoryId: filterCategory.value,
    status: filterStatus.value
  })
  if (res.code === 200) {
    courses.value = res.data.list
    total.value = res.data.total
  }
}

// 新增课程
const handleCreate = async () => {
  const res = await createCourse(form)
  if (res.code === 200) {
    ElMessage.success('新增成功')
    dialogVisible.value = false
    loadCourses()
  }
}

// 编辑课程
const handleUpdate = async () => {
  const res = await updateCourse(editingId.value, form)
  if (res.code === 200) {
    ElMessage.success('修改成功')
    dialogVisible.value = false
    loadCourses()
  }
}

// 上架
const handleOnline = async (id) => {
  const res = await onlineCourse(id)
  if (res.code === 200) {
    ElMessage.success('上架成功')
    loadCourses()
  }
}

// 下架
const handleOffline = async (id) => {
  const res = await offlineCourse(id)
  if (res.code === 200) {
    ElMessage.success('下架成功')
    loadCourses()
  }
}
```

**验收标准**：
- 课程列表分页正常
- 新增课程成功后列表刷新
- 编辑课程成功后列表刷新
- 上架/下架操作正常
- 搜索和筛选功能正常

---

### 10.4 EnrollmentManagePage 对接报名 API

**目标**：替换 mock 数据，调用报名管理 API

**修改文件**：`frontend/src/views/admin/EnrollmentManagePage.vue`

**API 调用**：
```javascript
import { getEnrollments, updateEnrollmentStatus } from '@/api/admin'

// 加载报名列表
const loadEnrollments = async () => {
  const res = await getEnrollments({
    pageNum: currentPage.value,
    pageSize: 10,
    keyword: searchKeyword.value,
    enrollmentStatus: filterEnrollmentStatus.value,
    paymentStatus: filterPaymentStatus.value
  })
  if (res.code === 200) {
    enrollments.value = res.data.list
    total.value = res.data.total
  }
}

// 修改报名状态
const handleStatusUpdate = async (id, newStatus) => {
  const res = await updateEnrollmentStatus(id, { status: newStatus })
  if (res.code === 200) {
    ElMessage.success('状态更新成功')
    statusDialogVisible.value = false
    loadEnrollments()
  }
}
```

**验收标准**：
- 报名列表分页正常
- 搜索和筛选功能正常
- 状态修改功能正常
- 状态流转规则正确（PENDING→CONTACTED→ENROLLED/CANCELLED）

---

### 10.5 FinancePage 对接财务 API

**目标**：替换 mock 数据，调用财务 API

**修改文件**：`frontend/src/views/admin/FinancePage.vue`

**API 调用**：
```javascript
import { getFinanceSummary, getEnrollments, createPayment } from '@/api/admin'

// 加载财务汇总
const loadSummary = async () => {
  const res = await getFinanceSummary()
  if (res.code === 200) {
    summary.value = res.data
  }
}

// 加载缴费记录
const loadRecords = async () => {
  const res = await getEnrollments({
    pageNum: currentPage.value,
    pageSize: 10,
    paymentStatus: filterPaymentStatus.value
  })
  if (res.code === 200) {
    records.value = res.data.list
    total.value = res.data.total
  }
}

// 登记缴费
const handlePayment = async () => {
  const res = await createPayment({
    orderId: selectedOrder.value.id,
    amount: paymentForm.amount,
    paymentMethod: paymentForm.paymentMethod,
    remark: paymentForm.remark
  })
  if (res.code === 200) {
    ElMessage.success('缴费登记成功')
    paymentDialogVisible.value = false
    loadSummary()
    loadRecords()
  }
}
```

**验收标准**：
- 财务汇总卡片显示正确数据
- 缴费记录列表分页正常
- 登记缴费功能正常
- 缴费后汇总数据自动更新

---

### 10.6 访客页面对接公开 API

**目标**：替换 mock 数据，调用公开 API

**修改文件**：
- `frontend/src/views/public/HomePage.vue`
- `frontend/src/views/public/CourseListPage.vue`
- `frontend/src/views/public/CourseDetailPage.vue`
- `frontend/src/views/public/EnrollmentPage.vue`

**API 调用**：
```javascript
import { getCourses, getCourseDetail, submitEnrollment } from '@/api/public'

// HomePage - 获取热门课程
const hotCourses = ref([])
onMounted(async () => {
  const res = await getCourses({ pageSize: 4 })
  if (res.code === 200) {
    hotCourses.value = res.data.list
  }
})

// CourseListPage - 获取课程列表
const loadCourses = async () => {
  const res = await getCourses({
    pageNum: currentPage.value,
    pageSize: 12,
    keyword: searchKeyword.value,
    categoryId: filterCategory.value
  })
  if (res.code === 200) {
    courses.value = res.data.list
    total.value = res.data.total
  }
}

// CourseDetailPage - 获取课程详情
const course = ref({})
onMounted(async () => {
  const res = await getCourseDetail(route.params.id)
  if (res.code === 200) {
    course.value = res.data
  }
})

// EnrollmentPage - 提交报名
const handleSubmit = async () => {
  const res = await submitEnrollment({
    courseId: route.params.courseId,
    studentName: form.name,
    studentPhone: form.phone,
    studentEmail: form.email,
    remark: form.remark
  })
  if (res.code === 200) {
    ElMessage.success('报名成功')
    submitted.value = true
    orderNo.value = res.data.orderNo
  }
}
```

**验收标准**：
- 首页显示热门课程
- 课程列表分页正常
- 课程详情显示完整信息
- 报名表单提交成功
- 搜索和筛选功能正常

---

### 10.7 端到端流程验证

**目标**：验证完整业务流程

**测试流程**（参考 test-cases.md Section 8.1）：

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| 1 | 管理员登录 | 获取 token，跳转仪表盘 |
| 2 | 新增课程 | status=DRAFT，列表显示 |
| 3 | 上架课程 | status=ONLINE |
| 4 | 访客查看课程列表 | 能看到该课程 |
| 5 | 访客提交报名 | 返回 orderNo，状态 PENDING |
| 6 | 管理员查看报名列表 | 能看到该订单 |
| 7 | 管理员修改状态为 CONTACTED | 状态变更成功 |
| 8 | 管理员登记部分缴费 300 | payment_status=PARTIAL |
| 9 | 管理员登记剩余缴费 200 | payment_status=PAID |
| 10 | 查看仪表盘 | 数据全部正确更新 |
| 11 | 查看财务汇总 | paidAmount 正确 |

**验收方法**：
1. 启动后端：`cd backend && mvn spring-boot:run`
2. 启动前端：`cd frontend && pnpm dev`
3. 按步骤手动测试
4. 记录测试结果

---

## 3. 技术要点

### 3.1 API 响应处理

所有 API 返回格式统一为 `Result<T>`：
```javascript
{
  code: 200,        // 200=成功, 400=参数错误, 401=未认证, 404=不存在, 409=冲突, 500=服务器错误
  message: "success",
  data: { ... }
}
```

前端处理模式：
```javascript
const res = await apiCall()
if (res.code === 200) {
  // 处理成功
} else {
  ElMessage.error(res.message)
}
```

### 3.2 错误处理

- 401：Token 过期，跳转登录页
- 400/404/409：显示错误消息
- 500/网络错误：显示"网络错误"

已在 `api/request.js` 的响应拦截器中处理 401。

### 3.3 加载状态

使用 `v-loading` 指令显示加载状态：
```javascript
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    // API 调用
  } finally {
    loading.value = false
  }
}
```

### 3.4 空数据处理

列表为空时显示提示：
```html
<el-empty v-if="list.length === 0" description="暂无数据" />
```

---

## 4. 执行顺序

```
10.1 LoginPage（必须先完成，获取 token）
    ↓
10.2 DashboardPage
10.3 CourseManagePage
10.4 EnrollmentManagePage
10.5 FinancePage
    ↓（并行）
10.6 访客页面（无需登录）
    ↓
10.7 端到端流程验证
```

---

## 5. 验证命令

```bash
# 后端编译
cd backend && mvn compile

# 后端测试
cd backend && mvn test

# 前端 lint
cd frontend && pnpm lint

# 前端构建
cd frontend && pnpm build

# 启动后端
cd backend && mvn spring-boot:run

# 启动前端
cd frontend && pnpm dev
```

---

## 6. 风险与应对

| 风险 | 应对方案 |
|------|----------|
| API 接口格式不匹配 | 检查 DTO/VO 定义，与前端期望对比 |
| CORS 跨域问题 | 已配置 Vite proxy，开发环境无此问题 |
| Token 过期 | 响应拦截器已处理 401，自动跳转登录 |
| 数据库无数据 | 首次启动自动插入 admin 用户和默认分类 |

---

## 7. 完成标准

- [x] 所有页面使用真实 API 数据
- [x] 登录流程正常
- [x] 课程 CRUD 正常
- [x] 报名管理正常
- [x] 缴费登记正常
- [x] 仪表盘数据正确
- [x] 财务汇总正确
- [x] 访客页面正常
- [ ] 端到端流程通过
- [x] pnpm lint PASS
- [x] pnpm build PASS
- [ ] mvn test PASS
