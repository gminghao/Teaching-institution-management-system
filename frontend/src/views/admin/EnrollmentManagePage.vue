<template>
  <div class="enrollment-manage-page">
    <section class="manage-head">
      <div>
        <h2>报名管理</h2>
        <p>查看和管理学员课程报名及缴费情况。</p>
      </div>
      <button type="button">
        <el-icon><Plus /></el-icon>
        新增报名
      </button>
    </section>

    <section class="filter-card">
      <label>
        <el-icon><Document /></el-icon>
        <input type="text" placeholder="订单编号...">
      </label>
      <label>
        <el-icon><User /></el-icon>
        <input type="text" placeholder="学生姓名或邮箱...">
      </label>
      <select>
        <option>所有状态</option>
        <option>待处理</option>
        <option>已批准</option>
        <option>候补</option>
      </select>
      <button type="button" class="ghost-btn">
        <el-icon><Filter /></el-icon>
        筛选
      </button>
    </section>

    <section class="table-card">
      <table>
        <thead>
          <tr>
            <th>订单编号</th>
            <th>学员及联系方式</th>
            <th>课程名称</th>
            <th>费用 / 已缴</th>
            <th>缴费状态</th>
            <th>报名状态</th>
            <th class="right">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in rows" :key="item.orderNo">
            <td class="order-cell">{{ item.orderNo }}</td>
            <td>
              <div class="student-cell">
                <span>{{ item.studentName.slice(0, 1) }}</span>
                <div>
                  <strong>{{ item.studentName }}</strong>
                  <small>{{ item.email }}</small>
                </div>
              </div>
            </td>
            <td>
              <strong>{{ item.courseTitle }}</strong>
              <small class="course-code">{{ item.code }}</small>
            </td>
            <td>
              <strong>{{ item.amount }}</strong>
              <small class="paid">Paid: {{ item.paid }}</small>
            </td>
            <td>
              <span :class="['status-pill', item.paymentTone]">{{ item.paymentStatus }}</span>
            </td>
            <td>
              <span :class="['status-pill', item.enrollmentTone]">{{ item.enrollmentStatus }}</span>
            </td>
            <td class="right action-cell">{{ index === 0 ? '查看' : '修改状态' }}</td>
          </tr>
        </tbody>
      </table>
      <footer>
        <span>显示 1 到 3 条，共 142 条结果</span>
        <nav>
          <button type="button" disabled>&lt;</button>
          <button type="button" class="active">1</button>
          <button type="button">2</button>
          <button type="button">3</button>
          <span>...</span>
          <button type="button">&gt;</button>
        </nav>
      </footer>
    </section>
  </div>
</template>

<script setup>
import { Document, Filter, Plus, User } from '@element-plus/icons-vue'

const rows = [
  { orderNo: '#ORD-9821', studentName: 'Eleanor Vance', email: 'e.vance@example.com', courseTitle: 'Advanced Data Structures', code: 'CS-401 · Fall 2024', amount: '$1,200.00', paid: '$1,200.00', paymentStatus: '已结清', paymentTone: 'success', enrollmentStatus: '已批准', enrollmentTone: 'success' },
  { orderNo: '#ORD-9822', studentName: 'Theodore Hayes', email: 't.hayes@example.com', courseTitle: 'Modern European History', code: 'HIS-205 · Fall 2024', amount: '$850.00', paid: '$400.00', paymentStatus: '部分', paymentTone: 'warning', enrollmentStatus: '待处理', enrollmentTone: 'warning' },
  { orderNo: '#ORD-9823', studentName: 'Maya Lin', email: 'm.lin@example.com', courseTitle: 'Intro to Machine Learning', code: 'CS-505 · Fall 2024', amount: '$1,500.00', paid: '$0.00', paymentStatus: '未交', paymentTone: 'danger', enrollmentStatus: '候补', enrollmentTone: 'info' }
]
</script>

<style scoped>
.enrollment-manage-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.manage-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
}

.manage-head h2 {
  margin: 0 0 8px;
  color: var(--color-primary-deep);
  font-size: 34px;
}

.manage-head p {
  margin: 0;
  color: var(--color-text-muted);
}

.manage-head button,
.ghost-btn {
  height: 44px;
  padding: 0 18px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border-radius: var(--radius-control);
  cursor: pointer;
  font-weight: 800;
}

.manage-head button {
  color: #fff;
  background: var(--color-primary-deep);
  border: none;
}

.ghost-btn {
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}

.filter-card {
  padding: 20px;
  display: grid;
  grid-template-columns: minmax(230px, 1fr) minmax(260px, 1.2fr) 160px auto;
  gap: 14px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 18px;
  box-shadow: var(--shadow-subtle);
}

.filter-card label {
  position: relative;
}

.filter-card label .el-icon {
  position: absolute;
  top: 50%;
  left: 13px;
  color: var(--color-text-muted);
  transform: translateY(-50%);
}

input,
select {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  color: var(--color-text);
  background: var(--color-surface-muted);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-control);
  outline: none;
}

label input {
  padding-left: 40px;
}

.table-card {
  overflow: hidden;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 18px;
  box-shadow: var(--shadow-subtle);
}

table {
  width: 100%;
  min-width: 1050px;
  border-collapse: collapse;
}

th,
td {
  padding: 20px 24px;
  text-align: left;
  border-bottom: 1px solid var(--color-border);
}

th {
  color: var(--color-text-muted);
  background: var(--color-surface-muted);
  font-size: 13px;
}

.order-cell {
  color: var(--color-primary-deep);
  font-weight: 800;
}

.student-cell {
  display: flex;
  align-items: center;
  gap: 14px;
}

.student-cell > span {
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  background: var(--color-surface-muted);
  border-radius: var(--radius-pill);
  font-weight: 800;
}

small {
  display: block;
  margin-top: 4px;
  color: var(--color-text-muted);
}

.paid {
  color: #ff0000;
}

.course-code {
  font-size: 13px;
}

.status-pill {
  display: inline-flex;
  min-width: 54px;
  justify-content: center;
  padding: 6px 12px;
  border-radius: var(--radius-pill);
  font-size: 12px;
  font-weight: 800;
}

.status-pill.success {
  color: #047857;
  background: rgba(16, 185, 129, 0.13);
}

.status-pill.warning {
  color: #b45309;
  background: rgba(245, 158, 11, 0.15);
}

.status-pill.danger {
  color: #dc2626;
  background: rgba(239, 68, 68, 0.13);
}

.status-pill.info {
  color: #475569;
  background: rgba(100, 116, 139, 0.16);
}

.right {
  text-align: right;
}

.action-cell {
  color: var(--color-primary);
  font-weight: 800;
}

footer {
  padding: 18px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

footer span {
  color: var(--color-text-muted);
}

footer nav {
  display: flex;
  gap: 8px;
}

footer button {
  width: 40px;
  height: 40px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  cursor: pointer;
}

footer button.active {
  color: #fff;
  background: var(--color-primary-deep);
}

@media (max-width: 1050px) {
  .filter-card {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 720px) {
  .manage-head,
  footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .filter-card {
    grid-template-columns: 1fr;
  }
}
</style>
