import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  // 访客端路由
  {
    path: '/',
    component: () => import('@/components/PublicLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/public/HomePage.vue')
      },
      {
        path: 'courses',
        name: 'CourseList',
        component: () => import('@/views/public/CourseListPage.vue')
      },
      {
        path: 'courses/:id',
        name: 'CourseDetail',
        component: () => import('@/views/public/CourseDetailPage.vue')
      },
      {
        path: 'enroll/:courseId',
        name: 'Enrollment',
        component: () => import('@/views/public/EnrollmentPage.vue')
      }
    ]
  },
  // 管理员登录（独立页面）
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/LoginPage.vue')
  },
  // 管理员端路由（需要鉴权）
  {
    path: '/admin',
    component: () => import('@/components/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/DashboardPage.vue')
      },
      {
        path: 'courses',
        name: 'CourseManage',
        component: () => import('@/views/admin/CourseManagePage.vue')
      },
      {
        path: 'enrollments',
        name: 'EnrollmentManage',
        component: () => import('@/views/admin/EnrollmentManagePage.vue')
      },
      {
        path: 'finance',
        name: 'Finance',
        component: () => import('@/views/admin/FinancePage.vue')
      }
    ]
  },
  // 404路由
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundPage.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 导航守卫：管理员端需要鉴权
router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    const token = getToken()
    if (!token) {
      next({ name: 'AdminLogin', query: { redirect: to.fullPath } })
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
