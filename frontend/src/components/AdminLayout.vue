<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <h2>管理后台</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/courses">
          <el-icon><Reading /></el-icon>
          <span>课程管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/enrollments">
          <el-icon><Document /></el-icon>
          <span>报名管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/finance">
          <el-icon><Money /></el-icon>
          <span>财务管理</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="admin-main">
      <header class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="username">{{ username }}</span>
          <el-button type="text" @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser, logout } from '@/utils/auth'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const username = computed(() => {
  const user = getUser()
  return user?.realName || user?.username || '管理员'
})

const pageTitles = {
  '/admin/dashboard': '仪表盘',
  '/admin/courses': '课程管理',
  '/admin/enrollments': '报名管理',
  '/admin/finance': '财务管理'
}

const currentPageTitle = computed(() => {
  return pageTitles[route.path] || ''
})

const handleLogout = () => {
  ElMessageBox.confirm('确定退出登录？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    logout()
    router.push('/admin/login')
  }).catch(() => {})
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.admin-sidebar {
  width: 220px;
  background: #304156;
  color: #bfcbd9;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #263445;
}

.sidebar-header h2 {
  color: #fff;
  font-size: 16px;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
}

.sidebar-menu .el-menu-item {
  color: #bfcbd9;
}

.sidebar-menu .el-menu-item:hover,
.sidebar-menu .el-menu-item.is-active {
  background: #263445;
  color: #409eff;
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

.admin-header {
  height: 60px;
  background: #fff;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.username {
  font-size: 14px;
  color: #606266;
}

.admin-content {
  flex: 1;
  padding: 20px;
}
</style>
