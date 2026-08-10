<template>
  <span class="request-status-tag" :style="{ '--status-color': color }">
    <span class="request-status-tag__dot"></span>
    {{ status }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { statusColorMap } from '@/data/requestOptions'
import type { RequestStatus } from '@/types/requests'

const props = defineProps<{
  status: RequestStatus
}>()

const color = computed(() => statusColorMap[props.status])
</script>

<style scoped lang="scss">
.request-status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 9px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--status-color) 20%, #ffffff);
  background: color-mix(in srgb, var(--status-color) 10%, #ffffff);
  color: var(--status-color);
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  white-space: nowrap;
}

.request-status-tag__dot {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: var(--status-color);
}
</style>
