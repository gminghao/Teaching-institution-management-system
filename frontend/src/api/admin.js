import request from './request'

// ========== 认证 ==========
export function login(data) {
  return request.post('/admin/auth/login', data)
}

// ========== 仪表盘 ==========
export function getDashboardOverview() {
  return request.get('/admin/dashboard/overview')
}

// ========== 课程管理 ==========
export function getAdminCourses(params) {
  return request.get('/admin/courses', { params })
}

export function createCourse(data) {
  return request.post('/admin/courses', data)
}

export function updateCourse(id, data) {
  return request.put(`/admin/courses/${id}`, data)
}

export function onlineCourse(id) {
  return request.put(`/admin/courses/${id}/online`)
}

export function offlineCourse(id) {
  return request.put(`/admin/courses/${id}/offline`)
}

// ========== 报名管理 ==========
export function getEnrollments(params) {
  return request.get('/admin/enrollments', { params })
}

export function updateEnrollmentStatus(id, data) {
  return request.put(`/admin/enrollments/${id}/status`, data)
}

// ========== 缴费管理 ==========
export function createPayment(data) {
  return request.post('/admin/payments', data)
}

// ========== 财务管理 ==========
export function getFinanceSummary() {
  return request.get('/admin/finance/summary')
}
