<template>
  <div class="courses-page">
    <div class="page-intro">
      <h1>探索课程</h1>
      <p>发现专为卓越学术和专业荣誉而设计的课程。</p>
    </div>

    <section class="course-toolbar">
      <label class="search-box">
        <el-icon><Search /></el-icon>
        <input v-model="keyword" type="text" placeholder="搜索课程、讲师或主题...">
      </label>

      <div class="category-tabs">
        <button
          v-for="category in courseCategories"
          :key="category"
          type="button"
          :class="{ active: category === activeCategory }"
          @click="activeCategory = category"
        >
          {{ category }}
        </button>
      </div>
    </section>

    <section class="course-grid">
      <article v-for="course in filteredCourses" :key="course.id" class="course-card">
        <router-link :to="`/courses/${course.id}`" class="image-wrap">
          <img :src="course.image" :alt="course.title">
          <span>{{ course.format }}</span>
        </router-link>

        <div class="card-body">
          <div class="meta-row">
            <span class="category">{{ course.category }}</span>
            <span class="duration">
              <el-icon><Clock /></el-icon>
              {{ course.duration }}
            </span>
          </div>

          <router-link :to="`/courses/${course.id}`" class="course-title">
            {{ course.title }}
          </router-link>
          <p>{{ course.description }}</p>

          <div class="course-footer">
            <div class="instructor">
              <span>{{ course.instructor.initials }}</span>
              <strong>{{ course.instructor.name }}</strong>
            </div>
            <strong class="price">{{ course.price }}</strong>
          </div>
        </div>
      </article>
    </section>

    <nav class="pagination" aria-label="课程分页">
      <button type="button" disabled>&lt;</button>
      <button type="button" class="active">1</button>
      <button type="button">2</button>
      <button type="button">3</button>
      <span>...</span>
      <button type="button">8</button>
      <button type="button">&gt;</button>
    </nav>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Clock, Search } from '@element-plus/icons-vue'
import { courseCategories, mockCourses } from '@/data/mock'

const keyword = ref('')
const activeCategory = ref('全部课程')

const filteredCourses = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase()

  return mockCourses.filter(course => {
    const categoryMatched = activeCategory.value === '全部课程' || course.category === activeCategory.value
    const keywordMatched = !normalizedKeyword ||
      course.title.toLowerCase().includes(normalizedKeyword) ||
      course.instructor.name.toLowerCase().includes(normalizedKeyword) ||
      course.category.toLowerCase().includes(normalizedKeyword)

    return categoryMatched && keywordMatched
  })
})
</script>

<style scoped>
.courses-page {
  width: min(100% - 48px, var(--container-width));
  margin: 0 auto;
  padding: 56px 0 80px;
}

.page-intro {
  margin-bottom: 40px;
}

.page-intro h1 {
  margin: 0 0 14px;
  color: var(--color-text);
  font-size: 40px;
  font-weight: 800;
  line-height: 1.2;
}

.page-intro p {
  max-width: 640px;
  margin: 0;
  color: var(--color-text-muted);
  font-size: 18px;
  line-height: 1.6;
}

.course-toolbar {
  margin-bottom: 48px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.search-box {
  position: relative;
  flex: 1;
  max-width: 520px;
  display: block;
}

.search-box .el-icon {
  position: absolute;
  top: 50%;
  left: 15px;
  color: var(--color-text-muted);
  font-size: 20px;
  transform: translateY(-50%);
}

.search-box input {
  width: 100%;
  height: 52px;
  padding: 0 16px 0 44px;
  color: var(--color-text);
  background: var(--color-surface-muted);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-control);
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.search-box input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(15, 76, 129, 0.12);
}

.category-tabs {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  overflow-x: auto;
  scrollbar-width: none;
}

.category-tabs::-webkit-scrollbar {
  display: none;
}

.category-tabs button {
  height: 42px;
  padding: 0 20px;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-pill);
  white-space: nowrap;
  cursor: pointer;
  font-weight: 650;
  transition: color 0.2s ease, background 0.2s ease, border 0.2s ease;
}

.category-tabs button.active {
  color: #fff;
  background: var(--color-primary-deep);
  border-color: var(--color-primary-deep);
}

.course-grid {
  margin-bottom: 64px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 32px;
}

.course-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 18px;
  transition: box-shadow 0.25s ease, transform 0.25s ease;
}

.course-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card);
}

.image-wrap {
  position: relative;
  height: 192px;
  display: block;
  overflow: hidden;
}

.image-wrap img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.course-card:hover .image-wrap img {
  transform: scale(1.05);
}

.image-wrap span {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 5px 10px;
  color: var(--color-primary);
  background: rgba(255, 255, 255, 0.92);
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.1);
  font-size: 12px;
  font-weight: 800;
}

.card-body {
  flex: 1;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.meta-row {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.category {
  padding: 5px 9px;
  color: var(--color-text);
  background: var(--color-surface-muted);
  border-radius: 5px;
  font-size: 11px;
  font-weight: 800;
}

.duration {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: 650;
}

.course-title {
  margin-bottom: 12px;
  color: var(--color-text);
  font-size: 20px;
  font-weight: 800;
  line-height: 1.35;
  text-decoration: none;
}

.course-title:hover {
  color: var(--color-primary);
}

.card-body p {
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
  gap: 16px;
  border-top: 1px solid var(--color-border);
}

.instructor {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.instructor span {
  width: 32px;
  height: 32px;
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: var(--color-primary-container);
  border-radius: var(--radius-pill);
  font-size: 12px;
  font-weight: 800;
}

.instructor strong {
  min-width: 0;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.price {
  color: var(--color-primary);
  font-size: 18px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.pagination button {
  width: 42px;
  height: 42px;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 7px;
  cursor: pointer;
  font-weight: 700;
}

.pagination button.active {
  color: #fff;
  background: var(--color-primary-deep);
  border-color: var(--color-primary-deep);
}

.pagination button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.pagination span {
  color: var(--color-text-muted);
}

@media (max-width: 1050px) {
  .course-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .search-box {
    max-width: none;
  }

  .category-tabs {
    width: 100%;
  }

  .course-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .courses-page {
    width: min(100% - 32px, var(--container-width));
    padding-top: 40px;
  }

  .page-intro h1 {
    font-size: 32px;
  }

  .course-grid {
    grid-template-columns: 1fr;
  }
}
</style>
