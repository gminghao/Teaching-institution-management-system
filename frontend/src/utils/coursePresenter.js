import { formatMoney } from '@/utils/format'

function teacherInitials(name) {
  if (!name) return 'NA'
  return name
    .trim()
    .split(/\s+/)
    .map(part => part[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
}

export function toDisplayCourse(course) {
  const teacherName = course.teacherName || '未知讲师'

  return {
    id: course.id,
    title: course.title || '未知课程',
    description: course.description || course.subtitle || '',
    category: course.categoryName || '未分类',
    duration: '—',
    instructor: {
      name: teacherName,
      initials: teacherInitials(teacherName)
    },
    price: `¥${formatMoney(course.price)}`,
    registrationFee: `¥${formatMoney(course.registrationFee)}`,
    format: course.format || '在线',
    image: course.coverImage || '',
    raw: course
  }
}

export function toDisplayCourses(courses = []) {
  return courses.map(toDisplayCourse)
}
