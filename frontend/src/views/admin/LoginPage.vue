<template>
  <div class="login-page">
    <section class="login-visual">
      <span class="brand-mark">
        <el-icon><Reading /></el-icon>
      </span>
      <h1>Admin Portal</h1>
      <p>Institutional Excellence</p>
    </section>

    <section class="login-card surface-card">
      <div class="login-heading">
        <h2>管理员登录</h2>
        <p>进入后台管理课程、报名和财务数据。</p>
      </div>
      <form @submit.prevent="handleLogin">
        <label>
          <span>用户名</span>
          <input v-model="form.username" type="text" placeholder="admin">
        </label>
        <label>
          <span>密码</span>
          <input v-model="form.password" type="password" placeholder="admin123">
        </label>
        <button type="submit">登录</button>
      </form>
    </section>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Reading } from '@element-plus/icons-vue'
import { setToken, setUser } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

const form = reactive({
  username: 'admin',
  password: 'admin123'
})

const handleLogin = () => {
  setToken('mock-admin-token')
  setUser({ username: form.username, realName: '陈管理员' })
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/admin/dashboard'
  router.push(redirect)
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(420px, 1.05fr);
  background: var(--color-background);
}

.login-visual {
  padding: 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
  background:
    linear-gradient(rgba(17, 24, 39, 0.82), rgba(17, 24, 39, 0.9)),
    radial-gradient(circle at 25% 20%, rgba(15, 76, 129, 0.7), transparent 42%),
    var(--color-sidebar);
}

.brand-mark {
  width: 58px;
  height: 58px;
  margin-bottom: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary);
  border-radius: 16px;
  font-size: 30px;
}

.login-visual h1 {
  margin: 0;
  font-size: 42px;
  line-height: 1.1;
}

.login-visual p {
  margin: 10px 0 0;
  color: #cbd5e1;
  font-size: 18px;
}

.login-card {
  place-self: center;
  width: min(100% - 48px, 460px);
  padding: 42px;
}

.login-heading {
  margin-bottom: 28px;
}

.login-heading h2 {
  margin: 0 0 8px;
  color: var(--color-text);
  font-size: 30px;
}

.login-heading p {
  margin: 0;
  color: var(--color-text-muted);
}

form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: var(--color-text);
  font-weight: 800;
}

input {
  height: 46px;
  padding: 0 14px;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-control);
  outline: none;
}

input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(15, 76, 129, 0.12);
}

button {
  height: 48px;
  margin-top: 6px;
  color: #fff;
  background: var(--color-primary-deep);
  border: none;
  border-radius: var(--radius-control);
  cursor: pointer;
  font-weight: 800;
}

@media (max-width: 820px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-visual {
    min-height: 260px;
  }
}
</style>
