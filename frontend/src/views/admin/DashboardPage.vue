<template>
  <div class="dashboard-page">
    <section class="admin-section-heading">
      <h2>概览</h2>
      <p>本学期机构运营数据概览</p>
    </section>

    <!-- Skeleton state -->
    <template v-if="loading">
      <section class="metric-grid">
        <article v-for="i in 6" :key="'sk-'+i" class="metric-card skeleton-card">
          <div class="skeleton-icon"></div>
          <div class="skeleton-text">
            <div class="skeleton-line short"></div>
            <div class="skeleton-line long"></div>
            <div class="skeleton-line medium"></div>
          </div>
        </article>
      </section>
      <section class="recent-card skeleton-table-card">
        <header>
          <div class="skeleton-line" style="width:120px;height:24px"></div>
          <div class="skeleton-line" style="width:100px;height:36px;border-radius:8px"></div>
        </header>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th v-for="j in 5" :key="'skh-'+j">
                  <div class="skeleton-line" style="height:14px;width:60px"></div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="k in 5" :key="'skr-'+k">
                <td v-for="j in 5" :key="'skd-'+k+'-'+j">
                  <div class="skeleton-line" :style="{ width: [100,80,120,90,70][j-1]+'px' }"></div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>

    <!-- Loaded state -->
    <template v-else>
      <section class="metric-grid">
        <article
          v-for="(metric, index) in metrics"
          :key="metric.label"
          :class="['metric-card', metric.featured ? 'featured' : '']"
          :style="{ '--i': index }"
        >
          <div class="metric-icon" :class="[metric.tone, metric.pulse ? 'has-pulse' : '']">
            <el-icon><component :is="metric.icon" /></el-icon>
          </div>
          <div>
            <p>{{ metric.label }}</p>
            <strong><CountUpNumber :target="metric.rawValue" :duration="800" /></strong>
            <small :class="metric.tone">{{ metric.trend }}</small>
          </div>
        </article>
      </section>

      <section class="recent-card">
        <header>
          <h3>最近报名</h3>
          <button type="button" @click="$router.push('/admin/enrollments')">查看全部</button>
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
                  <StatusBadge
                    :status="item.enrollmentStatus"
                    :label="enrollmentStatusInfo(item.enrollmentStatus).label"
                    :tone="enrollmentStatusInfo(item.enrollmentStatus).tone"
                  />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Collection, DataAnalysis, Money, Reading, Timer, User } from '@element-plus/icons-vue'
import { getDashboardOverview } from '@/api/admin'
import { enrollmentStatusInfo } from '@/utils/format'
import CountUpNumber from '@/components/dashboard/CountUpNumber.vue'
import StatusBadge from '@/components/dashboard/StatusBadge.vue'

const loading = ref(true)

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
  { icon: Reading, label: '课程总数', rawValue: overview.value.totalCourses, trend: '', tone: 'success' },
  { icon: Collection, label: '已上架课程', rawValue: overview.value.onlineCourses, trend: '', tone: 'info' },
  { icon: User, label: '报名总数', rawValue: overview.value.totalEnrollments, trend: '', tone: 'success' },
  { icon: Timer, label: '待处理事项', rawValue: overview.value.pendingEnrollments, trend: '', tone: 'warning', pulse: overview.value.pendingEnrollments > 0 },
  { icon: Money, label: '已缴费', rawValue: overview.value.paidCount, trend: '', tone: 'success', featured: true },
  { icon: DataAnalysis, label: '未缴费', rawValue: overview.value.unpaidCount, trend: '', tone: 'danger' }
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
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.admin-section-heading {
  margin-bottom: 0;
}

.admin-section-heading h2 {
  margin: 0;
  color: var(--color-text);
  font-size: 28px;
  font-weight: 700;
  line-height: 1.3;
}

.admin-section-heading p {
  margin: 6px 0 0;
  color: var(--color-text-muted);
  font-size: 15px;
  line-height: 1.6;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.metric-card {
  min-height: 168px;
  padding: 28px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-subtle);
  display: flex;
  align-items: flex-start;
  gap: 20px;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  animation-delay: calc(var(--i) * 80ms);
}

.metric-card.featured {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-card), 0 0 0 1px rgba(15, 76, 129, 0.08);
}

.metric-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
  position: relative;
}

.metric-icon.success {
  color: var(--color-success);
  background: rgba(16, 185, 129, 0.1);
}

.metric-icon.info {
  color: var(--color-info);
  background: rgba(59, 130, 246, 0.1);
}

.metric-icon.warning {
  color: var(--color-warning);
  background: rgba(245, 158, 11, 0.1);
}

.metric-icon.danger {
  color: var(--color-danger);
  background: rgba(239, 68, 68, 0.1);
}

.metric-icon.has-pulse::after {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: 18px;
  border: 2px solid rgba(245, 158, 11, 0.35);
  animation: pulse-ring 2s ease-in-out infinite;
  pointer-events: none;
}

@keyframes pulse-ring {
  0% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.08); opacity: 0.2; }
  100% { transform: scale(1); opacity: 0.6; }
}

.metric-card p {
  margin: 0 0 6px;
  color: var(--color-text-muted);
  font-size: 14px;
  font-weight: 500;
}

.metric-card strong {
  display: block;
  margin: 0 0 6px;
  color: var(--color-text);
  font-size: 32px;
  font-weight: 800;
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}

.metric-card small {
  font-size: 13px;
  font-weight: 600;
}

.metric-card small.success { color: var(--color-success); }
.metric-card small.info { color: var(--color-info); }
.metric-card small.warning { color: var(--color-warning); }
.metric-card small.danger { color: var(--color-danger); }

.recent-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-subtle);
  overflow: hidden;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  animation-delay: 500ms;
}

.recent-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 32px 16px;
}

.recent-card header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text);
}

.recent-card header button {
  padding: 8px 20px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-control);
  background: var(--color-surface);
  color: var(--color-primary);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}

.recent-card header button:hover {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
}

.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 20px 32px;
  text-align: left;
  font-size: 14px;
  line-height: 1.5;
}

th {
  background: #f0f2fa;
  color: var(--color-text-muted);
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.01em;
  white-space: nowrap;
  border-bottom: 1px solid var(--color-border);
}

thead tr th:first-child { border-radius: 8px 0 0 0; }
thead tr th:last-child { border-radius: 0 8px 0 0; }

tbody tr {
  transition: background-color 0.2s ease, box-shadow 0.2s ease;
}

tbody tr:hover {
  background-color: rgba(15, 76, 129, 0.03);
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.04);
}

tbody td {
  border-bottom: 1px solid var(--color-border);
  color: var(--color-text-regular);
}

tbody tr:last-child td { border-bottom: none; }

.link-cell {
  color: var(--color-primary);
  font-weight: 700;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  cursor: pointer;
}

.link-cell:hover { text-decoration: underline; }

.muted-cell { color: var(--color-text-muted); }

.skeleton-card { animation: none !important; opacity: 1 !important; }

.skeleton-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: #e5e7eb;
  flex-shrink: 0;
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

.skeleton-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 8px;
}

.skeleton-line {
  height: 16px;
  background: #e5e7eb;
  border-radius: 8px;
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

.skeleton-line.short { width: 60px; }
.skeleton-line.medium { width: 100px; }
.skeleton-line.long { width: 140px; }

.skeleton-table-card { animation: none !important; opacity: 1 !important; }
.skeleton-table-card header { padding: 24px 32px 16px; }
.skeleton-table-card table th,
.skeleton-table-card table td { padding: 20px 32px; }

@media (max-width: 1280px) {
  .metric-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}

@media (max-width: 1024px) {
  .metric-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 760px) {
  .metric-grid { grid-template-columns: 1fr; }
}

@media (max-width: 640px) {
  .admin-section-heading h2 { font-size: 22px; }
  th, td { padding: 16px 20px; }
  .recent-card header { padding: 20px 20px 12px; }
}
</style>