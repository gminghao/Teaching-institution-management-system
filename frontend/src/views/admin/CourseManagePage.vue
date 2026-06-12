<template>
  <div class="manage-page">
    <section class="manage-head">
      <div>
        <h2>课程管理</h2>
        <p>维护课程目录、价格和上线状态。</p>
      </div>
      <button type="button">
        <el-icon><Plus /></el-icon>
        新增课程
      </button>
    </section>

    <section class="filter-card">
      <label>
        <el-icon><Search /></el-icon>
        <input type="text" placeholder="搜索课程名称、讲师或分类...">
      </label>
      <select>
        <option>全部分类</option>
        <option v-for="category in courseCategories.slice(1)" :key="category">{{ category }}</option>
      </select>
      <select>
        <option>所有状态</option>
        <option>已上架</option>
        <option>草稿</option>
        <option>已下架</option>
      </select>
    </section>

    <section class="table-card">
      <table>
        <thead>
          <tr>
            <th>课程</th>
            <th>分类</th>
            <th>讲师</th>
            <th>周期</th>
            <th class="right">价格</th>
            <th>状态</th>
            <th class="right">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="course in courses" :key="course.id">
            <td>
              <div class="course-cell">
                <div>
                  <strong>{{ course.title }}</strong>
                  <small>{{ course.description }}</small>
                </div>
              </div>
            </td>
            <td>{{ course.categoryName }}</td>
            <td>{{ course.instructor }}</td>
            <td>{{ course.duration }}</td>
            <td class="right strong">¥{{ course.price }}</td>
            <td>
              <span :class="['status-pill', courseStatusTone(course.status)]">
                {{ courseStatusMap[course.status] || course.status }}
              </span>
            </td>
            <td class="right action-cell">
              <span v-if="course.status === 'DRAFT'" @click="handleOnline(course.id)">上架</span>
              <span v-else-if="course.status === 'ONLINE'" @click="handleOffline(course.id)">下架</span>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAdminCourses, onlineCourse, offlineCourse } from '@/api/admin'
import { courseStatusMap, courseStatusTone } from '@/utils/format'

const courses = ref([])
const loading = ref(false)
const currentPage = ref(1)
const total = ref(0)
const searchKeyword = ref('')
const filterCategory = ref('')
const filterStatus = ref('')
const courseCategories = ref(['全部分类'])

const loadCourses = async () => {
  loading.value = true
  try {
    const res = await getAdminCourses({
      pageNum: currentPage.value,
      pageSize: 10,
      keyword: searchKeyword.value || undefined,
      categoryId: filterCategory.value || undefined,
      status: filterStatus.value || undefined
    })
    if (res.code === 200) {
      courses.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('Failed to load courses:', e)
  } finally {
    loading.value = false
  }
}

const handleOnline = async (id) => {
  try {
    const res = await onlineCourse(id)
    if (res.code === 200) {
      ElMessage.success('上架成功')
      loadCourses()
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const handleOffline = async (id) => {
  try {
    const res = await offlineCourse(id)
    if (res.code === 200) {
      ElMessage.success('下架成功')
      loadCourses()
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(loadCourses)
</script>

<style scoped>
.manage-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.manage-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
}

.manage-head h2 {
  margin: 0 0 8px;
  color: var(--color-text);
  font-size: 30px;
}

.manage-head p {
  margin: 0;
  color: var(--color-text-muted);
}

.manage-head button {
  height: 44px;
  padding: 0 18px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  background: var(--color-primary-deep);
  border: none;
  border-radius: var(--radius-control);
  cursor: pointer;
  font-weight: 800;
}

.filter-card {
  padding: 20px;
  display: grid;
  grid-template-columns: minmax(260px, 1fr) 180px 180px;
  gap: 14px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 18px;
}

.filter-card label {
  position: relative;
}

.filter-card .el-icon {
  position: absolute;
  top: 50%;
  left: 13px;
  color: var(--color-text-muted);
  transform: translateY(-50%);
}

input,
select {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  color: var(--color-text);
  background: var(--color-surface-muted);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-control);
  outline: none;
}

label input {
  padding-left: 40px;
}

.table-card {
  overflow-x: auto;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 18px;
  box-shadow: var(--shadow-subtle);
}

table {
  width: 100%;
  min-width: 980px;
  border-collapse: collapse;
}

th,
td {
  padding: 18px 22px;
  text-align: left;
  border-bottom: 1px solid var(--color-border);
}

th {
  color: var(--color-text-muted);
  background: var(--color-surface-muted);
  font-size: 13px;
}

.course-cell {
  display: flex;
  align-items: center;
  gap: 14px;
}

.course-cell img {
  width: 72px;
  height: 48px;
  object-fit: cover;
  border-radius: 8px;
}

.course-cell strong,
.course-cell small {
  display: block;
}

.course-cell small {
  max-width: 360px;
  margin-top: 4px;
  color: var(--color-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.right {
  text-align: right;
}

.strong {
  font-weight: 800;
}

.status-pill {
  display: inline-flex;
  padding: 5px 12px;
  border-radius: var(--radius-pill);
  font-size: 12px;
  font-weight: 800;
}

.status-pill.success {
  color: #047857;
  background: rgba(16, 185, 129, 0.12);
}

.status-pill.warning {
  color: #b45309;
  background: rgba(245, 158, 11, 0.14);
}

.action-cell {
  color: var(--color-primary);
  font-weight: 800;
}

@media (max-width: 900px) {
  .filter-card {
    grid-template-columns: 1fr;
  }

  .manage-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
