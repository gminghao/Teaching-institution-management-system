# 测试用例文档

## 1. 测试策略

本项目采用黑盒测试为主，覆盖以下核心模块：

- 管理员登录
- 课程管理（CRUD + 上下架）
- 访客课程展示
- 报名订单提交与状态管理
- 缴费登记与财务统计
- 仪表盘统计

测试方法：等价类划分、边界值分析、状态转换法。

---

## 2. 管理员登录

| 编号 | 测试场景 | 前置条件 | 输入 | 预期结果 |
|------|----------|----------|------|----------|
| TC-LOGIN-01 | 正确账号密码登录 | 数据库存在 admin 账号 | username=admin, password=admin123 | code=200, 返回 token 和用户信息 |
| TC-LOGIN-02 | 错误密码 | 数据库存在 admin 账号 | username=admin, password=wrong | code=401, message="用户名或密码错误" |
| TC-LOGIN-03 | 不存在的用户名 | - | username=nobody, password=admin123 | code=401, message="用户名或密码错误" |
| TC-LOGIN-04 | 用户名为空 | - | username="", password=admin123 | code=400, message="用户名不能为空" |
| TC-LOGIN-05 | 密码为空 | - | username=admin, password="" | code=400, message="密码不能为空" |
| TC-LOGIN-06 | 未登录访问后台接口 | 未携带 Token | GET /api/admin/courses | code=401 |
| TC-LOGIN-07 | 使用过期 Token | Token 已过期 | Authorization: Bearer {expired_token} | code=401 |
| TC-LOGIN-08 | 使用伪造 Token | Token 签名无效 | Authorization: Bearer {fake_token} | code=401 |

---

## 3. 课程管理

### 3.1 课程 CRUD

| 编号 | 测试场景 | 前置条件 | 输入 | 预期结果 |
|------|----------|----------|------|----------|
| TC-COURSE-01 | 新增课程（正常） | 已登录 | 完整课程信息 | code=200, 返回课程数据, status=DRAFT |
| TC-COURSE-02 | 新增课程（标题为空） | 已登录 | title="" | code=400 |
| TC-COURSE-03 | 新增课程（价格为负） | 已登录 | price=-100 | code=400 |
| TC-COURSE-04 | 新增课程（分类不存在） | 已登录 | categoryId=99999 | code=404 |
| TC-COURSE-05 | 修改课程标题 | 课程已存在 | title="新标题" | code=200, 列表显示新标题 |
| TC-COURSE-06 | 修改不存在的课程 | - | id=99999 | code=404 |
| TC-COURSE-07 | 查询课程详情（访客端） | 课程已上架 | GET /api/public/courses/{id} | code=200, 返回课程详情 |
| TC-COURSE-08 | 查询未上架课程详情（访客端） | 课程为 DRAFT | GET /api/public/courses/{id} | code=404 |

### 3.2 课程上下架

| 编号 | 测试场景 | 前置条件 | 操作 | 预期结果 |
|------|----------|----------|------|----------|
| TC-COURSE-09 | 上架 DRAFT 课程 | status=DRAFT | PUT /online | code=200, status=ONLINE |
| TC-COURSE-10 | 上架 OFFLINE 课程 | status=OFFLINE | PUT /online | code=200, status=ONLINE |
| TC-COURSE-11 | 重复上架 ONLINE 课程 | status=ONLINE | PUT /online | code=409 |
| TC-COURSE-12 | 下架 ONLINE 课程 | status=ONLINE | PUT /offline | code=200, status=OFFLINE |
| TC-COURSE-13 | 下架 DRAFT 课程 | status=DRAFT | PUT /offline | code=409 |
| TC-COURSE-14 | 下架 OFFLINE 课程（重复） | status=OFFLINE | PUT /offline | code=409 |

### 3.3 课程列表（访客端）

| 编号 | 测试场景 | 前置条件 | 输入 | 预期结果 |
|------|----------|----------|------|----------|
| TC-COURSE-15 | 查看课程列表 | 有 ONLINE 和 DRAFT 课程 | GET /api/public/courses | 只返回 ONLINE 课程 |
| TC-COURSE-16 | 按分类筛选 | 有多个分类的课程 | categoryId=1 | 只返回该分类的课程 |
| TC-COURSE-17 | 关键词搜索 | 有标题含"Java"的课程 | keyword=Java | 返回匹配课程 |
| TC-COURSE-18 | 分页查询 | 有 25 条课程 | pageNum=1, pageSize=10 | 返回 10 条, total=25 |
| TC-COURSE-19 | 空结果搜索 | - | keyword=不存在的关键词 | list=[], total=0 |

---

## 4. 报名订单

### 4.1 提交报名

| 编号 | 测试场景 | 前置条件 | 输入 | 预期结果 |
|------|----------|----------|------|----------|
| TC-ENROLL-01 | 正常报名 | 课程已上线 | 有效 courseId + 姓名 + 手机号 | code=200, 返回 orderNo |
| TC-ENROLL-02 | 手机号格式错误 | - | phone="123" | code=400 |
| TC-ENROLL-03 | 手机号为空 | - | studentPhone="" | code=400 |
| TC-ENROLL-04 | 姓名为空 | - | studentName="" | code=400 |
| TC-ENROLL-05 | 姓名超长 | - | studentName=51字 | code=400 |
| TC-ENROLL-06 | 课程不存在 | - | courseId=99999 | code=404 |
| TC-ENROLL-07 | 课程未上线 | 课程为 DRAFT | courseId=DRAFT课程ID | code=409 |
| TC-ENROLL-08 | 报名费快照 | 课程 registration_fee=500 | 正常报名 | 订单中 registration_fee=500 |
| TC-ENROLL-09 | 订单编号唯一性 | - | 连续提交两次报名 | 生成两个不同的 orderNo |
| TC-ENROLL-10 | 初始状态正确 | - | 正常报名 | enrollment_status=PENDING, payment_status=UNPAID |

### 4.2 报名状态修改

| 编号 | 测试场景 | 当前状态 | 目标状态 | 预期结果 |
|------|----------|----------|----------|----------|
| TC-STATUS-01 | 待处理→已联系 | PENDING | CONTACTED | code=200 |
| TC-STATUS-02 | 已联系→已报名 | CONTACTED | ENROLLED | code=200 |
| TC-STATUS-03 | 待处理→已取消 | PENDING | CANCELLED | code=200 |
| TC-STATUS-04 | 已联系→已取消 | CONTACTED | CANCELLED | code=200 |
| TC-STATUS-05 | 待处理→已报名（跳级） | PENDING | ENROLLED | code=409 |
| TC-STATUS-06 | 待处理→已联系→已报名（正常流程） | PENDING | ENROLLED（分两步） | 两步均 code=200 |
| TC-STATUS-07 | 已报名→已联系（终态变更） | ENROLLED | CONTACTED | code=409 |
| TC-STATUS-08 | 已取消→已联系（终态变更） | CANCELLED | CONTACTED | code=409 |
| TC-STATUS-09 | 不存在的订单 | - | id=99999 | code=404 |
| TC-STATUS-10 | 无效的目标状态 | PENDING | "INVALID" | code=400 |

---

## 5. 缴费登记

| 编号 | 测试场景 | 前置条件 | 输入 | 预期结果 |
|------|----------|----------|------|----------|
| TC-PAY-01 | 部分缴费 | 应缴 500 | amount=300 | code=200, 订单 paid_amount=300, payment_status=PARTIAL |
| TC-PAY-02 | 全额缴费（一次缴清） | 应缴 500 | amount=500 | code=200, paid_amount=500, payment_status=PAID |
| TC-PAY-03 | 多次累计缴满 | 应缴 500, 已缴 200 | amount=300 | paid_amount=500, payment_status=PAID |
| TC-PAY-04 | 超额缴费 | 应缴 500 | amount=600 | paid_amount=600, payment_status=PAID |
| TC-PAY-05 | 金额为 0 | - | amount=0 | code=400 |
| TC-PAY-06 | 金额为负 | - | amount=-100 | code=400 |
| TC-PAY-07 | 订单不存在 | - | orderId=99999 | code=404 |
| TC-PAY-08 | 支付方式必填 | - | paymentMethod="" | code=400 |
| TC-PAY-09 | 不同支付方式 | - | 分别用 CASH/WECHAT/ALIPAY/BANK | 均 code=200 |
| TC-PAY-10 | 事务一致性 | - | 缴费过程中模拟异常 | 记录未插入，订单状态不变 |

---

## 6. 仪表盘统计

| 编号 | 测试场景 | 前置条件 | 操作 | 预期结果 |
|------|----------|----------|------|----------|
| TC-DASH-01 | 空数据仪表盘 | 无任何数据 | GET /api/admin/dashboard/overview | 所有计数为 0, recentEnrollments=[] |
| TC-DASH-02 | 课程统计 | 新增 3 门课, 2 门上架 | 查看仪表盘 | totalCourses=3, onlineCourses=2 |
| TC-DASH-03 | 报名统计 | 新增 5 条报名 | 查看仪表盘 | totalEnrollments=5 |
| TC-DASH-04 | 待处理统计 | 5 条报名中 2 条已联系 | 查看仪表盘 | pendingEnrollments=3 |
| TC-DASH-05 | 缴费统计 | 3 条订单: 1 已缴, 1 部分缴, 1 未缴 | 查看仪表盘 | paidCount=1, partialCount=1, unpaidCount=1 |
| TC-DASH-06 | 最近报名列表 | 有 15 条报名记录 | 查看仪表盘 | recentEnrollments 返回 10 条 |
| TC-DASH-07 | 数据实时性 | 新增 1 条报名后立即查看 | 查看仪表盘 | totalEnrollments 加 1 |

---

## 7. 财务汇总

| 编号 | 测试场景 | 前置条件 | 操作 | 预期结果 |
|------|----------|----------|------|----------|
| TC-FIN-01 | 无缴费记录 | 无数据 | GET /api/admin/finance/summary | totalPaidAmount=0 |
| TC-FIN-02 | 有缴费记录 | 3 笔缴费共 1500 | 查看汇总 | totalPaidAmount=1500 |
| TC-FIN-03 | 混合状态 | 5 条订单各种状态 | 查看汇总 | 各计数字段正确 |
| TC-FIN-04 | 汇总金额精度 | 缴费 100.50 + 200.30 | 查看汇总 | totalPaidAmount=300.80 |

---

## 8. 业务流程集成测试

### 8.1 完整报名-缴费流程

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| 1 | 管理员登录 | 获取 token |
| 2 | 新增课程 | status=DRAFT |
| 3 | 上架课程 | status=ONLINE |
| 4 | 访客查看课程列表 | 能看到该课程 |
| 5 | 访客提交报名 | 返回 orderNo, 状态 PENDING |
| 6 | 管理员查看报名列表 | 能看到该订单 |
| 7 | 管理员修改状态为 CONTACTED | 状态变更成功 |
| 8 | 管理员登记部分缴费 300 | payment_status=PARTIAL |
| 9 | 管理员登记剩余缴费 200 | payment_status=PAID |
| 10 | 查看仪表盘 | 数据全部正确更新 |
| 11 | 查看财务汇总 | paidAmount 正确 |
