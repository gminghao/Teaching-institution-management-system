<template>
  <div class="finance-page">
    <section class="finance-head">
      <div>
        <h2>财务概览</h2>
        <p>本月实时财务数据更新至 {{ currentDate }}</p>
      </div>
      <div class="head-actions">
        <el-tooltip content="暂不支持">
          <button type="button" class="ghost-btn" disabled>
            <el-icon><Download /></el-icon>
            导出报表
          </button>
        </el-tooltip>
        <button type="button" class="primary-btn" @click="openPaymentDialog">
          <el-icon><Plus /></el-icon>
          登记缴费
        </button>
      </div>
    </section>

    <section class="finance-stats">
      <article v-for="stat in stats" :key="stat.label" class="stat-card">
        <div class="stat-title">
          <p>{{ stat.label }}</p>
          <el-icon :class="stat.tone"><component :is="stat.icon" /></el-icon>
        </div>
        <strong :class="stat.tone">{{ stat.value }}</strong>
        <small>{{ stat.note }}</small>
      </article>
    </section>

    <section class="finance-panels">
      <article class="trend-card">
        <h3>缴费状态分布 (Payment Status)</h3>
        <div class="chart">
          <div class="bars bars--status">
            <div v-for="bar in bars" :key="bar.label" class="bar-item">
              <span :style="{ height: bar.height }" :class="bar.tone" />
              <strong>{{ bar.label }}</strong>
              <small>{{ bar.count }}</small>
            </div>
          </div>
        </div>
      </article>

      <article class="recent-payments">
        <header>
          <h3>最近缴费记录</h3>
          <a @click="router.push('/admin/enrollments')">查看全部</a>
        </header>
        <div class="payment-list">
          <div v-for="tx in transactions.slice(0, 3)" :key="tx.id" class="payment-item">
            <span class="payer-avatar">{{ (tx.studentName || '')[0] }}</span>
            <div class="payer-main">
              <strong>{{ tx.studentName }}</strong>
              <small>{{ tx.courseTitle }}</small>
            </div>
            <div class="payment-result">
              <strong>+¥{{ formatMoney(tx.paidAmount) }}</strong>
              <small :class="tx.paymentStatus">{{ statusText(tx.paymentStatus) }}</small>
            </div>
          </div>
        </div>
      </article>
    </section>

    <section class="transaction-card">
      <header>
        <h3>缴费明细流水</h3>
        <label>
          <el-icon><Search /></el-icon>
          <input type="text" v-model="searchTerm" placeholder="搜索订单号或姓名..." @input="handleSearch">
        </label>
      </header>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>日期 (Date)</th>
              <th>订单号 (Order ID)</th>
              <th>学员姓名 (Payer)</th>
              <th class="right">金额 (Amount)</th>
              <th>支付方式 (Method)</th>
              <th>状态 (Status)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tx in transactions" :key="tx.id">
              <td>{{ tx.createTime }}</td>
              <td class="order-id">{{ tx.orderNo }}</td>
              <td>{{ tx.studentName }}</td>
              <td class="right strong">¥{{ formatMoney(tx.registrationFee) }}</td>
              <td class="muted">{{ tx.paymentMethod }}</td>
              <td>
                <span :class="['tx-status', tx.paymentStatus]">{{ statusText(tx.paymentStatus) }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <footer>
        <span>共 {{ total }} 条记录</span>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </footer>
    </section>

    <!-- 登记缴费弹窗 -->
    <el-dialog v-model="paymentDialogVisible" title="登记缴费" width="480px" @close="resetPaymentForm">
      <el-form ref="paymentFormRef" :model="paymentForm" :rules="paymentRules" label-width="100px">
        <el-form-item label="选择订单" prop="orderId">
          <el-select v-model="paymentForm.orderId" placeholder="请选择未缴/部分缴订单" style="width: 100%" @change="onOrderChange">
            <el-option
              v-for="order in unpaidOrders"
              :key="order.id"
              :label="`${order.orderNo} - ${order.studentName} (${order.courseTitle})`"
              :value="order.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="缴费金额" prop="amount">
          <el-input-number v-model="paymentForm.amount" :min="0.01" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="paymentForm.paymentMethod" placeholder="请选择支付方式" style="width: 100%">
            <el-option label="现金" value="CASH" />
            <el-option label="银行转账" value="BANK_TRANSFER" />
            <el-option label="微信支付" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="经办人" prop="operatorName">
          <el-input v-model="paymentForm.operatorName" placeholder="请输入经办人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paymentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPayment">确认缴费</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Download, Money, Plus, Search, TrendCharts, UserFilled, Wallet } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getFinanceSummary, getEnrollments, createPayment } from '@/api/admin'
import { formatMoney } from '@/utils/format'

const router = useRouter()

// 1. 动态日期
const currentDate = new Date().toLocaleDateString('zh-CN')

// 搜索、分页
const searchTerm = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 登记缴费弹窗
const paymentDialogVisible = ref(false)
const paymentFormRef = ref(null)
const unpaidOrders = ref([])
const paymentForm = ref({
  orderId: '',
  amount: 0,
  paymentMethod: '',
  operatorName: ''
})
const paymentRules = {
  orderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' }
  ],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

const summary = ref({
  totalPaidAmount: 0,
  totalRegistrationFee: 0,
  paidCount: 0,
  unpaidCount: 0,
  partialCount: 0
})

const transactions = ref([])
const loading = ref(false)

// 4. 数据驱动的柱状图（paid/unpaid/partial 计数对比）
const bars = computed(() => {
  const counts = [
    { label: '已缴费', count: summary.value.paidCount || 0, tone: 'success' },
    { label: '未缴费', count: summary.value.unpaidCount || 0, tone: 'danger' },
    { label: '部分缴费', count: summary.value.partialCount || 0, tone: 'warning' }
  ]
  const maxCount = Math.max(...counts.map(c => c.count), 1)
  return counts.map(c => ({
    ...c,
    height: `${(c.count / maxCount) * 100}%`
  }))
})

const stats = computed(() => [
  { icon: Money, label: '已缴金额 (Paid Amount)', value: `¥${formatMoney(summary.value.totalPaidAmount)}`, note: '', tone: 'success' },
  { icon: Wallet, label: '报名费总额 (Total Fees)', value: `¥${formatMoney(summary.value.totalRegistrationFee)}`, note: '', tone: 'default' },
  { icon: UserFilled, label: '已缴费人数 (Paid Count)', value: String(summary.value.paidCount), note: '', tone: 'success' },
  { icon: TrendCharts, label: '未缴人数 (Unpaid)', value: String(summary.value.unpaidCount), note: '', tone: 'warning' }
])

const loadSummary = async () => {
  try {
    const res = await getFinanceSummary()
    if (res.code === 200) {
      summary.value = res.data
    }
  } catch (e) {
    console.error('Failed to load finance summary:', e)
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (searchTerm.value) {
      params.keyword = searchTerm.value
    }
    const res = await getEnrollments(params)
    if (res.code === 200) {
      transactions.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('Failed to load records:', e)
  } finally {
    loading.value = false
  }
}

const statusText = (status) => {
  if (status === 'PAID') return '已缴费'
  if (status === 'PARTIAL') return '部分缴费'
  if (status === 'UNPAID') return '未缴费'
  if (status === 'REFUNDED') return '已退款'
  return status || '未知'
}

// 搜索防抖
let searchTimer = null
const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    loadRecords()
  }, 300)
}

// 分页
const handlePageChange = (page) => {
  currentPage.value = page
  loadRecords()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadRecords()
}

// 登记缴费弹窗
const loadUnpaidOrders = async () => {
  try {
    const res = await getEnrollments({ pageNum: 1, pageSize: 200, paymentStatus: 'UNPAID' })
    if (res.code === 200) {
      const unpaidList = (res.data.list || []).filter(o => o.paymentStatus !== 'PAID')
      unpaidOrders.value = unpaidList
    }
  } catch (e) {
    console.error('Failed to load unpaid orders:', e)
  }
}

const openPaymentDialog = () => {
  loadUnpaidOrders()
  paymentDialogVisible.value = true
}

const onOrderChange = (orderId) => {
  const order = unpaidOrders.value.find(o => o.id === orderId)
  if (order) {
    paymentForm.value.amount = order.registrationFee || 0
  }
}

const submitPayment = async () => {
  if (!paymentFormRef.value) return
  await paymentFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const res = await createPayment({
        enrollmentId: paymentForm.value.orderId,
        amount: paymentForm.value.amount,
        paymentMethod: paymentForm.value.paymentMethod,
        operatorName: paymentForm.value.operatorName
      })
      if (res.code === 200) {
        ElMessage.success('缴费登记成功')
        paymentDialogVisible.value = false
        resetPaymentForm()
        loadSummary()
        loadRecords()
      } else {
        ElMessage.error(res.message || '缴费登记失败')
      }
    } catch (e) {
      console.error('Failed to create payment:', e)
      ElMessage.error('缴费登记失败，请稍后重试')
    }
  })
}

const resetPaymentForm = () => {
  paymentForm.value = { orderId: '', amount: 0, paymentMethod: '', operatorName: '' }
  if (paymentFormRef.value) {
    paymentFormRef.value.resetFields()
  }
}

onMounted(() => {
  loadSummary()
  loadRecords()
})
</script>

<style scoped>
.finance-page {
  width: min(100%, var(--container-width));
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.finance-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
}

.finance-head h2 {
  margin: 0 0 8px;
  color: var(--color-text);
  font-size: 28px;
}

.finance-head p {
  margin: 0;
  color: var(--color-text-muted);
}

.head-actions {
  display: flex;
  gap: 12px;
}

.ghost-btn,
.primary-btn {
  height: 42px;
  padding: 0 18px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border-radius: var(--radius-control);
  cursor: pointer;
  font-weight: 800;
}

.ghost-btn {
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}

.ghost-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.primary-btn {
  color: #fff;
  background: var(--color-primary-container);
  border: 1px solid var(--color-primary-container);
}

.finance-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
}

.stat-card,
.trend-card,
.recent-payments,
.transaction-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 18px;
  box-shadow: var(--shadow-subtle);
}

.stat-card {
  padding: 24px;
}

.stat-title {
  margin-bottom: 18px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.stat-title p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 14px;
  font-weight: 800;
  line-height: 1.4;
}

.stat-title .el-icon {
  font-size: 22px;
}

.stat-card strong {
  display: block;
  color: var(--color-text);
  font-size: 34px;
  line-height: 1.2;
}

.stat-card strong.success,
.stat-title .success {
  color: var(--color-success);
}

.stat-card strong.warning,
.stat-title .warning {
  color: var(--color-warning);
}

.stat-card small {
  display: block;
  margin-top: 4px;
  color: var(--color-text-muted);
  font-weight: 700;
}

.finance-panels {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.trend-card,
.recent-payments {
  padding: 28px;
}

.trend-card h3,
.recent-payments h3,
.transaction-card h3 {
  margin: 0;
  color: var(--color-text);
  font-size: 20px;
}

.chart {
  position: relative;
  height: 300px;
  margin-top: 24px;
  padding-left: 58px;
  border-bottom: 1px solid var(--color-border);
}

.chart::before {
  position: absolute;
  inset: 0 0 0 58px;
  content: "";
  background: repeating-linear-gradient(to bottom, transparent 0, transparent 24%, var(--color-border) 24.3%, transparent 24.8%);
  opacity: 0.7;
}

.bars {
  position: relative;
  z-index: 1;
  height: 92%;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  align-items: end;
  gap: 28px;
}

.bar-item {
  height: 100%;
  display: flex;
  align-items: center;
  flex-direction: column;
  justify-content: flex-end;
  gap: 10px;
}

.bar-item span {
  width: 72%;
  display: block;
  background: rgba(15, 76, 129, 0.38);
  border-radius: 8px 8px 0 0;
  transition: height 0.4s ease;
}

.bar-item span.success {
  background: var(--color-success);
}

.bar-item span.danger {
  background: var(--color-danger);
}

.bar-item span.warning {
  background: var(--color-warning);
}

.bar-item strong {
  color: var(--color-text-muted);
  font-size: 13px;
}

.recent-payments header,
.transaction-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.recent-payments a {
  color: var(--color-primary);
  font-size: 14px;
  font-weight: 800;
  text-decoration: none;
  cursor: pointer;
}

.payment-list {
  margin-top: 22px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.payment-item {
  display: grid;
  grid-template-columns: 42px 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 12px;
  border-radius: 12px;
}

.payment-item:hover {
  background: var(--color-surface-muted);
}

.payer-avatar {
  width: 42px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  background: var(--color-surface-muted);
  border-radius: var(--radius-pill);
  font-weight: 800;
}

.payer-main strong,
.payment-result strong {
  display: block;
  color: var(--color-text);
  font-size: 14px;
}

.payer-main small {
  color: var(--color-text-muted);
}

.payment-result {
  text-align: right;
}

.payment-result small,
.tx-status {
  font-size: 12px;
  font-weight: 800;
}

.PAID {
  color: var(--color-success);
}

.PARTIAL {
  color: var(--color-warning);
}

.UNPAID {
  color: var(--color-danger);
}

.REFUNDED {
  color: var(--color-info);
}

/* 保留旧类名兼容 */
.completed {
  color: var(--color-success);
}

.pending {
  color: var(--color-warning);
}

.failed {
  color: var(--color-danger);
}

.transaction-card {
  overflow: hidden;
}

.transaction-card header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border);
}

.transaction-card label {
  position: relative;
  width: 280px;
  display: block;
}

.transaction-card label .el-icon {
  position: absolute;
  top: 50%;
  left: 12px;
  color: var(--color-text-muted);
  transform: translateY(-50%);
}

.transaction-card input {
  width: 100%;
  height: 40px;
  padding: 0 12px 0 36px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-control);
  outline: none;
}

.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 820px;
  border-collapse: collapse;
}

th,
td {
  padding: 14px 24px;
  text-align: left;
  border-bottom: 1px solid var(--color-border);
}

th {
  color: var(--color-text-muted);
  background: var(--color-surface-muted);
  font-size: 13px;
}

td {
  color: var(--color-text);
}

.right {
  text-align: right;
}

.strong {
  font-weight: 800;
}

.muted {
  color: var(--color-text-muted);
}

.order-id {
  color: var(--color-primary);
  font-weight: 800;
}

.tx-status {
  display: inline-flex;
  padding: 4px 9px;
  border-radius: 6px;
  border: 1px solid currentColor;
}

.tx-status.completed,
.tx-status.PAID {
  background: rgba(16, 185, 129, 0.1);
}

.tx-status.pending,
.tx-status.PARTIAL {
  background: rgba(245, 158, 11, 0.12);
}

.tx-status.failed,
.tx-status.UNPAID {
  background: rgba(239, 68, 68, 0.12);
}

.tx-status.REFUNDED {
  background: rgba(107, 114, 128, 0.12);
}

.transaction-card footer {
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  color: var(--color-text-muted);
  background: var(--color-surface-muted);
}

@media (max-width: 1120px) {
  .finance-stats,
  .finance-panels {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .trend-card {
    grid-column: 1 / -1;
  }
}

@media (max-width: 760px) {
  .finance-head,
  .transaction-card header,
  .transaction-card footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .finance-stats,
  .finance-panels {
    grid-template-columns: 1fr;
  }

  .transaction-card label {
    width: 100%;
  }
}
</style>
