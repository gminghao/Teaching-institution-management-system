<template>
  <div class="donut-wrapper">
    <div class="donut-chart" :class="{ visible: isVisible }">
      <svg viewBox="0 0 120 120" class="donut-svg">
        <circle v-if="total === 0" cx="60" cy="60" r="50" fill="none" stroke="#e5e7eb" stroke-width="16" />
        <circle v-if="total > 0 && paidRatio > 0" cx="60" cy="60" r="50" fill="none" stroke="#10b981" stroke-width="16" :stroke-dasharray="`${paidArc} ${circumference}`" stroke-dashoffset="0" class="donut-segment segment-0" />
        <circle v-if="total > 0 && unpaidRatio > 0" cx="60" cy="60" r="50" fill="none" stroke="#ef4444" stroke-width="16" :stroke-dasharray="`${unpaidArc} ${circumference}`" :stroke-dashoffset="-paidArc" class="donut-segment segment-1" />
        <circle v-if="total > 0 && partialRatio > 0" cx="60" cy="60" r="50" fill="none" stroke="#f59e0b" stroke-width="16" :stroke-dasharray="`${partialArc} ${circumference}`" :stroke-dashoffset="-(paidArc + unpaidArc)" class="donut-segment segment-2" />
      </svg>
      <div class="donut-center">
        <span class="donut-label">总计</span>
        <span class="donut-total">{{ total }}</span>
      </div>
    </div>

    <div class="status-list" :class="{ visible: isVisible }">
      <div class="status-row row-0">
        <span class="status-dot" style="background-color: #10b981" />
        <span class="status-name">已缴费</span>
        <span class="status-count">{{ paid }}</span>
        <span class="status-pct">{{ formatPct(paidRatio) }}</span>
      </div>
      <div class="status-row row-1">
        <span class="status-dot" style="background-color: #ef4444" />
        <span class="status-name">未缴费</span>
        <span class="status-count">{{ unpaid }}</span>
        <span class="status-pct">{{ formatPct(unpaidRatio) }}</span>
      </div>
      <div class="status-row row-2">
        <span class="status-dot" style="background-color: #f59e0b" />
        <span class="status-name">部分缴费</span>
        <span class="status-count">{{ partial }}</span>
        <span class="status-pct">{{ formatPct(partialRatio) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, nextTick } from 'vue'

const props = defineProps({
  paid: { type: Number, default: 0 },
  unpaid: { type: Number, default: 0 },
  partial: { type: Number, default: 0 },
})

const isVisible = ref(false)
const circumference = 2 * Math.PI * 50

const total = computed(() => props.paid + props.unpaid + props.partial)
const paidRatio = computed(() => (total.value > 0 ? props.paid / total.value : 0))
const unpaidRatio = computed(() => (total.value > 0 ? props.unpaid / total.value : 0))
const partialRatio = computed(() => (total.value > 0 ? props.partial / total.value : 0))
const paidArc = computed(() => paidRatio.value * circumference)
const unpaidArc = computed(() => unpaidRatio.value * circumference)
const partialArc = computed(() => partialRatio.value * circumference)

function formatPct(ratio) {
  return total.value === 0 ? '0%' : `${Math.round(ratio * 100)}%`
}

onMounted(() => { nextTick(() => { isVisible.value = true }) })
</script>

<style scoped>
.donut-wrapper {
  display: flex;
  align-items: center;
  gap: 2rem;
  padding: 1rem;
}
.donut-chart {
  position: relative;
  width: 140px;
  height: 140px;
  flex-shrink: 0;
  transform: rotate(-90deg);
}
.donut-svg { width: 100%; height: 100%; }
.donut-segment { opacity: 0; transition: opacity 0.4s ease; }
.donut-chart.visible .segment-0 { opacity: 1; transition-delay: 0ms; }
.donut-chart.visible .segment-1 { opacity: 1; transition-delay: 120ms; }
.donut-chart.visible .segment-2 { opacity: 1; transition-delay: 240ms; }
.donut-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transform: rotate(90deg);
}
.donut-label { font-size: 0.75rem; color: var(--color-text-muted); }
.donut-total { font-size: 1.5rem; font-weight: 700; color: var(--color-text); line-height: 1.2; }
.status-list { display: flex; flex-direction: column; gap: 0.625rem; min-width: 160px; }
.status-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.375rem 0.625rem;
  border-radius: var(--radius-pill);
  opacity: 0;
  transform: translateY(8px);
  transition: opacity 0.35s ease, transform 0.35s ease;
}
.status-row:hover { background-color: var(--color-surface-muted); }
.status-list.visible .row-0 { opacity: 1; transform: translateY(0); transition-delay: 200ms; }
.status-list.visible .row-1 { opacity: 1; transform: translateY(0); transition-delay: 300ms; }
.status-list.visible .row-2 { opacity: 1; transform: translateY(0); transition-delay: 400ms; }
.status-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.status-name { flex: 1; font-size: 0.875rem; color: var(--color-text-regular); }
.status-count { font-size: 0.875rem; font-weight: 600; color: var(--color-text); min-width: 1.5rem; text-align: right; }
.status-pct { font-size: 0.75rem; color: var(--color-text-muted); min-width: 2.25rem; text-align: right; }
@media (max-width: 480px) {
  .donut-wrapper { flex-direction: column; align-items: center; }
  .status-list { width: 100%; }
}
@media (prefers-reduced-motion: reduce) {
  .donut-segment, .status-row { transition: none !important; opacity: 1 !important; transform: none !important; }
}
</style>
