<template>
  <div class="brand-logo" :class="{ compact }">
    <div class="brand-logo__plate">
      <img src="@/assets/nav-logo.png" alt="IT Request" />
    </div>
    <span v-if="subtitle && !compact">{{ subtitle }}</span>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  subtitle?: string
  compact?: boolean
}>()
</script>

<style scoped lang="scss">
.brand-logo {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.brand-logo__plate {
  width: 190px;
  height: 44px;
  display: flex;
  align-items: center;
  padding: 7px 12px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid rgba(255, 255, 255, 0.18);
  box-shadow: 0 10px 24px rgba(0, 0, 128, 0.16);
}

.brand-logo__plate img {
  display: block;
  width: 100%;
  height: auto;
}

.brand-logo span {
  color: rgba(255, 255, 255, 0.62);
  font-size: 12px;
  line-height: 1;
}

.brand-logo.compact {
  place-items: center;
}

.brand-logo.compact .brand-logo__plate {
  width: 42px;
  height: 42px;
  padding: 7px;
}
</style>
