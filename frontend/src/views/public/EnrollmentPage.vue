<template>
  <div class="enrollment-page">
    <section class="enrollment-card surface-card">
      <div class="course-summary">
        <img :src="course.image" :alt="course.title">
        <div>
          <span>{{ course.category }} · {{ course.duration }}</span>
          <h1>{{ course.title }}</h1>
          <p>{{ course.description }}</p>
          <div class="summary-price">
            <strong>{{ course.price }}</strong>
            <small>报名费 {{ course.registrationFee }}</small>
          </div>
        </div>
      </div>

      <form class="enrollment-form" @submit.prevent="handleSubmit">
        <div class="form-heading">
          <h2>提交报名信息</h2>
          <p>填写基础联系方式，管理员会在后台报名管理中跟进确认。</p>
        </div>

        <label>
          <span>姓名 *</span>
          <input v-model="form.studentName" type="text" placeholder="请输入姓名" required>
        </label>
        <label>
          <span>手机号 *</span>
          <input v-model="form.studentPhone" type="tel" placeholder="13800138000" required>
        </label>
        <label>
          <span>邮箱</span>
          <input v-model="form.studentEmail" type="email" placeholder="student@example.com">
        </label>
        <label>
          <span>备注</span>
          <textarea v-model="form.remark" rows="4" placeholder="希望周末班或其他报名说明" />
        </label>

        <button type="submit">提交报名</button>
        <p v-if="submitted" class="submit-result">
          报名请求已记录，模拟订单号 EN-202606-001。后续可在管理后台查看报名与缴费状态。
        </p>
      </form>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { mockCourses } from '@/data/mock'

const route = useRoute()
const submitted = ref(false)

const form = reactive({
  studentName: '',
  studentPhone: '',
  studentEmail: '',
  remark: ''
})

const course = computed(() => {
  return mockCourses.find(item => item.id === route.params.courseId) || mockCourses[0]
})

const handleSubmit = () => {
  submitted.value = true
}
</script>

<style scoped>
.enrollment-page {
  width: min(100% - 48px, 1120px);
  margin: 0 auto;
  padding: 56px 0 80px;
}

.enrollment-card {
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(360px, 1.05fr);
  gap: 0;
  overflow: hidden;
}

.course-summary {
  padding: 34px;
  display: flex;
  flex-direction: column;
  background: var(--color-surface-muted);
}

.course-summary img {
  width: 100%;
  height: 260px;
  margin-bottom: 28px;
  display: block;
  object-fit: cover;
  border-radius: 16px;
}

.course-summary span {
  color: var(--color-primary);
  font-size: 13px;
  font-weight: 800;
}

.course-summary h1 {
  margin: 10px 0 14px;
  color: var(--color-text);
  font-size: 30px;
  line-height: 1.25;
}

.course-summary p {
  margin: 0 0 28px;
  color: var(--color-text-muted);
  line-height: 1.75;
}

.summary-price {
  margin-top: auto;
  padding-top: 22px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-top: 1px solid var(--color-border);
}

.summary-price strong {
  color: var(--color-primary);
  font-size: 30px;
}

.summary-price small {
  color: var(--color-text-muted);
  font-weight: 700;
}

.enrollment-form {
  padding: 42px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  background: var(--color-surface);
}

.form-heading {
  margin-bottom: 8px;
}

.form-heading h2 {
  margin: 0 0 8px;
  font-size: 28px;
}

.form-heading p {
  margin: 0;
  color: var(--color-text-muted);
  line-height: 1.6;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: var(--color-text);
  font-weight: 700;
}

label span {
  font-size: 14px;
}

input,
textarea {
  width: 100%;
  padding: 13px 14px;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-control);
  outline: none;
  resize: vertical;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

input:focus,
textarea:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(15, 76, 129, 0.12);
}

button {
  height: 52px;
  color: #fff;
  background: var(--color-primary-deep);
  border: none;
  border-radius: var(--radius-control);
  cursor: pointer;
  font-weight: 800;
}

.submit-result {
  margin: 0;
  padding: 14px 16px;
  color: #047857;
  background: rgba(16, 185, 129, 0.12);
  border-radius: var(--radius-control);
  line-height: 1.6;
}

@media (max-width: 900px) {
  .enrollment-card {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .enrollment-page {
    width: min(100% - 32px, 1120px);
  }

  .course-summary,
  .enrollment-form {
    padding: 24px;
  }
}
</style>
