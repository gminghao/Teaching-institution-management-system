<template>
  <div class="public-layout">
    <header class="public-header">
      <div class="public-header__inner">
        <router-link to="/" class="brand" aria-label="返回首页">
          <span class="brand__mark">
            <el-icon><Reading /></el-icon>
          </span>
          <span>卓越学术机构</span>
        </router-link>

        <nav class="public-nav" aria-label="公共导航">
          <router-link to="/">首页</router-link>
          <router-link to="/courses">课程列表</router-link>
          <a href="#about">关于我们</a>
          <router-link to="/admin/login">管理登录</router-link>
        </nav>

        <el-button class="enroll-button" type="primary" @click="goEnrollment">
          立即报名
        </el-button>
      </div>
    </header>

    <main class="public-main">
      <router-view />
    </main>

    <footer id="about" class="public-footer">
      <div class="public-footer__inner">
        <section>
          <h2>卓越学术机构</h2>
          <p>通过严谨的课程体系与清晰的报名管理，帮助小型教学机构完成课程宣传、报名转化和收费统计。</p>
        </section>
        <nav aria-label="页脚导航">
          <router-link to="/">首页</router-link>
          <router-link to="/courses">课程列表</router-link>
          <router-link to="/admin/login">管理后台</router-link>
        </nav>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Reading } from '@element-plus/icons-vue'

const router = useRouter()

const goEnrollment = () => {
  router.push('/courses')
}
</script>

<style scoped>
.public-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-background);
}

.public-header {
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(194, 199, 209, 0.72);
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.05);
}

.public-header__inner {
  width: min(100% - 48px, var(--container-width));
  height: 68px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(180px, 1fr) auto minmax(180px, 1fr);
  align-items: center;
  gap: 24px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  width: fit-content;
  color: var(--color-primary-deep);
  font-size: 20px;
  font-weight: 800;
  text-decoration: none;
  letter-spacing: 0;
}

.brand__mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  color: var(--color-primary);
  background: #eef4ff;
  border-radius: var(--radius-control);
}

.public-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 30px;
  color: var(--color-text);
  font-size: 15px;
  font-weight: 650;
}

.public-nav a {
  position: relative;
  padding: 22px 0;
  color: inherit;
  text-decoration: none;
}

.public-nav a::after {
  position: absolute;
  right: 0;
  bottom: 15px;
  left: 0;
  height: 2px;
  content: "";
  background: var(--color-primary);
  border-radius: var(--radius-pill);
  opacity: 0;
  transform: scaleX(0.6);
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.public-nav a:hover,
.public-nav a.router-link-active {
  color: var(--color-primary-deep);
}

.public-nav a:hover::after,
.public-nav a.router-link-active::after {
  opacity: 1;
  transform: scaleX(1);
}

.enroll-button {
  justify-self: end;
  min-width: 108px;
  height: 40px;
}

.public-main {
  flex: 1;
}

.public-footer {
  border-top: 1px solid var(--color-border);
  background: #eef2ff;
}

.public-footer__inner {
  width: min(100% - 48px, var(--container-width));
  margin: 0 auto;
  padding: 48px 0;
  display: flex;
  justify-content: space-between;
  gap: 40px;
}

.public-footer h2 {
  margin: 0 0 12px;
  color: var(--color-primary-deep);
  font-size: 20px;
  line-height: 1.3;
}

.public-footer p {
  max-width: 520px;
  margin: 0;
  color: var(--color-text-muted);
  line-height: 1.7;
}

.public-footer nav {
  display: flex;
  gap: 28px;
  align-items: flex-start;
}

.public-footer a {
  color: var(--color-text-muted);
  font-weight: 600;
  text-decoration: none;
}

.public-footer a:hover {
  color: var(--color-primary);
}

@media (max-width: 900px) {
  .public-header__inner {
    height: auto;
    padding: 14px 0;
    grid-template-columns: 1fr auto;
  }

  .public-nav {
    grid-column: 1 / -1;
    order: 3;
    justify-content: flex-start;
    gap: 20px;
    overflow-x: auto;
    white-space: nowrap;
  }

  .public-nav a {
    padding: 8px 0;
  }

  .public-nav a::after {
    bottom: 2px;
  }
}

@media (max-width: 640px) {
  .public-header__inner,
  .public-footer__inner {
    width: min(100% - 32px, var(--container-width));
  }

  .brand {
    font-size: 18px;
  }

  .enroll-button {
    min-width: auto;
  }

  .public-footer__inner {
    flex-direction: column;
  }

  .public-footer nav {
    flex-wrap: wrap;
  }
}
</style>
