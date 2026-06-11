<template>
  <main class="not-found-page">
    <section class="not-found-card surface-card">
      <p class="eyebrow">404 / Page not found</p>
      <h1>页面暂未开放</h1>
      <p class="description">
        当前地址没有匹配到可访问页面。你可以返回首页继续浏览课程，或回到上一页检查入口是否正确。
      </p>
      <div class="actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </section>
  </main>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  padding: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    linear-gradient(rgba(249, 250, 251, 0.9), rgba(249, 250, 251, 0.96)),
    radial-gradient(circle at 20% 20%, rgba(15, 76, 129, 0.18), transparent 32%),
    var(--color-background);
}

.not-found-card {
  width: min(100%, 620px);
  padding: 48px;
  text-align: center;
}

.eyebrow {
  width: fit-content;
  margin: 0 auto 18px;
  padding: 6px 14px;
  color: var(--color-primary);
  background: rgba(15, 76, 129, 0.1);
  border-radius: var(--radius-pill);
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0;
}

h1 {
  margin: 0;
  color: var(--color-text);
  font-size: 40px;
  line-height: 1.2;
}

.description {
  max-width: 460px;
  margin: 18px auto 30px;
  color: var(--color-text-muted);
  font-size: 16px;
  line-height: 1.7;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

@media (max-width: 640px) {
  .not-found-page {
    padding: 20px;
  }

  .not-found-card {
    padding: 32px 20px;
  }

  h1 {
    font-size: 30px;
  }

  .actions {
    flex-direction: column;
  }
}
</style>
