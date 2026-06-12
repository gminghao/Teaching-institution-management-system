<template>
  <div class="manage-page">
    <section class="manage-head">
      <div>
        <h2>课程管理</h2>
        <p>维护课程目录、价格和上线状态。</p>
      </div>
      <button type="button" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新增课程
      </button>
    </section>

    <section class="filter-card">
      <label>
        <el-icon><Search /></el-icon>
        <input v-model="searchKeyword" type="text" placeholder="搜索课程名称、讲师或分类...">
      </label>
      <select v-model="filterCategory" @change="loadCourses()">
        <option value="">全部分类</option>
        <option v-for="cat in categoryOptions" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
      </select>
      <select v-model="filterStatus" @change="loadCourses()">
        <option value="">所有状态</option>
        <option value="ONLINE">已上架</option>
        <option value="DRAFT">草稿</option>
        <option value="OFFLINE">已下架</option>
      </select>
    </section>

    <section class="table-card">
      <table>
        <thead>
          <tr>
            <th>课程</th>
            <th>分类</th>
            <th>讲师</th>
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
            <td>{{ course.teacherName }}</td>
            <td class="right strong">¥{{ course.price }}</td>
            <td>
              <span :class="['status-pill', courseStatusTone(course.status)]">
                {{ courseStatusMap[course.status] || course.status }}
              </span>
            </td>
            <td class="right action-cell">
              <span @click="openEditDialog(course)">编辑</span>
              <span v-if="course.status === 'DRAFT'" @click="handleOnline(course.id)">上架</span>
              <span v-else-if="course.status === 'ONLINE'" @click="handleOffline(course.id)">下架</span>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <section class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadCourses()"
        @size-change="loadCourses()"
      />
    </section>

    <!-- 新增/编辑课程弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? '编辑课程' : '新增课程'"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="courseForm"
        :rules="formRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="课程分类" prop="categoryId">
          <el-select v-model="courseForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categoryOptions"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程标题" prop="title">
          <el-input v-model="courseForm.title" placeholder="请输入课程标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="副标题" prop="subtitle">
          <el-input v-model="courseForm.subtitle" placeholder="请输入副标题" />
        </el-form-item>
        <el-form-item label="讲师姓名" prop="teacherName">
          <el-input v-model="courseForm.teacherName" placeholder="请输入讲师姓名" />
        </el-form-item>
        <el-form-item label="课程价格" prop="price">
          <el-input-number v-model="courseForm.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="报名费" prop="registrationFee">
          <el-input-number v-model="courseForm.registrationFee" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="courseForm.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ isEditMode ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, nextTick } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getAdminCourses,
  getCategories,
  createCourse,
  updateCourse,
  onlineCourse,
  offlineCourse
} from '@/api/admin'
import { courseStatusMap, courseStatusTone } from '@/utils/format'

const courses = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const filterCategory = ref('')
const filterStatus = ref('')
const categoryOptions = ref([])

// 弹窗相关
const dialogVisible = ref(false)
const isEditMode = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const emptyForm = () => ({
  categoryId: null,
  title: '',
  subtitle: '',
  teacherName: '',
  price: 0,
  registrationFee: 0,
  description: ''
})

const courseForm = ref(emptyForm())

const formRules = {
  categoryId: [{ required: true, message: '请选择课程分类', trigger: 'change' }],
  title: [{ required: true, message: '请输入课程标题', trigger: 'blur' }],
  price: [
    { required: true, message: '请输入课程价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能为负', trigger: 'blur' }
  ],
  registrationFee: [
    { required: true, message: '请输入报名费', trigger: 'blur' },
    { type: 'number', min: 0, message: '报名费不能为负', trigger: 'blur' }
  ]
}

// 防抖搜索
let searchTimer = null
watch(searchKeyword, () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    loadCourses()
  }, 300)
})

// 从API加载分类选项
async function loadCategories() {
  try {
    const res = await getCategories()
    if (res.code === 200) {
      categoryOptions.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to load categories:', e)
  }
}

const loadCourses = async () => {
  loading.value = true
  try {
    const res = await getAdminCourses({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
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

// 新增弹窗
function openCreateDialog() {
  isEditMode.value = false
  editingId.value = null
  courseForm.value = emptyForm()
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

// 编辑弹窗
function openEditDialog(course) {
  isEditMode.value = true
  editingId.value = course.id
  courseForm.value = {
    categoryId: course.categoryId,
    title: course.title,
    subtitle: course.subtitle || '',
    teacherName: course.teacherName || '',
    price: course.price != null ? Number(course.price) : 0,
    registrationFee: course.registrationFee != null ? Number(course.registrationFee) : 0,
    description: course.description || ''
  }
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

// 提交表单
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEditMode.value) {
      const res = await updateCourse(editingId.value, courseForm.value)
      if (res.code === 200) {
        ElMessage.success('课程更新成功')
        dialogVisible.value = false
        loadCourses()
      }
    } else {
      const res = await createCourse(courseForm.value)
      if (res.code === 200) {
        ElMessage.success('课程创建成功')
        dialogVisible.value = false
        loadCourses()
      }
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
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

onMounted(() => {
  loadCategories()
  loadCourses()
})
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
  min-width: 780px;
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

.action-cell span {
  cursor: pointer;
  margin-left: 12px;
}

.action-cell span:first-child {
  margin-left: 0;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 4px 0;
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
