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
        </nav>

        <el-button class="enroll-button" type="primary" @click="goEnrollment">
          立即报名
        </el-button>
      </div>
    </header>

    <main class="public-main">
      <router-view />
    </main>

    <footer class="public-footer">
      <div class="public-footer__inner">
        <section>
          <h2>卓越学术机构</h2>
          <p>通过严谨的学术卓越表现培养下一代行业领导者。</p>
          <small>&copy; 2026 Academic Distinction Institute. 保留所有权利。</small>
        </section>
        <div class="footer-column">
          <h3>机构</h3>
          <router-link to="/">关于我们</router-link>
          <router-link to="/">领导团队</router-link>
        </div>
        <div class="footer-column">
          <h3>资源</h3>
          <router-link to="/">校园地图</router-link>
          <router-link to="/">学生门户</router-link>
        </div>
        <div class="footer-column">
          <h3>法律</h3>
          <router-link to="/">联系我们</router-link>
          <router-link to="/">隐私政策</router-link>
        </div>
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
  padding: 64px 0;
  display: grid;
  grid-template-columns: 1.15fr repeat(3, minmax(150px, 1fr));
  gap: 32px;
}

.public-footer h2 {
  margin: 0 0 12px;
  color: var(--color-primary-deep);
  font-size: 20px;
  line-height: 1.3;
}

.public-footer p {
  margin: 0;
  color: var(--color-text-muted);
  line-height: 1.7;
}

.public-footer small {
  display: block;
  margin-top: 24px;
  color: var(--color-text-muted);
}

.footer-column {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.footer-column h3 {
  margin: 0;
  color: var(--color-text);
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0;
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
    grid-template-columns: 1fr;
  }
}
</style>
