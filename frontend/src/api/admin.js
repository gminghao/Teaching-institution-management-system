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

export function getCourseDetail(id) {
  return request.get(`/admin/courses/${id}`)
}

export function createCourse(data) {
  return request.post('/admin/courses', data)
}

export function updateCourse(id, data) {
  return request.put(`/admin/courses/${id}`, data)
}

export function deleteCourse(id) {
  return request.delete(`/admin/courses/${id}`)
}

export function onlineCourse(id) {
  return request.put(`/admin/courses/${id}/online`)
}

export function offlineCourse(id) {
  return request.put(`/admin/courses/${id}/offline`)
}

export function getCategories() {
  return request.get('/admin/courses/categories')
}

export function uploadCourseImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/courses/upload-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
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

export function getPaymentRecords(orderId) {
  return request.get('/admin/payments', { params: { orderId } })
}

// ========== 财务管理 ==========
export function getFinanceSummary() {
  return request.get('/admin/finance/summary')
}
