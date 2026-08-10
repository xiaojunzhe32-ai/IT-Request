<template>
  <div class="admin-page">
    <PageHeader eyebrow="Admin Console" title="Permissions" description="Permission reference for request workflow, organization management and audit access." />
    <div v-loading="loading" class="permission-grid">
      <el-card v-for="group in groupedPermissions" :key="group.name" class="permission-card" shadow="never">
        <template #header><div class="card-title-row"><div><strong>{{ group.name }}</strong><span>{{ group.items.length }} permission codes</span></div></div></template>
        <div class="perm-list"><article v-for="permission in group.items" :key="permission" class="perm-item"><code>{{ permission }}</code><span>{{ describePermission(permission) }}</span></article></div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { roleApi } from '@/api/system'

const permissions = ref<string[]>([])
const loading = ref(false)
const groupName = (permission: string) => permission === '*' ? 'Global' : permission.split(':')[0].replace(/^./, (letter) => letter.toUpperCase())
const groupedPermissions = computed(() => Array.from(permissions.value.reduce((map, permission) => { const name = groupName(permission); map.set(name, [...(map.get(name) || []), permission]); return map }, new Map<string, string[]>())).map(([name, items]) => ({ name, items })))
const describePermission = (permission: string) => permission === '*' ? 'Full system access' : permission.endsWith(':*') ? `All ${groupName(permission).toLowerCase()} operations` : `${permission.split(':')[1]?.replace(/-/g, ' ')} access for ${groupName(permission).toLowerCase()}`
onMounted(async () => { loading.value = true; try { permissions.value = await roleApi.permissions() } catch { ElMessage.error('Unable to load permissions') } finally { loading.value = false } })
</script>

<style scoped lang="scss">
.admin-page { display: grid; gap: 16px; }
.permission-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.permission-card { border: 1px solid rgba(15, 23, 42, 0.08); }
.perm-list { display: grid; gap: 10px; }
.perm-item { display: grid; gap: 5px; padding: 10px 12px; border-radius: 8px; background: #f8fafc; border: 1px solid #e5e7eb; }
.perm-item code { color: #000080; font-weight: 800; font-size: 12px; }
.perm-item span { color: #667085; font-size: 12px; line-height: 1.5; }
@media (max-width: 980px) { .permission-grid { grid-template-columns: 1fr; } }
</style>
