<template>
  <span class="count-up-number">{{ displayText }}</span>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  value: { type: Number, default: 0 },
  prefix: { type: String, default: '' },
  suffix: { type: String, default: '' },
  decimals: { type: Number, default: 0 },
  duration: { type: Number, default: 800 }
})

const currentDisplay = ref(0)
let animationFrameId = null

function prefersReducedMotion() {
  if (typeof window === 'undefined') return true
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

function formatNumber(n) {
  const fixed = n.toFixed(props.decimals)
  return fixed.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const displayText = computed(() => {
  return props.prefix + formatNumber(currentDisplay.value) + props.suffix
})

function easeOutCubic(progress) {
  return 1 - Math.pow(1 - progress, 3)
}

function animate(from, to) {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
    animationFrameId = null
  }
  if (from === to) { currentDisplay.value = to; return }
  if (prefersReducedMotion()) { currentDisplay.value = to; return }

  const startTime = performance.now()
  const durationMs = props.duration

  function step(currentTime) {
    const elapsed = currentTime - startTime
    const rawProgress = Math.min(elapsed / durationMs, 1)
    const easedProgress = easeOutCubic(rawProgress)
    currentDisplay.value = from + (to - from) * easedProgress
    if (rawProgress < 1) {
      animationFrameId = requestAnimationFrame(step)
    } else {
      currentDisplay.value = to
      animationFrameId = null
    }
  }
  animationFrameId = requestAnimationFrame(step)
}

onMounted(() => {
  if (props.value > 0) animate(0, props.value)
})

watch(() => props.value, (newVal, oldVal) => {
  animate(oldVal ?? 0, newVal)
})

onBeforeUnmount(() => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
    animationFrameId = null
  }
})
</script>

<style scoped>
.count-up-number {
  font-variant-numeric: tabular-nums;
}
</style>
