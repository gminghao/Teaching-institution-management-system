/**
 * 金额格式化
 * @param {number} amount
 * @returns {string}
 */
export function formatMoney(amount) {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

/**
 * 日期格式化
 * @param {string|Date} date
 * @returns {string}
 */
export function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 报名状态文本映射
 */
export const enrollmentStatusMap = {
  PENDING: '待处理',
  CONTACTED: '已联系',
  ENROLLED: '已报名',
  CANCELLED: '已取消'
}

/**
 * 缴费状态文本映射
 */
export const paymentStatusMap = {
  UNPAID: '未缴费',
  PARTIAL: '部分缴费',
  PAID: '已缴费',
  REFUNDED: '已退款'
}

/**
 * 课程状态文本映射
 */
export const courseStatusMap = {
  DRAFT: '草稿',
  ONLINE: '已上架',
  OFFLINE: '已下架'
}
