import request from './request'

/**
 * 查询课程列表
 */
export function getCourses(params) {
  return request.get('/public/courses', { params })
}

/**
 * 查询课程详情
 */
export function getCourseDetail(id) {
  return request.get(`/public/courses/${id}`)
}

/**
 * 提交报名
 */
export function submitEnrollment(data) {
  return request.post('/public/enrollments', data)
}
