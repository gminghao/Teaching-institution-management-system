<template>
  <div class="detail-page">
    <nav class="breadcrumb" aria-label="面包屑">
      <router-link to="/">首页</router-link>
      <span>/</span>
      <router-link to="/courses">课程列表</router-link>
      <span>/</span>
      <strong>{{ course.title }}</strong>
    </nav>

    <section class="detail-hero">
      <div class="detail-image">
        <img :src="course.image" :alt="course.title">
        <span>{{ course.format }}</span>
      </div>
      <div class="detail-info">
        <div class="meta-line">
          <span>{{ course.category }}</span>
          <span>
            <el-icon><Clock /></el-icon>
            {{ course.duration }}
          </span>
        </div>
        <h1>{{ course.title }}</h1>
        <p>{{ course.description }}</p>
        <div class="teacher-row">
          <span>{{ course.instructor.initials }}</span>
          <div>
            <small>授课教师</small>
            <strong>{{ course.instructor.name }}</strong>
          </div>
        </div>
        <div class="price-panel">
          <div>
            <small>课程费用</small>
            <strong>{{ course.price }}</strong>
          </div>
          <div>
            <small>报名费</small>
            <strong>{{ course.registrationFee }}</strong>
          </div>
        </div>
        <router-link :to="`/enroll/${course.id}`" class="enroll-link">
          立即报名
          <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
    </section>

    <section class="detail-content surface-card">
      <h2>课程介绍</h2>
      <p>
        本课程采用结构化教学路径，将核心理论、案例研讨和实践任务结合起来，帮助学员在短周期内形成可复用的专业能力。
      </p>
      <div class="content-grid">
        <article>
          <h3>学习目标</h3>
          <p>建立系统知识框架，理解课程主题在真实业务和学术场景中的应用方式。</p>
        </article>
        <article>
          <h3>适合人群</h3>
          <p>适合希望提升专业竞争力、准备转岗或补足系统知识结构的学员。</p>
        </article>
        <article>
          <h3>报名说明</h3>
          <p>提交报名后，管理员会联系确认课程安排和缴费状态，过程保持简单透明。</p>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowRight, Clock } from '@element-plus/icons-vue'
import { mockCourses } from '@/data/mock'

const route = useRoute()

const course = computed(() => {
  return mockCourses.find(item => item.id === route.params.id) || mockCourses[0]
})
</script>

<style scoped>
.detail-page {
  width: min(100% - 48px, var(--container-width));
  margin: 0 auto;
  padding: 40px 0 80px;
}

.breadcrumb {
  margin-bottom: 28px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--color-text-muted);
  font-size: 14px;
}

.breadcrumb a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 700;
}

.breadcrumb strong {
  color: var(--color-text);
}

.detail-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(360px, 0.95fr);
  gap: 36px;
  align-items: stretch;
}

.detail-image {
  position: relative;
  min-height: 460px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: 22px;
  background: var(--color-surface);
}

.detail-image img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.detail-image span {
  position: absolute;
  top: 22px;
  right: 22px;
  padding: 6px 12px;
  color: var(--color-primary);
  background: rgba(255, 255, 255, 0.92);
  border-radius: 7px;
  font-weight: 800;
}

.detail-info {
  padding: 36px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 22px;
  box-shadow: var(--shadow-subtle);
}

.meta-line {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.meta-line span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 10px;
  color: var(--color-primary);
  background: var(--color-surface-muted);
  border-radius: 7px;
  font-size: 12px;
  font-weight: 800;
}

.detail-info h1 {
  margin: 0 0 16px;
  color: var(--color-text);
  font-size: 38px;
  line-height: 1.18;
}

.detail-info p {
  margin: 0 0 28px;
  color: var(--color-text-muted);
  font-size: 16px;
  line-height: 1.75;
}

.teacher-row {
  margin-bottom: 28px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.teacher-row > span {
  width: 46px;
  height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: var(--color-primary-container);
  border-radius: var(--radius-pill);
  font-weight: 800;
}

small {
  display: block;
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: 700;
}

.teacher-row strong {
  color: var(--color-text);
  font-size: 16px;
}

.price-panel {
  margin-bottom: 30px;
  padding: 22px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  background: var(--color-surface-muted);
  border-radius: 16px;
}

.price-panel strong {
  display: block;
  margin-top: 6px;
  color: var(--color-primary);
  font-size: 26px;
}

.enroll-link {
  width: 100%;
  height: 52px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  background: var(--color-primary-deep);
  border-radius: var(--radius-control);
  font-weight: 800;
  text-decoration: none;
}

.detail-content {
  margin-top: 36px;
  padding: 34px;
}

.detail-content h2 {
  margin: 0 0 14px;
  font-size: 26px;
}

.detail-content > p {
  max-width: 820px;
  margin: 0 0 28px;
  color: var(--color-text-muted);
  line-height: 1.75;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.content-grid article {
  padding: 20px;
  background: var(--color-surface-muted);
  border-radius: 14px;
}

.content-grid h3 {
  margin: 0 0 8px;
}

.content-grid p {
  margin: 0;
  color: var(--color-text-muted);
  line-height: 1.65;
}

@media (max-width: 900px) {
  .detail-hero,
  .content-grid {
    grid-template-columns: 1fr;
  }

  .detail-image {
    min-height: 320px;
  }
}

@media (max-width: 640px) {
  .detail-page {
    width: min(100% - 32px, var(--container-width));
  }

  .detail-info {
    padding: 24px;
  }

  .detail-info h1 {
    font-size: 30px;
  }

  .price-panel {
    grid-template-columns: 1fr;
  }
}
</style>
