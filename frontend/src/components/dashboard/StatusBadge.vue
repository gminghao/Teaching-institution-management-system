<template>
  <span class="status-badge" :class="[toneClass, { 'is-animating': animating }]">
    {{ label }}
  </span>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'

const props = defineProps({
  status: {
    type: String,
    default: ''
  },
  label: {
    type: String,
    default: ''
  },
  tone: {
    type: String,
    default: 'info'
  }
})

const animating = ref(false)

const toneClass = computed(() => {
  const map = {
    success: 'badge-success',
    warning: 'badge-warning',
    danger: 'badge-danger',
    info: 'badge-info'
  }
  return map[props.tone] || 'badge-info'
})

watch(
  () => props.status,
  async () => {
    animating.value = true
    await nextTick()
    setTimeout(() => {
      animating.value = false
    }, 400)
  }
)
</script>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 5px 14px;
  border-radius: var(--radius-pill);
  font-size: 12px;
  font-weight: 700;
  line-height: 1.4;
  transition: background-color 0.35s ease, color 0.35s ease, transform 0.25s ease, box-shadow 0.25s ease;
  white-space: nowrap;
}

.status-badge.is-animating {
  animation: badge-pop 0.4s ease;
}

@keyframes badge-pop {
  0% {
    transform: scale(1);
  }
  40% {
    transform: scale(1.08);
  }
  100% {
    transform: scale(1);
  }
}

/* Pending - soft orange */
.badge-warning {
  color: #92400e;
  background: rgba(245, 158, 11, 0.13);
}

/* Contacted - soft blue */
.badge-info {
  color: #1e40af;
  background: rgba(59, 130, 246, 0.12);
}

/* Paid / Enrolled - soft green */
.badge-success {
  color: #065f46;
  background: rgba(16, 185, 129, 0.13);
}

/* Cancelled - soft gray-red */
.badge-danger {
  color: #991b1b;
  background: rgba(239, 68, 68, 0.12);
}
</style>
