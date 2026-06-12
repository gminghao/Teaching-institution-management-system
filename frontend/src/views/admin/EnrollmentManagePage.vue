<template>
  <div class="enrollment-manage-page">
    <section class="manage-head">
      <div>
        <h2>报名管理</h2>
        <p>查看和管理学员课程报名及缴费情况。</p>
      </div>
    </section>

    <section class="filter-card">
      <label>
        <el-icon><Document /></el-icon>
        <input type="text" placeholder="订单编号..." v-model="searchKeyword" />
      </label>
      <label>
        <el-icon><User /></el-icon>
        <input type="text" placeholder="学生姓名..." v-model="searchStudentName" />
      </label>
      <select v-model="filterEnrollmentStatus" @change="loadEnrollments()">
        <option value="">全部状态</option>
        <option value="PENDING">待处理</option>
        <option value="CONTACTED">已联系</option>
        <option value="ENROLLED">已报名</option>
        <option value="CANCELLED">已取消</option>
      </select>
      <select v-model="filterPaymentStatus" @change="loadEnrollments()">
        <option value="">全部缴费</option>
        <option value="UNPAID">未缴费</option>
        <option value="PARTIAL">部分缴费</option>
        <option value="PAID">已缴费</option>
        <option value="REFUNDED">已退款</option>
      </select>
      <button type="button" class="ghost-btn" @click="loadEnrollments()">
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
          <tr v-for="item in rows" :key="item.orderNo">
            <td class="order-cell">{{ item.orderNo }}</td>
            <td>
              <div class="student-cell">
                <span>{{ item.studentName.slice(0, 1) }}</span>
                <div>
                  <strong>{{ item.studentName }}</strong>
                  <small>{{ item.studentEmail }}</small>
                </div>
              </div>
            </td>
            <td>
              <strong>{{ item.courseTitle }}</strong>
            </td>
            <td>
              <strong>{{ item.amountText }}</strong>
              <small class="paid">已缴: {{ item.paidText }}</small>
            </td>
            <td>
              <span :class="['status-pill', item.paymentTone]">{{ item.paymentStatusText }}</span>
            </td>
            <td>
              <span :class="['status-pill', item.enrollmentTone]">{{ item.enrollmentStatusText }}</span>
            </td>
            <td class="right action-cell">
              <template v-if="item.enrollmentStatus !== 'ENROLLED' && item.enrollmentStatus !== 'CANCELLED'">
                <a href="javascript:void(0)" @click="openStatusDialog(item)">修改状态</a>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
      <footer>
        <span>显示 {{ rows.length > 0 ? 1 : 0 }} 到 {{ rows.length }} 条，共 {{ total }} 条结果</span>
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadEnrollments()"
        />
      </footer>
    </section>

    <!-- 修改状态弹窗 -->
    <el-dialog v-model="statusDialogVisible" title="修改报名状态" width="420px" :close-on-click-modal="false">
      <div v-if="statusTarget" class="status-dialog-body">
        <p>
          <strong>订单编号：</strong>{{ statusTarget.orderNo }}
        </p>
        <p>
          <strong>当前状态：</strong>
          <span :class="['status-pill', statusTarget.enrollmentTone]">{{ statusTarget.enrollmentStatusText }}</span>
        </p>
        <div class="status-select-row">
          <label>目标状态：</label>
          <select v-model="statusNextValue">
            <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
        </div>
      </div>
      <template #footer>
        <button type="button" class="ghost-btn" @click="statusDialogVisible = false">取消</button>
        <button type="button" class="primary-btn" :disabled="!statusNextValue" @click="submitStatusChange">确认</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Document, Filter, User } from '@element-plus/icons-vue'
import { getEnrollments, updateEnrollmentStatus } from '@/api/admin'
import {
  enrollmentStatusMap,
  enrollmentStatusTone,
  formatMoney,
  paymentStatusMap,
  paymentStatusTone
} from '@/utils/format'
import { ElMessage } from 'element-plus'

const enrollments = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const searchStudentName = ref('')
const filterEnrollmentStatus = ref('')
const filterPaymentStatus = ref('')

const rows = ref([])

// ---- debounce helper ----
let debounceTimer = null
function debounce(fn, delay = 300) {
  return (...args) => {
    clearTimeout(debounceTimer)
    debounceTimer = setTimeout(() => fn(...args), delay)
  }
}

const debouncedLoad = debounce(() => loadEnrollments(), 300)

watch(searchKeyword, () => debouncedLoad())
watch(searchStudentName, () => debouncedLoad())

const loadEnrollments = async () => {
  loading.value = true
  try {
    const res = await getEnrollments({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      studentName: searchStudentName.value || undefined,
      enrollmentStatus: filterEnrollmentStatus.value || undefined,
      paymentStatus: filterPaymentStatus.value || undefined
    })
    if (res.code === 200) {
      enrollments.value = res.data.list || []
      total.value = res.data.total || 0
      rows.value = enrollments.value.map(e => ({
        ...e,
        amountText: `¥${formatMoney(e.registrationFee)}`,
        paidText: `¥${formatMoney(e.paidAmount)}`,
        paymentStatusText: paymentStatusMap[e.paymentStatus] || e.paymentStatus,
        paymentTone: paymentStatusTone(e.paymentStatus),
        enrollmentStatusText: enrollmentStatusMap[e.enrollmentStatus] || e.enrollmentStatus,
        enrollmentTone: enrollmentStatusTone(e.enrollmentStatus)
      }))
    }
  } catch (e) {
    console.error('Failed to load enrollments:', e)
  } finally {
    loading.value = false
  }
}

// ---- 状态修改弹窗 ----
const statusDialogVisible = ref(false)
const statusTarget = ref(null)
const statusNextValue = ref('')

// 合法状态转换映射
const legalTransitions = {
  PENDING: ['CONTACTED', 'CANCELLED'],
  CONTACTED: ['ENROLLED', 'CANCELLED'],
  ENROLLED: [],
  CANCELLED: []
}

const statusOptions = computed(() => {
  if (!statusTarget.value) return []
  const current = statusTarget.value.enrollmentStatus
  const allowed = legalTransitions[current] || []
  return allowed.map(val => ({
    value: val,
    label: enrollmentStatusMap[val] || val
  }))
})

function openStatusDialog(item) {
  statusTarget.value = item
  statusNextValue.value = ''
  statusDialogVisible.value = true
}

async function submitStatusChange() {
  if (!statusNextValue.value || !statusTarget.value) return
  try {
    const res = await updateEnrollmentStatus(statusTarget.value.id, {
      enrollmentStatus: statusNextValue.value
    })
    if (res.code === 200) {
      ElMessage.success('状态修改成功')
      statusDialogVisible.value = false
      loadEnrollments()
    } else {
      ElMessage.error(res.message || '状态修改失败')
    }
  } catch (e) {
    console.error('Failed to update enrollment status:', e)
    ElMessage.error('状态修改失败，请重试')
  }
}

onMounted(loadEnrollments)
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

.ghost-btn,
.primary-btn {
  height: 44px;
  padding: 0 18px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border-radius: var(--radius-control);
  cursor: pointer;
  font-weight: 800;
  border: none;
}

.ghost-btn {
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}

.primary-btn {
  color: #fff;
  background: var(--color-primary-deep);
}

.primary-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.filter-card {
  padding: 20px;
  display: grid;
  grid-template-columns: minmax(200px, 1fr) minmax(200px, 1fr) 150px 150px auto;
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

.action-cell a {
  color: var(--color-primary);
  font-weight: 800;
  text-decoration: none;
  cursor: pointer;
}

.action-cell a:hover {
  text-decoration: underline;
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
  white-space: nowrap;
}

/* dialog internal styles */
.status-dialog-body p {
  margin: 0 0 12px;
}

.status-select-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-select-row label {
  white-space: nowrap;
  font-weight: 600;
}

.status-select-row select {
  flex: 1;
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
