<template>
  <div class="dashboard-page">
    <section class="admin-section-heading">
      <h2>概览</h2>
      <p>Institutional performance metrics for the current term.</p>
    </section>

    <section class="metric-grid">
      <article v-for="metric in metrics" :key="metric.label" :class="['metric-card', metric.featured ? 'featured' : '']">
        <div class="metric-icon" :class="metric.tone">
          <el-icon><component :is="metric.icon" /></el-icon>
        </div>
        <div>
          <p>{{ metric.label }}</p>
          <strong>{{ metric.value }}</strong>
          <small :class="metric.tone">{{ metric.trend }}</small>
        </div>
      </article>
    </section>

    <section class="recent-card">
      <header>
        <h3>最近报名</h3>
        <button type="button" @click="$router.push('/admin/enrollments')">View All</button>
      </header>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>订单编号</th>
              <th>学生姓名</th>
              <th>课程</th>
              <th>日期</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in recentEnrollments" :key="item.orderNo">
              <td class="link-cell">{{ item.orderNo }}</td>
              <td>{{ item.studentName }}</td>
              <td class="muted-cell">{{ item.courseTitle }}</td>
              <td class="muted-cell">{{ item.createTime }}</td>
              <td>
                <span :class="['status-pill', enrollmentStatusTone(item.enrollmentStatus)]">
                  {{ enrollmentStatusMap[item.enrollmentStatus] || item.enrollmentStatus }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Collection, DataAnalysis, Money, Reading, Timer, User } from '@element-plus/icons-vue'
import { getDashboardOverview } from '@/api/admin'
import { enrollmentStatusMap, enrollmentStatusTone } from '@/utils/format'

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

const metrics = computed(() => [
  { icon: Reading, label: '课程总数', value: String(overview.value.totalCourses), trend: '', tone: 'success' },
  { icon: Collection, label: '已上架课程', value: String(overview.value.onlineCourses), trend: '', tone: 'info' },
  { icon: User, label: '报名总数', value: String(overview.value.totalEnrollments), trend: '', tone: 'success' },
  { icon: Timer, label: '待处理事项', value: String(overview.value.pendingEnrollments), trend: '', tone: 'warning' },
  { icon: Money, label: '已缴费', value: String(overview.value.paidCount), trend: '', tone: 'success', featured: true },
  { icon: DataAnalysis, label: '未缴费', value: String(overview.value.unpaidCount), trend: '', tone: 'danger' }
])

const recentEnrollments = computed(() => overview.value.recentEnrollments || [])

onMounted(async () => {
  try {
    const res = await getDashboardOverview()
    if (res.code === 200) {
      overview.value = res.data
    }
  } catch (e) {
    console.error('Failed to load dashboard:', e)
  }
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 36px;
}

.admin-section-heading h2 {
  margin: 0 0 8px;
  color: var(--color-text);
  font-size: 34px;
  font-weight: 800;
}

.admin-section-heading p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 18px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 20px;
}

.metric-card {
  min-height: 168px;
  padding: 28px;
  display: flex;
  align-items: flex-start;
  gap: 18px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  box-shadow: var(--shadow-subtle);
}

.metric-card.featured {
  color: #fff;
  background: var(--color-primary-container);
  border-color: var(--color-primary-container);
}

.metric-icon {
  width: 46px;
  height: 46px;
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--color-surface-muted);
  border-radius: 10px;
  font-size: 23px;
}

.metric-icon.success {
  color: var(--color-success);
  background: rgba(16, 185, 129, 0.12);
}

.metric-icon.info {
  color: var(--color-info);
  background: rgba(59, 130, 246, 0.12);
}

.metric-icon.warning {
  color: var(--color-warning);
  background: rgba(245, 158, 11, 0.14);
}

.metric-icon.danger {
  color: var(--color-danger);
  background: rgba(239, 68, 68, 0.12);
}

.metric-card p {
  margin: 4px 0 22px;
  color: var(--color-text-muted);
  font-size: 14px;
  font-weight: 800;
  line-height: 1.4;
}

.metric-card.featured p,
.metric-card.featured small {
  color: rgba(255, 255, 255, 0.82);
}

.metric-card strong {
  display: block;
  margin-bottom: 16px;
  color: var(--color-text);
  font-size: 48px;
  line-height: 1;
}

.metric-card.featured strong {
  color: #fff;
}

.metric-card small {
  font-size: 14px;
  font-weight: 800;
}

.metric-card small.success {
  color: var(--color-success);
}

.metric-card small.warning {
  color: var(--color-warning);
}

.metric-card small.danger {
  color: var(--color-danger);
}

.recent-card {
  overflow: hidden;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 18px;
  box-shadow: var(--shadow-subtle);
}

.recent-card header {
  padding: 28px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.recent-card h3 {
  margin: 0;
  color: var(--color-text);
  font-size: 24px;
}

.recent-card button {
  height: 44px;
  padding: 0 24px;
  color: var(--color-text);
  background: var(--color-surface-muted);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-control);
  cursor: pointer;
  font-weight: 700;
}

.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 780px;
  border-collapse: collapse;
}

th,
td {
  padding: 18px 32px;
  text-align: left;
  border-top: 1px solid var(--color-border);
}

th {
  color: var(--color-text-muted);
  background: var(--color-surface-muted);
  font-size: 13px;
  font-weight: 800;
}

td {
  color: var(--color-text);
  font-size: 15px;
}

.link-cell {
  color: var(--color-primary);
  font-weight: 800;
}

.muted-cell {
  color: var(--color-text-muted);
}

.status-pill {
  display: inline-flex;
  padding: 5px 13px;
  border-radius: var(--radius-pill);
  font-size: 12px;
  font-weight: 800;
}

.status-pill.success {
  color: #047857;
  background: rgba(16, 185, 129, 0.12);
}

.status-pill.warning {
  color: #b45309;
  background: rgba(245, 158, 11, 0.14);
}

.status-pill.danger {
  color: #b91c1c;
  background: rgba(239, 68, 68, 0.14);
}

.status-pill.info {
  color: #475569;
  background: rgba(100, 116, 139, 0.16);
}

@media (max-width: 1280px) {
  .metric-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
