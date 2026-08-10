<template>
  <div class="request-state-flow">
    <div
      v-for="(item, index) in requestStatuses"
      :key="item"
      class="request-state-flow__step"
      :class="{ active: item === status, complete: index < activeIndex }"
    >
      <span class="step-dot">{{ index + 1 }}</span>
      <span class="step-label">{{ item }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { requestStatuses } from '@/data/requestOptions'
import type { RequestStatus } from '@/types/requests'

const props = defineProps<{
  status: RequestStatus
}>()

const activeIndex = computed(() => requestStatuses.indexOf(props.status))
</script>

<style scoped lang="scss">
.request-state-flow {
  display: flex;
  align-items: center;
  overflow-x: auto;
  padding: 10px 2px 2px;
}

.request-state-flow__step {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: max-content;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 700;
}

.request-state-flow__step:not(:last-child) {
  padding-right: 34px;
}

.request-state-flow__step:not(:last-child)::after {
  content: '';
  position: absolute;
  left: calc(100% - 26px);
  width: 20px;
  height: 1px;
  background: #dbe3ea;
}

.step-dot {
  display: grid;
  place-items: center;
  width: 24px;
  height: 24px;
  border-radius: 999px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 11px;
}

.request-state-flow__step.complete .step-dot,
.request-state-flow__step.active .step-dot {
  background: #d97706;
  color: #fff;
}

.request-state-flow__step.complete,
.request-state-flow__step.active {
  color: #1f2937;
}
</style>
