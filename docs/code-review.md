# 代码审查报告

> 审查时间：2026-06-11
> 审查范围：Phase 3-8 全部代码

---

## 一、总体概述

项目后端架构设计规范，分层清晰，异常处理体系完整。但存在以下核心问题：

1. **前端完全未与后端对接**（所有页面使用 mock 数据）
2. **安全配置需要改进**（JWT 密钥、数据库凭据）
3. **性能问题**（全表加载、N+1 查询）
4. **正确性问题**（并发控制、参数顺序）

---

## 二、严重问题（P0 - 必须修复）

### 2.1 前端登录绕过后端认证

**文件**: `frontend/src/views/admin/LoginPage.vue`

```javascript
// 当前实现（错误）
const handleLogin = () => {
  setToken('mock-admin-token')  // 硬编码假 Token
  setUser({ username: form.username, realName: '陈管理员' })
  router.push(redirect)
}

// 应改为
const handleLogin = async () => {
  const res = await login(form)  // 调用后端接口
  setToken(res.data.token)
  setUser({ username: res.data.username, realName: res.data.realName })
  router.push(redirect)
}
```

### 2.2 前端页面全部使用 Mock 数据

**涉及文件**:
- `DashboardPage.vue` - 使用 `mockEnrollments`
- `CourseManagePage.vue` - 使用 `mockCourses`
- `EnrollmentManagePage.vue` - 硬编码 `rows`
- `FinancePage.vue` - 使用 `mockTransactions`
- `CourseListPage.vue` - 使用 `mockCourses`
- `CourseDetailPage.vue` - 使用 `mockCourses`
- `EnrollmentPage.vue` - 不调用 API

### 2.3 财务汇总全表加载

**文件**: `backend/.../service/impl/FinanceServiceImpl.java`

```java
// 当前实现（错误）
List<EnrollmentOrder> orders = list();  // 全表加载到内存

// 应改为 SQL 聚合查询
SELECT 
  SUM(registration_fee) as total_fee,
  SUM(paid_amount) as total_paid,
  COUNT(*) as total_count,
  SUM(CASE WHEN payment_status = 'PAID' THEN 1 ELSE 0 END) as paid_count
FROM enrollment_order
```

### 2.4 缴费接口缺少并发控制

**文件**: `backend/.../service/impl/PaymentServiceImpl.java`

需要添加乐观锁或悲观锁防止并发金额错误。

### 2.5 参数顺序错误

**文件**: `backend/.../controller/admin/AdminEnrollmentController.java`

```java
// 当前实现（参数顺序错误）
return Result.success(enrollmentService.getEnrollmentList(
    pageNum, pageSize, keyword, paymentStatus, enrollmentStatus));

// 应改为（与 Service 方法签名一致）
return Result.success(enrollmentService.getEnrollmentList(
    pageNum, pageSize, keyword, enrollmentStatus, paymentStatus));
```

---

## 三、安全问题（P1）

| 问题 | 文件 | 修复方案 |
|------|------|----------|
| JWT 密钥硬编码 | `application.yml` | 改为环境变量 `${JWT_SECRET}` |
| 数据库密码明文 | `application.yml` | 改为环境变量 `${DB_PASSWORD}` |
| CORS 配置过宽 | `WebMvcConfig.java` | 限制具体域名 |
| Token 存储 localStorage | `utils/auth.js` | 考虑 httpOnly Cookie |
| 拦截器吞没异常 | `AdminAuthInterceptor.java` | 添加日志记录 |

---

## 四、性能问题（P1）

| 问题 | 文件 | 修复方案 |
|------|------|----------|
| N+1 查询 | `CourseServiceImpl.java` | 批量查询或缓存分类 |
| 分页无上限 | `CourseServiceImpl.java` | 添加 `Math.min(pageSize, 100)` |
| SQL 日志输出 | `application.yml` | 改为 Slf4j |

---

## 五、代码质量问题（P2）

| 问题 | 文件 | 修复方案 |
|------|------|----------|
| Service 继承不当 | 多个 Service | 改为构造函数注入 |
| 工具方法重复 | 多个 Service | 抽取到工具类 |
| 前端死代码 | `api/*.js` | 对接或移除 |

---

## 六、修复优先级

### 立即修复（P0）
1. ✅ 修复参数顺序错误
2. ⏳ 前端对接后端 API（Phase 10 集成测试）
3. ⏳ 财务汇总改用 SQL 聚合

### 计划修复（P1）
1. ⏳ JWT 密钥环境变量化
2. ⏳ 数据库凭据环境变量化
3. ⏳ 添加并发控制

### 后续优化（P2）
1. ⏳ 代码重构
2. ⏳ 性能优化

---

## 七、结论

后端架构设计良好，但前端未与后端对接是当前最大问题。安全和性能问题需要在生产部署前修复。
