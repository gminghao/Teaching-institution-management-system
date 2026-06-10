<template>
  <div class="public-layout">
    <header class="public-header">
      <div class="header-content">
        <router-link to="/" class="logo">课程管理系统</router-link>
        <nav class="nav-links">
          <router-link to="/">首页</router-link>
          <router-link to="/courses">课程列表</router-link>
          <router-link to="/admin/login" class="admin-link">管理员登录</router-link>
        </nav>
      </div>
    </header>

    <main class="public-main">
      <router-view />
    </main>

    <footer class="public-footer">
      <p>&copy; 2026 教学机构课程管理系统</p>
    </footer>
  </div>
</template>

<script setup>
</script>

<style scoped>
.public-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.public-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  text-decoration: none;
}

.nav-links {
  display: flex;
  gap: 24px;
}

.nav-links a {
  text-decoration: none;
  color: #333;
  font-size: 15px;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  color: #409eff;
}

.admin-link {
  color: #909399 !important;
}

.public-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}

.public-footer {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
  border-top: 1px solid #eee;
}
</style>
