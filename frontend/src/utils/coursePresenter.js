import { mockCourses } from '@/data/mock'
import { formatMoney } from '@/utils/format'

function fallbackCourse(index = 0) {
  return mockCourses[index % mockCourses.length]
}

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

export function toDisplayCourse(course, index = 0) {
  const fallback = fallbackCourse(index)
  const teacherName = course.teacherName || fallback.instructor.name
  const description = course.description || course.subtitle || fallback.description

  return {
    id: course.id,
    title: course.title || fallback.title,
    description,
    category: course.categoryName || fallback.category,
    duration: fallback.duration,
    instructor: {
      name: teacherName,
      initials: teacherInitials(teacherName)
    },
    price: `¥${formatMoney(course.price)}`,
    registrationFee: `¥${formatMoney(course.registrationFee)}`,
    format: fallback.format,
    image: course.coverImage || fallback.image,
    raw: course
  }
}

export function toDisplayCourses(courses = []) {
  return courses.map((course, index) => toDisplayCourse(course, index))
}
