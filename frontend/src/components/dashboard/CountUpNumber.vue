<template>
  <span class="count-up">{{ displayValue }}</span>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'

const props = defineProps({
  target: {
    type: [Number, String],
    default: 0
  },
  duration: {
    type: Number,
    default: 800
  }
})

const displayValue = ref('0')

function animateTo(targetNum) {
  const end = targetNum
  if (end === 0) {
    displayValue.value = '0'
    return
  }

  const startTime = performance.now()
  const duration = props.duration

  function step(currentTime) {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    displayValue.value = String(Math.round(end * eased))
    if (progress < 1) {
      requestAnimationFrame(step)
    }
  }

  requestAnimationFrame(step)
}

onMounted(() => {
  const num = Number(props.target)
  if (!isNaN(num)) animateTo(num)
})

watch(
  () => props.target,
  (newVal) => {
    const num = Number(newVal)
    if (!isNaN(num)) animateTo(num)
  }
)
</script>

<style scoped>
.count-up {
  display: inline-block;
  font-variant-numeric: tabular-nums;
}
</style>
