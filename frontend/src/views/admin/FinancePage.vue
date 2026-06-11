<template>
  <div class="finance-page">
    <section class="finance-head">
      <div>
        <h2>财务概览</h2>
        <p>本月实时财务数据更新至 2026年06月11日</p>
      </div>
      <div class="head-actions">
        <button type="button" class="ghost-btn">
          <el-icon><Download /></el-icon>
          导出报表
        </button>
        <button type="button" class="primary-btn">
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
        <h3>营收趋势 (Revenue Trend)</h3>
        <div class="chart">
          <div class="axis">
            <span>¥400k</span>
            <span>¥300k</span>
            <span>¥200k</span>
            <span>¥100k</span>
            <span>¥0</span>
          </div>
          <div class="bars">
            <div v-for="bar in bars" :key="bar.month" class="bar-item">
              <span :style="{ height: bar.height }" />
              <strong>{{ bar.month }}</strong>
            </div>
          </div>
        </div>
      </article>

      <article class="recent-payments">
        <header>
          <h3>最近缴费记录</h3>
          <a href="#">查看全部</a>
        </header>
        <div class="payment-list">
          <div v-for="tx in mockTransactions.slice(0, 3)" :key="tx.id" class="payment-item">
            <span class="payer-avatar">{{ tx.name[0] }}</span>
            <div class="payer-main">
              <strong>{{ tx.name }}</strong>
              <small>{{ tx.method }}</small>
            </div>
            <div class="payment-result">
              <strong>+{{ tx.amount }}</strong>
              <small :class="tx.status">{{ statusText(tx.status) }}</small>
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
          <input type="text" placeholder="搜索订单号或姓名...">
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
              <th>经办人 (Operator)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tx in mockTransactions" :key="tx.id">
              <td>{{ tx.date }}</td>
              <td class="order-id">{{ tx.id }}</td>
              <td>{{ tx.name }}</td>
              <td class="right strong">{{ tx.amount }}</td>
              <td class="muted">{{ tx.method }}</td>
              <td>
                <span :class="['tx-status', tx.status]">{{ statusText(tx.status, true) }}</span>
              </td>
              <td class="muted">{{ tx.operator }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <footer>
        <span>显示 1-4 条，共 1,248 条记录</span>
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
import { Download, Money, Plus, Search, TrendCharts, UserFilled, Wallet } from '@element-plus/icons-vue'
import { mockTransactions } from '@/data/mock'

const stats = [
  { icon: Wallet, label: '总营收 (Total Revenue)', value: '¥1,245,000', note: '+12.5% 较上月', tone: 'success' },
  { icon: UserFilled, label: '报名费 (Registration Fees)', value: '¥850,000', note: '+5.2% 较上月', tone: 'success' },
  { icon: Money, label: '已缴金额 (Paid Amount)', value: '¥1,100,000', note: '88% 收款率', tone: 'default' },
  { icon: TrendCharts, label: '待缴余额 (Outstanding)', value: '¥145,000', note: '125 名学员未缴清', tone: 'warning' }
]

const bars = [
  { month: '9月', height: '40%' },
  { month: '10月', height: '60%' },
  { month: '11月', height: '85%' },
  { month: '12月', height: '100%' }
]

const statusText = (status, formal = false) => {
  if (status === 'completed') return formal ? '已完成' : '成功'
  if (status === 'pending') return '处理中'
  return formal ? '失败' : '失败'
}
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

.axis {
  position: absolute;
  inset: 0 auto 0 0;
  width: 58px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: 800;
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
  grid-template-columns: repeat(4, 1fr);
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
}

.bar-item:last-child span {
  background: var(--color-primary-container);
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
  min-width: 950px;
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

.tx-status.completed {
  background: rgba(16, 185, 129, 0.1);
}

.tx-status.pending {
  background: rgba(245, 158, 11, 0.12);
}

.tx-status.failed {
  background: rgba(239, 68, 68, 0.12);
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

.transaction-card nav {
  display: flex;
  gap: 6px;
}

.transaction-card nav button {
  width: 34px;
  height: 34px;
  border: 1px solid var(--color-border);
  border-radius: 7px;
  background: var(--color-surface);
  cursor: pointer;
}

.transaction-card nav button.active {
  color: #fff;
  background: var(--color-primary-deep);
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
