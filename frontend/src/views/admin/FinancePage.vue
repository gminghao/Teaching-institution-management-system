<template>
  <div class="finance-page">
    <section class="finance-head" :class="{ 'is-visible': phase >= 1 }">
      <div>
        <h2>财务概览</h2>
        <p>本月实时财务数据更新至 {{ currentDate }}</p>
      </div>
      <div class="head-actions">
        <el-tooltip content="暂不支持">
          <button
            type="button"
            class="ghost-btn"
            :class="{ 'is-loading': exporting }"
            :disabled="exporting"
            @click="handleExport"
          >
            <el-icon v-if="!exporting"><Download /></el-icon>
            <span v-else class="btn-spinner" />
            {{ exporting ? '导出中...' : '导出报表' }}
          </button>
        </el-tooltip>
        <button type="button" class="primary-btn" @click="openPaymentDialog">
          <el-icon><Plus /></el-icon>
          登记缴费
        </button>
      </div>
    </section>

    <section class="finance-stats">
      <template v-if="summaryLoading">
        <article v-for="i in 4" :key="'sk-' + i" class="stat-card skeleton-card">
          <div class="skeleton-line skeleton-title" />
          <div class="skeleton-line skeleton-value" />
          <div class="skeleton-line skeleton-note" />
        </article>
      </template>
      <template v-else>
        <article
          v-for="(stat, i) in stats"
          :key="stat.label"
          class="stat-card"
          :class="{ 'is-visible': phase >= 1, 'pulse-unpaid': stat.pulse }"
          :style="{ '--stagger': i * 80 + 'ms' }"
        >
          <div class="stat-title">
            <p>{{ stat.label }}</p>
            <el-icon :class="stat.tone"><component :is="stat.icon" /></el-icon>
          </div>
          <strong :class="stat.tone">
            <CountUpNumber
              :value="stat.rawValue"
              :prefix="stat.prefix || ''"
              :decimals="stat.decimals || 0"
              :duration="800"
            />
          </strong>
          <small>{{ stat.note }}</small>
        </article>
      </template>
    </section>

    <section class="finance-panels">
      <article class="trend-card" :class="{ 'is-visible': phase >= 2 }">
        <h3>缴费状态分布 (Payment Status)</h3>
        <template v-if="summaryLoading">
          <div class="skeleton-chart">
            <div class="skeleton-circle" />
            <div class="skeleton-lines">
              <div class="skeleton-line" style="width:80%" />
              <div class="skeleton-line" style="width:65%" />
              <div class="skeleton-line" style="width:70%" />
            </div>
          </div>
        </template>
        <template v-else>
          <PaymentStatusDonut
            :paid="summary.paidCount || 0"
            :unpaid="summary.unpaidCount || 0"
            :partial="summary.partialCount || 0"
          />
        </template>
      </article>

      <article class="recent-payments" :class="{ 'is-visible': phase >= 2 }">
        <header>
          <h3>最近缴费记录</h3>
          <a @click="router.push('/admin/enrollments')">查看全部</a>
        </header>
        <template v-if="recordsLoading">
          <div class="skeleton-payment-list">
            <div v-for="i in 3" :key="'sk-p-' + i" class="skeleton-payment-item">
              <div class="skeleton-avatar" />
              <div class="skeleton-payment-info">
                <div class="skeleton-line" style="width:60%" />
                <div class="skeleton-line" style="width:40%" />
              </div>
              <div class="skeleton-payment-amount">
                <div class="skeleton-line" style="width:50px" />
                <div class="skeleton-line" style="width:40px" />
              </div>
            </div>
          </div>
        </template>
        <template v-else>
          <div class="payment-list">
            <div
              v-for="tx in transactions.slice(0, 3)"
              :key="tx.id"
              class="payment-item"
            >
              <span class="payer-avatar">{{ (tx.studentName || '')[0] }}</span>
              <div class="payer-main">
                <strong>{{ tx.studentName }}</strong>
                <small>{{ tx.courseTitle }}</small>
              </div>
              <div class="payment-result">
                <strong>+¥{{ formatMoney(tx.paidAmount) }}</strong>
                <span :class="['status-pill', 'status-pill--' + (tx.paymentStatus || '').toLowerCase()]">
                  {{ statusText(tx.paymentStatus) }}
                </span>
              </div>
            </div>
          </div>
        </template>
      </article>
    </section>

    <section class="transaction-card" :class="{ 'is-visible': phase >= 3 }">
      <header>
        <h3>缴费明细流水</h3>
        <div class="search-wrapper" ref="searchWrapperRef">
          <label>
            <el-icon><Search /></el-icon>
            <input
              v-model="searchTerm"
              type="text"
              placeholder="搜索订单号或姓名..."
              @input="handleSearch"
              @focus="showSuggestions = true"
              @blur="showSuggestions = false"
            >
          </label>
          <Transition name="suggestion-fade">
            <div v-if="showSuggestions && searchTerm.trim()" class="suggestion-dropdown">
              <div
                v-for="s in filteredSuggestions"
                :key="s.id"
                class="suggestion-item"
                @mousedown.prevent="applySuggestion(s)"
              >
                <span class="suggestion-type">{{ s.typeLabel }}</span>
                <span class="suggestion-text">{{ s.display }}</span>
              </div>
              <div v-if="filteredSuggestions.length === 0" class="suggestion-empty">暂无匹配结果</div>
            </div>
          </Transition>
        </div>
      </header>

      <div class="table-wrap">
        <table v-if="recordsLoading" class="skeleton-table">
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
            <tr v-for="i in pageSize" :key="'sk-t-' + i">
              <td><div class="skeleton-line" style="width:80px" /></td>
              <td><div class="skeleton-line" style="width:100px" /></td>
              <td><div class="skeleton-line" style="width:60px" /></td>
              <td class="right"><div class="skeleton-line" style="width:60px;margin-left:auto" /></td>
              <td><div class="skeleton-line" style="width:50px" /></td>
              <td><div class="skeleton-line skeleton-pill" /></td>
            </tr>
          </tbody>
        </table>

        <table v-else>
          <thead>
            <tr>
              <th>日期 (Date)</th>
              <th>订单号 (Order ID)</th>
              <th>学员姓名 (Payer)</th>
              <th class="right">金额 (Amount)</th>
              <th>支付方式 (Method)</th>
              <th>状态 (Status)</th>
              <th v-if="showActions" class="actions-col">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tx in transactions" :key="tx.id" class="table-row">
              <td>{{ tx.createTime }}</td>
              <td>
                <span class="order-id">{{ tx.orderNo }}</span>
              </td>
              <td>{{ tx.studentName }}</td>
              <td class="right strong">¥{{ formatMoney(tx.registrationFee) }}</td>
              <td class="muted">{{ tx.paymentMethod }}</td>
              <td>
                <span :class="['status-pill', 'status-pill--' + (tx.paymentStatus || '').toLowerCase()]">
                  {{ statusText(tx.paymentStatus) }}
                </span>
              </td>
              <td v-if="showActions" class="row-actions">
                <button class="action-link" @click="viewDetail(tx)">查看详情</button>
                <button v-if="tx.paymentStatus !== 'PAID'" class="action-link" @click="registerForRecord">登记缴费</button>
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

    <el-dialog
      v-model="paymentDialogVisible"
      title="登记缴费"
      width="480px"
      :close-on-click-modal="false"
      @close="resetPaymentForm"
    >
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
            <el-option label="银行转账" value="BANK" />
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
        <el-button type="primary" :loading="submitting" @click="submitPayment">确认缴费</el-button>
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
import CountUpNumber from '@/components/admin/finance/CountUpNumber.vue'
import PaymentStatusDonut from '@/components/admin/finance/PaymentStatusDonut.vue'

const router = useRouter()

const phase = ref(0)
const currentDate = new Date().toLocaleDateString('zh-CN')

const searchTerm = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showActions = ref(true)
const showSuggestions = ref(false)

const filteredSuggestions = computed(() => {
  const q = searchTerm.value.trim().toLowerCase()
  if (!q) return []
  const results = []
  for (const s of transactions.value) {
    if (results.length >= 6) break
    if (s.orderNo?.toLowerCase().includes(q)) {
      results.push({ id: s.id, typeLabel: '订单', display: s.orderNo, orderNo: s.orderNo })
    } else if (s.studentName?.toLowerCase().includes(q)) {
      results.push({ id: s.id, typeLabel: '学员', display: s.studentName, orderNo: s.orderNo })
    } else if (s.courseTitle?.toLowerCase().includes(q)) {
      results.push({ id: s.id, typeLabel: '课程', display: s.courseTitle, orderNo: s.orderNo })
    }
  }
  return results
})

function applySuggestion(s) {
  searchTerm.value = s.orderNo || s.display || ''
  showSuggestions.value = false
  currentPage.value = 1
  loadRecords()
}

const paymentDialogVisible = ref(false)
const paymentFormRef = ref(null)
const unpaidOrders = ref([])
const submitting = ref(false)
const paymentForm = ref({ orderId: '', amount: 0, paymentMethod: '', operatorName: '' })
const paymentRules = {
  orderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' }
  ],
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

const summary = ref({ totalPaidAmount: 0, totalRegistrationFee: 0, paidCount: 0, unpaidCount: 0, partialCount: 0 })
const transactions = ref([])
const summaryLoading = ref(true)
const recordsLoading = ref(true)

const exporting = ref(false)
const handleExport = () => {
  if (exporting.value) return
  exporting.value = true
  setTimeout(() => { exporting.value = false; ElMessage.info('导出功能暂未开放') }, 1200)
}

const stats = computed(() => [
  { icon: Money, label: '已缴金额 (Paid Amount)', rawValue: summary.value.totalPaidAmount || 0, prefix: '¥', decimals: 2, note: '', tone: 'success', pulse: false },
  { icon: Wallet, label: '报名费总额 (Total Fees)', rawValue: summary.value.totalRegistrationFee || 0, prefix: '¥', decimals: 2, note: '', tone: 'default', pulse: false },
  { icon: UserFilled, label: '已缴费人数 (Paid Count)', rawValue: summary.value.paidCount || 0, prefix: '', decimals: 0, note: '', tone: 'success', pulse: false },
  { icon: TrendCharts, label: '未缴人数 (Unpaid)', rawValue: summary.value.unpaidCount || 0, prefix: '', decimals: 0, note: '', tone: 'warning', pulse: (summary.value.unpaidCount || 0) > 0 }
])

const loadSummary = async () => {
  summaryLoading.value = true
  try {
    const res = await getFinanceSummary()
    if (res.code === 200) summary.value = res.data
  } catch (e) {
    console.error('Failed to load finance summary:', e)
  } finally {
    summaryLoading.value = false
  }
}

const loadRecords = async () => {
  recordsLoading.value = true
  try {
    const params = { pageNum: currentPage.value, pageSize: pageSize.value }
    if (searchTerm.value) params.keyword = searchTerm.value
    const res = await getEnrollments(params)
    if (res.code === 200) {
      transactions.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('Failed to load records:', e)
  } finally {
    recordsLoading.value = false
  }
}

const statusText = (status) => {
  if (status === 'PAID') return '已缴费'
  if (status === 'PARTIAL') return '部分缴费'
  if (status === 'UNPAID') return '未缴费'
  if (status === 'REFUNDED') return '已退款'
  return status || '未知'
}

let searchTimer = null
const handleSearch = () => {
  showSuggestions.value = true
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { currentPage.value = 1; loadRecords() }, 300)
}

const handlePageChange = (page) => { currentPage.value = page; loadRecords() }
const handleSizeChange = (size) => { pageSize.value = size; currentPage.value = 1; loadRecords() }

const viewDetail = (tx) => { ElMessage.info(`查看订单详情: ${tx.orderNo}`) }
const registerForRecord = () => { openPaymentDialog() }

const loadUnpaidOrders = async () => {
  try {
    const res = await getEnrollments({ pageNum: 1, pageSize: 200, paymentStatus: 'UNPAID' })
    if (res.code === 200) {
      unpaidOrders.value = (res.data.list || []).filter(o => o.paymentStatus !== 'PAID')
    }
  } catch (e) {
    console.error('Failed to load unpaid orders:', e)
  }
}

const openPaymentDialog = () => { loadUnpaidOrders(); paymentDialogVisible.value = true }

const onOrderChange = (orderId) => {
  const order = unpaidOrders.value.find(o => o.id === orderId)
  if (order) paymentForm.value.amount = order.registrationFee || 0
}

const submitPayment = async () => {
  if (!paymentFormRef.value) return
  await paymentFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const res = await createPayment({
        orderId: paymentForm.value.orderId,
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
    } finally {
      submitting.value = false
    }
  })
}

const resetPaymentForm = () => {
  paymentForm.value = { orderId: '', amount: 0, paymentMethod: '', operatorName: '' }
  if (paymentFormRef.value) paymentFormRef.value.resetFields()
}

onMounted(async () => {
  await Promise.all([loadSummary(), loadRecords()])
  phase.value = 1
  setTimeout(() => { phase.value = 2 }, 200)
  setTimeout(() => { phase.value = 3 }, 400)
})
</script>

<style scoped>
/* ═══ 入场动画 ═══ */
.finance-head, .stat-card, .trend-card, .recent-payments, .transaction-card {
  opacity: 0; transform: translateY(12px);
  transition: opacity 0.5s ease, transform 0.5s ease;
}
.finance-head.is-visible, .stat-card.is-visible, .trend-card.is-visible, .recent-payments.is-visible, .transaction-card.is-visible {
  opacity: 1; transform: translateY(0);
}
.stat-card.is-visible { transition-delay: var(--stagger, 0ms); }
@media (prefers-reduced-motion: reduce) {
  .finance-head, .stat-card, .trend-card, .recent-payments, .transaction-card {
    transition: none; opacity: 1; transform: none;
  }
}

/* ═══ 页面基础 ═══ */
.finance-page { width: min(100%, var(--container-width)); display: flex; flex-direction: column; gap: 32px; }

/* ═══ 头部 ═══ */
.finance-head { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; }
.finance-head h2 { margin: 0 0 8px; color: var(--color-text); font-size: 28px; }
.finance-head p { margin: 0; color: var(--color-text-muted); }
.head-actions { display: flex; gap: 12px; }

/* ═══ 按钮状态 ═══ */
.ghost-btn, .primary-btn {
  height: 42px; padding: 0 18px;
  display: inline-flex; align-items: center; gap: 8px;
  border-radius: var(--radius-control); cursor: pointer; font-weight: 800;
  transition: background 0.15s ease, box-shadow 0.15s ease, transform 0.1s ease;
  position: relative;
}
.ghost-btn { color: var(--color-text); background: var(--color-surface); border: 1px solid var(--color-border); }
.ghost-btn:hover:not(:disabled) { background: var(--color-surface-muted); border-color: var(--color-border-strong); }
.ghost-btn:active:not(:disabled) { transform: scale(0.98); }
.ghost-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.ghost-btn.is-loading { pointer-events: none; }
.btn-spinner {
  width: 16px; height: 16px;
  border: 2px solid var(--color-border); border-top-color: var(--color-primary);
  border-radius: 50%; animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.primary-btn { color: #fff; background: var(--color-primary-container); border: 1px solid var(--color-primary-container); }
.primary-btn:hover { background: var(--color-primary-deep); border-color: var(--color-primary-deep); box-shadow: 0 2px 8px rgba(15, 76, 129, 0.25); }
.primary-btn:active { transform: scale(0.98); box-shadow: none; }

/* ═══ 统计卡片 ═══ */
.finance-stats { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 24px; }
.stat-card, .trend-card, .recent-payments, .transaction-card {
  background: var(--color-surface); border: 1px solid var(--color-border);
  border-radius: 18px; box-shadow: var(--shadow-subtle);
}
.stat-card { padding: 24px; transition: opacity 0.5s ease, transform 0.5s ease, box-shadow 0.25s ease; }
.stat-card:hover { transform: translateY(-3px) !important; box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08); }
.stat-card.pulse-unpaid { animation: subtle-pulse 3s ease-in-out infinite; }
@keyframes subtle-pulse {
  0%, 100% { box-shadow: var(--shadow-subtle); }
  50% { box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.15), var(--shadow-subtle); }
}
@media (prefers-reduced-motion: reduce) { .stat-card.pulse-unpaid { animation: none; } }

.stat-title { margin-bottom: 18px; display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.stat-title p { margin: 0; color: var(--color-text-muted); font-size: 14px; font-weight: 800; line-height: 1.4; }
.stat-title .el-icon { font-size: 22px; }
.stat-card strong { display: block; color: var(--color-text); font-size: 34px; line-height: 1.2; }
.stat-card strong.success, .stat-title .success { color: var(--color-success); }
.stat-card strong.warning, .stat-title .warning { color: var(--color-warning); }
.stat-card small { display: block; margin-top: 4px; color: var(--color-text-muted); font-weight: 700; }

/* ═══ 面板区域 ═══ */
.finance-panels { display: grid; grid-template-columns: 2fr 1fr; gap: 24px; }
.trend-card, .recent-payments { padding: 28px; }
.trend-card h3, .recent-payments h3, .transaction-card h3 { margin: 0; color: var(--color-text); font-size: 20px; }

/* ═══ 最近缴费记录 ═══ */
.recent-payments header, .transaction-card header { display: flex; align-items: center; justify-content: space-between; gap: 18px; }
.recent-payments a { color: var(--color-primary); font-size: 14px; font-weight: 800; text-decoration: none; cursor: pointer; transition: opacity 0.15s ease; }
.recent-payments a:hover { opacity: 0.75; }
.payment-list { margin-top: 22px; display: flex; flex-direction: column; gap: 4px; }
.payment-item { display: grid; grid-template-columns: 42px 1fr auto; gap: 12px; align-items: center; padding: 12px; border-radius: 12px; transition: background 0.15s ease; }
.payment-item:hover { background: var(--color-surface-muted); }
.payer-avatar { width: 42px; height: 42px; display: inline-flex; align-items: center; justify-content: center; color: var(--color-primary); background: var(--color-surface-muted); border-radius: var(--radius-pill); font-weight: 800; }
.payer-main strong, .payment-result strong { display: block; color: var(--color-text); font-size: 14px; }
.payer-main small { color: var(--color-text-muted); }
.payment-result { text-align: right; }

/* ═══ Pill 徽章 ═══ */
.status-pill { display: inline-flex; align-items: center; height: 24px; padding: 0 10px; border-radius: var(--radius-pill); font-size: 12px; font-weight: 700; white-space: nowrap; }
.status-pill--paid { color: #047857; background: rgba(16, 185, 129, 0.14); }
.status-pill--unpaid { color: #b91c1c; background: rgba(239, 68, 68, 0.12); }
.status-pill--partial { color: #b45309; background: rgba(245, 158, 11, 0.14); }
.status-pill--refunded { color: #4b5563; background: rgba(107, 114, 128, 0.12); }

/* ═══ 交易表格 ═══ */
.transaction-card { overflow: hidden; }
.transaction-card header { padding: 20px 24px; border-bottom: 1px solid var(--color-border); }
.search-wrapper { position: relative; width: 280px; }
.transaction-card label { position: relative; width: 100%; display: block; }
.transaction-card label .el-icon { position: absolute; top: 50%; left: 12px; color: var(--color-text-muted); transform: translateY(-50%); }
.transaction-card input { width: 100%; height: 40px; padding: 0 12px 0 36px; border: 1px solid var(--color-border); border-radius: var(--radius-control); outline: none; transition: border-color 0.15s ease, box-shadow 0.15s ease; }
.transaction-card input:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(15, 76, 129, 0.08); }

/* 搜索建议 */
.suggestion-dropdown { position: absolute; top: calc(100% + 4px); left: 0; right: 0; background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-control); box-shadow: 0 8px 24px rgba(15, 23, 42, 0.1); z-index: 100; overflow: hidden; }
.suggestion-item { display: flex; align-items: center; gap: 10px; padding: 10px 14px; cursor: pointer; font-size: 13px; transition: background 0.1s ease; }
.suggestion-item:hover { background: var(--color-surface-muted); }
.suggestion-type { flex-shrink: 0; width: 36px; height: 20px; display: inline-flex; align-items: center; justify-content: center; background: var(--color-surface-muted); border-radius: 4px; font-size: 11px; font-weight: 700; color: var(--color-text-muted); }
.suggestion-text { color: var(--color-text); font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.suggestion-empty { padding: 16px 14px; text-align: center; color: var(--color-text-muted); font-size: 13px; }
.suggestion-fade-enter-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.suggestion-fade-leave-active { transition: opacity 0.1s ease, transform 0.1s ease; }
.suggestion-fade-enter-from, .suggestion-fade-leave-to { opacity: 0; transform: translateY(-4px); }

/* 表格 */
.table-wrap { overflow-x: auto; }
table { width: 100%; min-width: 820px; border-collapse: collapse; }
th, td { padding: 16px 24px; text-align: left; border-bottom: 1px solid var(--color-border); }
th { color: var(--color-text-muted); background: var(--color-surface-muted); font-size: 13px; }
td { color: var(--color-text); }
.right { text-align: right; }
.strong { font-weight: 800; }
.muted { color: var(--color-text-muted); }
.order-id { color: var(--color-info); font-weight: 700; font-family: 'SF Mono', 'Cascadia Code', 'Consolas', monospace; font-size: 13px; cursor: pointer; border-bottom: 1px dashed transparent; transition: border-color 0.15s ease; }
.order-id:hover { border-bottom-color: var(--color-primary); }
.table-row { transition: background 0.15s ease; }
.table-row:hover { background: rgba(15, 76, 129, 0.03); }
.actions-col { width: 120px; }
.row-actions { display: flex; gap: 8px; opacity: 0.3; transition: opacity 0.2s ease; }
.table-row:hover .row-actions { opacity: 1; }
.action-link { padding: 0; border: none; background: none; color: var(--color-primary); font-size: 13px; font-weight: 600; cursor: pointer; white-space: nowrap; }
.action-link:hover { opacity: 0.7; }

/* ═══ Skeleton ═══ */
.skeleton-line { height: 14px; border-radius: 6px; background: linear-gradient(90deg, #e5e7eb 25%, #f3f4f6 50%, #e5e7eb 75%); background-size: 200% 100%; animation: shimmer 1.5s ease infinite; }
.skeleton-title { width: 60%; margin-bottom: 18px; }
.skeleton-value { width: 45%; height: 34px; margin-bottom: 8px; }
.skeleton-note { width: 30%; }
.skeleton-card { pointer-events: none; }
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.skeleton-chart { display: flex; align-items: center; gap: 28px; padding: 24px 0; }
.skeleton-circle { width: 140px; height: 140px; flex-shrink: 0; border-radius: 50%; background: linear-gradient(90deg, #e5e7eb 25%, #f3f4f6 50%, #e5e7eb 75%); background-size: 200% 100%; animation: shimmer 1.5s ease infinite; }
.skeleton-lines { flex: 1; display: flex; flex-direction: column; gap: 14px; }
.skeleton-payment-list { margin-top: 22px; display: flex; flex-direction: column; gap: 12px; }
.skeleton-payment-item { display: grid; grid-template-columns: 42px 1fr auto; gap: 12px; align-items: center; padding: 12px; }
.skeleton-avatar { width: 42px; height: 42px; border-radius: 50%; background: linear-gradient(90deg, #e5e7eb 25%, #f3f4f6 50%, #e5e7eb 75%); background-size: 200% 100%; animation: shimmer 1.5s ease infinite; }
.skeleton-payment-info { display: flex; flex-direction: column; gap: 6px; }
.skeleton-payment-amount { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; }
.skeleton-table .skeleton-line { height: 12px; }
.skeleton-table .skeleton-pill { width: 56px; height: 24px; border-radius: var(--radius-pill); }

/* ═══ 分页脚 ═══ */
.transaction-card footer { padding: 16px 24px; display: flex; justify-content: space-between; align-items: center; gap: 18px; color: var(--color-text-muted); background: var(--color-surface-muted); }

/* ═══ 响应式 ═══ */
@media (max-width: 1120px) {
  .finance-stats, .finance-panels { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .trend-card { grid-column: 1 / -1; }
}
@media (max-width: 760px) {
  .finance-head, .transaction-card header, .transaction-card footer { align-items: flex-start; flex-direction: column; }
  .finance-stats, .finance-panels { grid-template-columns: 1fr; }
  .search-wrapper { width: 100%; }
  .row-actions { opacity: 1; }
}
</style>
