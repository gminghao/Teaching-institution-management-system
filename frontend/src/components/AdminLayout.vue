<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <router-link to="/admin/dashboard" class="sidebar-brand" aria-label="返回控制台">
        <span class="sidebar-brand__mark">
          <el-icon><Reading /></el-icon>
        </span>
        <span>
          <strong>管理后台</strong>
          <small>Institutional Access</small>
        </span>
      </router-link>

      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>控制台</span>
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

      <button class="sidebar-logout" type="button" @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        <span>退出登录</span>
      </button>
    </aside>

    <div class="admin-main">
      <header class="admin-header">
        <div class="header-title">
          <h1>{{ currentPageTitle }}</h1>
        </div>

        <div class="header-actions">
          <div class="header-search">
            <el-icon><Search /></el-icon>
            <input
              v-model="searchKeyword"
              class="header-search__input"
              type="text"
              placeholder="搜索课程、订单或学员..."
              @keyup.enter="handleSearch"
            >
          </div>
          <div class="admin-user">
            <span class="admin-user__avatar">{{ username[0] }}</span>
            <span class="admin-user__name">{{ username }}</span>
          </div>
        </div>
      </header>

      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser, logout } from '@/utils/auth'
import { ElMessageBox } from 'element-plus'
import {
  DataAnalysis,
  Document,
  Money,
  Reading,
  Search,
  SwitchButton
} from '@element-plus/icons-vue'
import request from '@/api/request'
import { useIdleTimeout } from '@/composables/useIdleTimeout'

const route = useRoute()
const router = useRouter()

// 启用空闲超时自动登出（15分钟）
useIdleTimeout()

const activeMenu = computed(() => route.path)

const username = computed(() => {
  const user = getUser()
  return user?.realName || user?.username || '管理员'
})

const pageTitles = {
  '/admin/dashboard': '控制台',
  '/admin/courses': '课程管理',
  '/admin/enrollments': '报名管理',
  '/admin/finance': '财务管理'
}

const currentPageTitle = computed(() => {
  return pageTitles[route.path] || '管理后台'
})

const searchKeyword = ref('')

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    router.push({ path: route.path, query: { keyword } })
  } else {
    router.push({ path: route.path })
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定退出登录？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    // 调用后端登出接口
    try {
      await request.post('/admin/auth/logout')
    } catch {
      // 忽略登出接口错误
    }
    // 本地登出
    logout()
    // 通知其他标签页
    try {
      if (typeof BroadcastChannel !== 'undefined') {
        const channel = new BroadcastChannel('auth_sync')
        channel.postMessage({ type: 'logout' })
        channel.close()
      } else {
        localStorage.setItem('auth_logout_event', Date.now().toString())
      }
    } catch {
      // 忽略广播错误
    }
    router.push('/admin/login')
  }).catch(() => {})
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: flex;
  background: var(--color-background);
}

.admin-sidebar {
  position: sticky;
  top: 0;
  width: 260px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 28px 10px 18px;
  color: #e5edf8;
  background: var(--color-sidebar);
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 0 14px 36px;
  color: #ffffff;
  text-decoration: none;
}

.sidebar-brand__mark {
  width: 46px;
  height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  background: var(--color-primary);
  border-radius: 14px;
}

.sidebar-brand strong {
  display: block;
  font-size: 23px;
  line-height: 1.25;
}

.sidebar-brand small {
  display: block;
  margin-top: 2px;
  color: #cbd5e1;
  font-size: 13px;
  font-weight: 600;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  background: transparent;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 52px;
  margin: 6px 0;
  padding: 0 22px !important;
  color: #cbd5e1;
  border-radius: 9px;
  font-size: 15px;
  font-weight: 700;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-menu-item.is-active) {
  color: #ffffff;
  background: var(--color-primary);
}

.sidebar-menu :deep(.el-icon) {
  font-size: 19px;
}

.sidebar-logout {
  width: 100%;
  height: 50px;
  margin-top: 18px;
  padding: 0 22px;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: #e5edf8;
  background: transparent;
  border: 1px solid rgba(229, 237, 248, 0.16);
  border-radius: 9px;
  cursor: pointer;
  font-weight: 700;
}

.sidebar-logout:hover {
  background: var(--color-sidebar-muted);
}

.admin-main {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.admin-header {
  height: 78px;
  padding: 0 38px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  background: rgba(255, 255, 255, 0.94);
  border-bottom: 1px solid var(--color-border);
}

.header-title h1 {
  margin: 0;
  color: var(--color-text);
  font-size: 28px;
  font-weight: 800;
  line-height: 1.2;
}

.header-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 18px;
}

.header-search {
  width: 320px;
  height: 42px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--color-text-muted);
  background: #f1f3ff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-control);
  font-size: 14px;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--color-text-regular);
  font-weight: 700;
}

.admin-user__avatar {
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-pill);
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  text-transform: uppercase;
}

.header-search__input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: var(--color-text);
}

.header-search__input::placeholder {
  color: var(--color-text-muted);
}

.admin-content {
  min-width: 0;
  flex: 1;
  padding: 40px 38px 56px;
  overflow-x: auto;
}

@media (max-width: 960px) {
  .admin-sidebar {
    width: 86px;
  }

  .sidebar-brand {
    justify-content: center;
    margin-right: 0;
    margin-left: 0;
  }

  .sidebar-brand span:last-child,
  .sidebar-menu :deep(.el-menu-item span),
  .sidebar-logout span {
    display: none;
  }

  .sidebar-menu :deep(.el-menu-item) {
    justify-content: center;
    padding: 0 !important;
  }

  .sidebar-logout {
    justify-content: center;
    padding: 0;
  }

  .admin-header {
    padding: 0 24px;
  }

  .header-search {
    display: none;
  }

  .admin-content {
    padding: 28px 24px 44px;
  }
}

@media (max-width: 640px) {
  .admin-layout {
    display: block;
  }

  .admin-sidebar {
    position: static;
    width: 100%;
    height: auto;
    padding: 14px 16px;
  }

  .sidebar-brand {
    margin-bottom: 14px;
    justify-content: flex-start;
  }

  .sidebar-menu {
    display: flex;
    overflow-x: auto;
  }

  .sidebar-menu :deep(.el-menu-item) {
    min-width: 56px;
  }

  .sidebar-logout {
    display: none;
  }

  .admin-header {
    height: auto;
    padding: 18px 16px;
    align-items: flex-start;
  }

  .header-title h1 {
    font-size: 24px;
  }

  .admin-user__name {
    display: none;
  }

  .admin-content {
    padding: 24px 16px 36px;
  }
}
</style>
