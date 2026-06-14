<template>
  <div class="search-suggestion" v-if="visible">
    <ul class="suggestion-list" v-if="suggestions.length > 0">
      <li
        v-for="(item, index) in suggestions"
        :key="index"
        class="suggestion-item"
        @mousedown.prevent="handleSelect(item)"
      >
        <el-icon class="suggestion-icon"><Search /></el-icon>
        <div class="suggestion-content">
          <span class="suggestion-text" v-html="highlightMatch(item.text)"></span>
          <span class="suggestion-type">{{ item.type }}</span>
        </div>
      </li>
    </ul>
    <div class="suggestion-empty" v-else>
      暂无匹配结果
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted, onBeforeUnmount } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getDashboardOverview } from '@/api/admin'

const props = defineProps({
  keyword: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['select'])

const allItems = ref([])
const visible = ref(false)

// Load data for suggestions
async function loadSuggestionData() {
  try {
    const res = await getDashboardOverview()
    if (res.code === 200 && res.data) {
      const items = []
      // Add course names
      if (res.data.recentEnrollments) {
        res.data.recentEnrollments.forEach((e) => {
          if (e.courseTitle) {
            items.push({ text: e.courseTitle, type: '课程', route: '/admin/courses' })
          }
          if (e.orderNo) {
            items.push({ text: e.orderNo, type: '订单', route: '/admin/enrollments' })
          }
          if (e.studentName) {
            items.push({ text: e.studentName, type: '学员', route: '/admin/enrollments' })
          }
        })
      }
      // Deduplicate
      const seen = new Set()
      allItems.value = items.filter((item) => {
        const key = item.text + item.type
        if (seen.has(key)) return false
        seen.add(key)
        return true
      })
    }
  } catch {
    // silently fail
  }
}

const suggestions = computed(() => {
  const kw = props.keyword.trim().toLowerCase()
  if (!kw) return []
  const matches = allItems.value.filter((item) =>
    item.text.toLowerCase().includes(kw)
  )
  return matches.slice(0, 8)
})

watch(
  () => props.keyword,
  (val) => {
    visible.value = val.trim().length > 0
  }
)

function handleSelect(item) {
  emit('select', item)
  visible.value = false
}

function highlightMatch(text) {
  const kw = props.keyword.trim()
  if (!kw) return text
  const regex = new RegExp(`(${kw.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

function handleClickOutside(e) {
  const el = e.target.closest('.search-suggestion')
  if (!el) {
    visible.value = false
  }
}

onMounted(() => {
  loadSuggestionData()
  document.addEventListener('mousedown', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})
</script>

<style scoped>
.search-suggestion {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-control);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.1);
  z-index: 200;
  animation: fadeSlideIn 0.2s ease;
  overflow: hidden;
}

@keyframes fadeSlideIn {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.suggestion-list {
  list-style: none;
  margin: 0;
  padding: 6px 0;
  max-height: 280px;
  overflow-y: auto;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.suggestion-item:hover {
  background: var(--color-surface-muted);
}

.suggestion-icon {
  color: var(--color-text-muted);
  font-size: 14px;
  flex-shrink: 0;
}

.suggestion-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.suggestion-text {
  font-size: 14px;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.suggestion-text :deep(mark) {
  color: var(--color-primary);
  background: rgba(15, 76, 129, 0.1);
  border-radius: 2px;
  padding: 0 1px;
}

.suggestion-type {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--color-text-muted);
  background: var(--color-surface-muted);
  padding: 2px 8px;
  border-radius: var(--radius-pill);
}

.suggestion-empty {
  padding: 20px 16px;
  text-align: center;
  color: var(--color-text-muted);
  font-size: 14px;
}
</style>
