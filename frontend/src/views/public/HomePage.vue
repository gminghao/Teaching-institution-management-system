<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="hero-bg" :style="{ backgroundImage: `url(${referenceImages.heroCampus})` }" />
      <div class="hero-content">
        <span class="hero-badge">
          <el-icon><Trophy /></el-icon>
          卓越教育
        </span>
        <h1>培养全球领导力人才</h1>
        <p>
          卓越学术机构提供由世界一流教员指导的严谨、系统设计的课程。在无干扰、学术氛围浓厚的环境中提升您的潜力。
        </p>
        <div class="hero-actions">
          <router-link to="/courses" class="primary-link">
            探索课程
            <el-icon><ArrowRight /></el-icon>
          </router-link>
          <router-link to="/courses" class="secondary-link">查看课程表</router-link>
        </div>
      </div>
    </section>

    <section class="features-section">
      <div class="section-inner">
        <div class="section-heading centered">
          <h2>我们的优势</h2>
          <p>专为无妥协的学术成功而设计的框架。</p>
        </div>
        <div class="feature-grid">
          <article v-for="feature in features" :key="feature.title" class="feature-card">
            <div class="feature-icon">
              <el-icon><component :is="feature.icon" /></el-icon>
            </div>
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.desc }}</p>
          </article>
        </div>
      </div>
    </section>

    <section class="courses-section">
      <div class="section-inner">
        <div class="section-heading split">
          <div>
            <h2>热门课程</h2>
            <p>本学期最受欢迎的课程。</p>
          </div>
          <router-link to="/courses" class="view-all">
            查看全部
            <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>

        <div class="course-grid">
          <router-link
            v-for="course in popularCourses"
            :key="course.id"
            :to="`/courses/${course.id}`"
            class="course-card"
          >
            <div class="course-image">
              <img :src="course.image" :alt="course.title">
              <span>{{ course.format }}</span>
            </div>
            <div class="course-body">
              <h3>{{ course.title }}</h3>
              <p>{{ course.description }}</p>
              <div class="course-footer">
                <strong>{{ course.price }}</strong>
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ArrowRight, Check, CreditCard, Promotion, Trophy, User } from '@element-plus/icons-vue'
import { getCourses } from '@/api/public'
import { toDisplayCourse } from '@/utils/coursePresenter'
import { referenceImages } from '@/data/mock'

const features = [
  { icon: User, title: '专业师资', desc: '直接向行业领导者和杰出的终身教授学习。' },
  { icon: Check, title: '系统化课程', desc: '逻辑严密的课程体系确保全面掌握学科知识。' },
  { icon: CreditCard, title: '透明定价', desc: '清晰、预先支付的学费，无任何隐藏的机构费用。' },
  { icon: Promotion, title: '轻松入学', desc: '简化、顺畅的申请流程，全程在线管理。' }
]

const popularCourses = ref([])

onMounted(async () => {
  try {
    const res = await getCourses({ pageNum: 1, pageSize: 4 })
    if (res.code === 200) {
      popularCourses.value = (res.data.list || []).map(toDisplayCourse)
    }
  } catch (e) {
    console.error('Failed to load courses:', e)
  }
})
</script>

<style scoped>
.home-page {
  width: 100%;
}

.hero-section {
  position: relative;
  min-height: 700px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: var(--color-surface);
}

.hero-bg {
  position: absolute;
  inset: 0;
  background-position: center;
  background-size: cover;
  opacity: 0.1;
}

.hero-content {
  position: relative;
  z-index: 1;
  width: min(100% - 48px, var(--container-width));
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  padding: 7px 16px;
  color: var(--color-primary);
  background: rgba(15, 76, 129, 0.1);
  border: 1px solid rgba(15, 76, 129, 0.2);
  border-radius: var(--radius-pill);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
  font-size: 14px;
  font-weight: 600;
}

.hero-content h1 {
  max-width: 900px;
  margin: 0 0 24px;
  color: var(--color-text);
  font-size: clamp(40px, 6vw, 52px);
  font-weight: 800;
  line-height: 1.16;
  letter-spacing: 0;
}

.hero-content p {
  max-width: 720px;
  margin: 0 0 48px;
  color: var(--color-text-muted);
  font-size: 20px;
  line-height: 1.65;
}

.hero-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.primary-link,
.secondary-link {
  min-height: 52px;
  padding: 0 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: var(--radius-control);
  font-size: 18px;
  font-weight: 700;
  text-decoration: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.primary-link {
  color: #fff;
  background: var(--color-primary-deep);
  box-shadow: 0 10px 22px rgba(0, 53, 95, 0.2);
}

.primary-link:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 30px rgba(0, 53, 95, 0.24);
}

.secondary-link {
  color: var(--color-primary);
  background: transparent;
  border: 1px solid var(--color-border);
}

.secondary-link:hover {
  background: var(--color-surface-muted);
  border-color: var(--color-primary);
}

.features-section {
  padding: 96px 0;
  background: var(--color-surface-muted);
}

.courses-section {
  padding: 96px 0;
  background: var(--color-surface);
}

.section-inner {
  width: min(100% - 48px, var(--container-width));
  margin: 0 auto;
}

.section-heading {
  margin-bottom: 48px;
}

.section-heading.centered {
  text-align: center;
}

.section-heading.split {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
}

.section-heading h2 {
  margin: 0 0 10px;
  color: var(--color-text);
  font-size: 32px;
  font-weight: 800;
}

.section-heading p {
  margin: 0;
  color: var(--color-text-muted);
}

.feature-grid,
.course-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 32px;
}

.course-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.feature-card {
  padding: 32px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 18px;
  transition: box-shadow 0.25s ease, transform 0.25s ease;
}

.feature-card:hover,
.course-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card);
}

.feature-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  background: var(--color-surface-muted);
  border-radius: var(--radius-control);
  font-size: 22px;
  transition: color 0.2s ease, background 0.2s ease;
}

.feature-card:hover .feature-icon {
  color: #fff;
  background: var(--color-primary);
}

.feature-card h3 {
  margin: 0 0 8px;
  font-size: 20px;
}

.feature-card p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 14px;
  line-height: 1.6;
}

.view-all {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--color-primary);
  font-weight: 700;
  text-decoration: none;
}

.course-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: inherit;
  text-decoration: none;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 18px;
  transition: box-shadow 0.25s ease, transform 0.25s ease;
}

.course-image {
  position: relative;
  height: 192px;
  overflow: hidden;
}

.course-image img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.course-card:hover .course-image img {
  transform: scale(1.05);
}

.course-image span {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 4px 9px;
  color: var(--color-primary);
  background: rgba(255, 255, 255, 0.92);
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.1);
  font-size: 12px;
  font-weight: 800;
}

.course-body {
  flex: 1;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.course-body h3 {
  margin: 0 0 8px;
  font-size: 18px;
  line-height: 1.35;
}

.course-body p {
  flex: 1;
  margin: 0 0 24px;
  color: var(--color-text-muted);
  font-size: 14px;
  line-height: 1.65;
}

.course-footer {
  padding-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid var(--color-border);
}

.course-footer strong {
  font-size: 18px;
}

.course-footer .el-icon {
  color: var(--color-text-muted);
}

.course-card:hover .course-footer .el-icon {
  color: var(--color-primary);
}

@media (max-width: 960px) {
  .feature-grid,
  .course-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .hero-section {
    min-height: 620px;
  }

  .section-inner,
  .hero-content {
    width: min(100% - 32px, var(--container-width));
  }

  .features-section,
  .courses-section {
    padding: 72px 0;
  }

  .feature-grid,
  .course-grid {
    grid-template-columns: 1fr;
  }

  .section-heading.split {
    display: block;
  }

  .view-all {
    margin-top: 16px;
  }
}
</style>
