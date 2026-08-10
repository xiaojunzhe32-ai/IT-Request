<template>
  <span class="priority-tag" :style="{ '--priority-color': color }">
    {{ priority }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { priorityColorMap } from '@/data/requestOptions'
import type { RequestPriority } from '@/types/requests'

const props = defineProps<{
  priority: RequestPriority
}>()

const color = computed(() => priorityColorMap[props.priority])
</script>

<style scoped lang="scss">
.priority-tag {
  display: inline-flex;
  align-items: center;
  min-width: 64px;
  justify-content: center;
  padding: 5px 9px;
  border-radius: 999px;
  color: var(--priority-color);
  border: 1px solid color-mix(in srgb, var(--priority-color) 18%, #fff);
  background: color-mix(in srgb, var(--priority-color) 9%, #fff);
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
}
</style>
