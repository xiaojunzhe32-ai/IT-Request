<template>
  <div class="admin-page">
    <PageHeader v-if="!embedded" eyebrow="Admin Console" title="Roles" description="System roles are predefined and auto-derived from team membership. Manual editing is disabled." />
    <div v-loading="loading" class="role-grid">
      <el-card v-for="role in roles" :key="role.id" class="role-card" shadow="never">
        <div class="role-head"><div><strong>{{ role.name }}</strong><span>{{ role.roleCode }}</span></div><el-tag :type="role.isSystem ? 'info' : 'warning'" size="small">{{ role.isSystem ? 'System' : 'Custom' }}</el-tag></div>
        <p>{{ role.description || 'No description' }}</p>
        <div class="permission-list"><el-tag v-for="permission in role.permissions || []" :key="permission" size="small" effect="light">{{ permission }}</el-tag></div>
        <div class="role-foot"><span>{{ userCount(role.roleCode) }} users</span></div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'

defineProps<{ embedded?: boolean }>()
import { roleApi, userApi } from '@/api/system'
import type { SystemRole, SystemUser } from '@/types/system'

const roles = ref<SystemRole[]>([])
const users = ref<SystemUser[]>([])
const loading = ref(false)
const userCount = (code: string) => users.value.filter((user) => user.roleCodes?.includes(code)).length

const load = async () => {
  loading.value = true
  try {
    const [rolePage, userPage] = await Promise.all([roleApi.list({ page: 0, size: 100 }), userApi.list({ page: 0, size: 100 })])
    roles.value = rolePage.content; users.value = userPage.content
  } catch { ElMessage.error('Unable to load roles') }
  finally { loading.value = false }
}
onMounted(load)
</script>

<style scoped lang="scss">
.admin-page { display: grid; gap: 16px; }
.role-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.role-card { border: 1px solid rgba(15, 23, 42, 0.08); }
.role-head, .role-foot { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.role-head strong, .role-head span { display: block; }
.role-head strong { color: #111827; font-size: 15px; }
.role-head span, .role-card p, .role-foot span { color: #667085; font-size: 12px; }
.role-card p { min-height: 42px; line-height: 1.55; margin: 14px 0; }
.permission-list { display: flex; flex-wrap: wrap; gap: 6px; min-height: 32px; }
.role-foot { margin-top: 16px; padding-top: 12px; border-top: 1px solid #edf0f3; }
@media (max-width: 980px) { .role-grid { grid-template-columns: 1fr; } }
</style>
