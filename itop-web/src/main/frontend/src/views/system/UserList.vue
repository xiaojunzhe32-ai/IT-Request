<template>
  <div class="admin-page">
    <PageHeader eyebrow="Admin Console" title="Users" description="Manage accounts, role assignments and active access state.">
      <template #actions><el-button type="primary" @click="openCreate">New User</el-button></template>
    </PageHeader>

    <el-card v-loading="loading" class="surface-card" shadow="never">
      <div class="toolbar-row">
        <el-input v-model="keyword" placeholder="Search username, name or email" clearable />
        <el-select v-model="roleFilter" placeholder="Role" clearable><el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.roleCode" /></el-select>
        <el-select v-model="statusFilter" placeholder="Status" clearable><el-option label="Active" value="active" /><el-option label="Inactive" value="inactive" /></el-select>
      </div>
      <el-table :data="filteredUsers" row-key="id" highlight-current-row>
        <el-table-column label="User" min-width="240">
          <template #default="{ row }"><div class="identity-cell"><span class="avatar">{{ displayName(row).slice(0, 1) }}</span><div><strong>{{ displayName(row) }}</strong><small>{{ row.username }} · {{ row.email }}</small></div></div></template>
        </el-table-column>
        <el-table-column prop="organizationName" label="Organization" min-width="180" />
        <el-table-column label="Roles" min-width="220"><template #default="{ row }"><el-tag v-for="role in row.roleCodes || []" :key="role" size="small" effect="light" class="role-tag">{{ role }}</el-tag></template></el-table-column>
        <el-table-column prop="status" label="Status" width="110"><template #default="{ row }"><el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag></template></el-table-column>
        <el-table-column prop="lastLogin" label="Last Login" width="170" />
        <el-table-column label="Actions" width="180" fixed="right"><template #default="{ row }"><el-button text @click="openEdit(row)">Edit</el-button><el-button text @click="toggleStatus(row)">{{ row.status === 'active' ? 'Disable' : 'Enable' }}</el-button></template></el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? 'Edit User' : 'New User'" width="640px">
      <el-form label-position="top" class="dialog-grid">
        <el-form-item label="Username"><el-input v-model="form.username" :disabled="Boolean(editingId)" /></el-form-item>
        <el-form-item label="Email"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="First Name"><el-input v-model="form.firstName" /></el-form-item>
        <el-form-item label="Last Name"><el-input v-model="form.lastName" /></el-form-item>
        <el-form-item label="Organization"><el-select v-model="form.organizationId" filterable><el-option v-for="org in organizations" :key="org.id" :label="org.name" :value="org.id" /></el-select></el-form-item>
        <el-form-item label="Status"><el-select v-model="form.status"><el-option label="Active" value="active" /><el-option label="Inactive" value="inactive" /></el-select></el-form-item>
        <el-form-item label="Roles" class="span-2"><el-select v-model="form.roleIds" multiple filterable><el-option v-for="role in roles" :key="role.id" :label="`${role.name} (${role.roleCode})`" :value="role.id" /></el-select></el-form-item>
        <el-form-item v-if="!editingId" label="Initial Password" class="span-2"><el-input v-model="form.password" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">Cancel</el-button><el-button type="primary" :loading="saving" @click="saveUser">Save</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { organizationApi } from '@/api/cmdb'
import { roleApi, userApi } from '@/api/system'
import type { Organization } from '@/types/cmdb'
import type { SystemRole, SystemUser } from '@/types/system'

const users = ref<SystemUser[]>([])
const roles = ref<SystemRole[]>([])
const organizations = ref<Organization[]>([])
const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const dialogVisible = ref(false)
const editingId = ref<number>()
const form = reactive({ username: '', email: '', firstName: '', lastName: '', organizationId: undefined as number | undefined, roleIds: [] as number[], status: 'active', password: '' })

const displayName = (user: SystemUser) => `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.username
const filteredUsers = computed(() => users.value.filter((user) => {
  const haystack = `${user.username} ${displayName(user)} ${user.email}`.toLowerCase()
  return (!keyword.value || haystack.includes(keyword.value.toLowerCase())) && (!roleFilter.value || user.roleCodes?.includes(roleFilter.value)) && (!statusFilter.value || user.status === statusFilter.value)
}))

const load = async () => {
  loading.value = true
  try {
    const [userPage, rolePage, orgPage] = await Promise.all([
      userApi.list({ page: 0, size: 100, sort: 'username' }),
      roleApi.list({ page: 0, size: 100, sort: 'name' }),
      organizationApi.getList({ page: 0, size: 100, sort: 'name' })
    ])
    users.value = userPage.content
    roles.value = rolePage.content
    organizations.value = orgPage.content
  } catch { ElMessage.error('Unable to load user management data') }
  finally { loading.value = false }
}

const resetForm = () => Object.assign(form, { username: '', email: '', firstName: '', lastName: '', organizationId: organizations.value[0]?.id, roleIds: [], status: 'active', password: '' })
const openCreate = () => { editingId.value = undefined; resetForm(); dialogVisible.value = true }
const openEdit = (user: SystemUser) => { editingId.value = user.id; Object.assign(form, { username: user.username, email: user.email, firstName: user.firstName || '', lastName: user.lastName || '', organizationId: user.organizationId, roleIds: user.roleIds || [], status: user.status || 'active', password: '' }); dialogVisible.value = true }

const saveUser = async () => {
  if (!form.username || !form.email || !form.organizationId || !form.roleIds.length || (!editingId.value && form.password.length < 6)) { ElMessage.warning('Username, email, organization, role and a valid password are required'); return }
  saving.value = true
  try {
    const payload = { ...form, language: 'en', authMethod: 'LOCAL' }
    if (editingId.value) await userApi.update(editingId.value, payload)
    else await userApi.create(payload)
    dialogVisible.value = false
    await load()
    ElMessage.success('User saved')
  } finally { saving.value = false }
}

const toggleStatus = async (user: SystemUser) => {
  await userApi.setStatus(user.id, user.status === 'active' ? 'inactive' : 'active')
  await load()
  ElMessage.success('User status updated')
}

onMounted(load)
</script>

<style scoped lang="scss">
.admin-page { display: grid; gap: 16px; }
.toolbar-row { display: grid; grid-template-columns: minmax(280px, 1fr) 180px 160px; gap: 12px; margin-bottom: 14px; }
.identity-cell { display: flex; align-items: center; gap: 10px; }
.avatar { display: grid; place-items: center; width: 34px; height: 34px; border-radius: 999px; background: #1c2430; color: #fff; font-weight: 800; }
.identity-cell strong, .identity-cell small { display: block; }
.identity-cell small { margin-top: 3px; color: #667085; font-size: 12px; }
.role-tag { margin-right: 6px; }
.dialog-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 16px; }
.span-2 { grid-column: 1 / -1; }
@media (max-width: 900px) { .toolbar-row, .dialog-grid { grid-template-columns: 1fr; } .span-2 { grid-column: auto; } }
</style>
