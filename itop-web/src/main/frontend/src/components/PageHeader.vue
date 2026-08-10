<template>
  <section class="page-header">
    <div class="page-header__main">
      <p v-if="eyebrow" class="page-header__eyebrow">{{ eyebrow }}</p>
      <h1 class="page-header__title">{{ title }}</h1>
      <p v-if="description" class="page-header__description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="page-header__actions">
      <slot name="actions" />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
  eyebrow?: string
}>()
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 16px;
  padding: 18px 20px;
  border: 1px solid rgba(28, 36, 48, 0.08);
  border-radius: 8px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfcfd 100%);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

.page-header__main {
  min-width: 0;
}

.page-header__eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
  color: #d97706;
}

.page-header__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.page-header__description {
  margin: 8px 0 0;
  max-width: 760px;
  font-size: 13px;
  line-height: 1.55;
  color: #667085;
}

.page-header__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 960px) {
  .page-header {
    flex-direction: column;
  }

  .page-header__actions {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 520px) {
  .page-header {
    gap: 14px;
    padding: 16px;
  }

  .page-header__title {
    font-size: 20px;
  }

  .page-header__actions :deep(.el-button) {
    width: 100%;
  }
}
</style>
