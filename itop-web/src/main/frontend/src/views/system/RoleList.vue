<template>
  <div class="admin-page">
    <PageHeader eyebrow="Admin Console" title="Roles" description="Role bundles define what each actor can do in the request workflow.">
      <template #actions><el-button type="primary" @click="openCreate">New Role</el-button></template>
    </PageHeader>
    <div v-loading="loading" class="role-grid">
      <el-card v-for="role in roles" :key="role.id" class="role-card" shadow="never">
        <div class="role-head"><div><strong>{{ role.name }}</strong><span>{{ role.roleCode }}</span></div><el-tag :type="role.isSystem ? 'info' : 'warning'" size="small">{{ role.isSystem ? 'System' : 'Custom' }}</el-tag></div>
        <p>{{ role.description || 'No description' }}</p>
        <div class="permission-list"><el-tag v-for="permission in role.permissions || []" :key="permission" size="small" effect="light">{{ permission }}</el-tag></div>
        <div class="role-foot"><span>{{ userCount(role.roleCode) }} users</span><div><el-button text @click="openEdit(role)">Edit</el-button><el-button v-if="!role.isSystem" text type="danger" @click="removeRole(role)">Delete</el-button></div></div>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? 'Edit Role' : 'New Role'" width="680px">
      <el-form label-position="top">
        <div class="dialog-grid"><el-form-item label="Name"><el-input v-model="form.name" /></el-form-item><el-form-item label="Role Code"><el-input v-model="form.roleCode" :disabled="Boolean(editingId)" /></el-form-item></div>
        <el-form-item label="Description"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="Permissions"><el-select v-model="form.permissions" multiple filterable><el-option v-for="permission in permissionCatalog" :key="permission" :label="permission" :value="permission" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">Cancel</el-button><el-button type="primary" :loading="saving" @click="saveRole">Save</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { roleApi, userApi } from '@/api/system'
import type { SystemRole, SystemUser } from '@/types/system'

const roles = ref<SystemRole[]>([])
const users = ref<SystemUser[]>([])
const permissionCatalog = ref<string[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()
const form = reactive({ name: '', roleCode: '', description: '', permissions: [] as string[] })
const userCount = (code: string) => users.value.filter((user) => user.roleCodes?.includes(code)).length

const load = async () => {
  loading.value = true
  try {
    const [rolePage, userPage, permissions] = await Promise.all([roleApi.list({ page: 0, size: 100 }), userApi.list({ page: 0, size: 100 }), roleApi.permissions()])
    roles.value = rolePage.content; users.value = userPage.content; permissionCatalog.value = permissions
  } catch { ElMessage.error('Unable to load roles') }
  finally { loading.value = false }
}
const openCreate = () => { editingId.value = undefined; Object.assign(form, { name: '', roleCode: '', description: '', permissions: [] }); dialogVisible.value = true }
const openEdit = (role: SystemRole) => { editingId.value = role.id; Object.assign(form, { name: role.name, roleCode: role.roleCode, description: role.description || '', permissions: [...(role.permissions || [])] }); dialogVisible.value = true }
const saveRole = async () => { if (!form.name || !form.roleCode) { ElMessage.warning('Name and role code are required'); return }; saving.value = true; try { const payload = { ...form, status: 'active' }; if (editingId.value) await roleApi.update(editingId.value, payload); else await roleApi.create(payload); dialogVisible.value = false; await load(); ElMessage.success('Role saved') } finally { saving.value = false } }
const removeRole = async (role: SystemRole) => { try { await ElMessageBox.confirm(`Delete ${role.name}?`, 'Delete Role', { type: 'warning', confirmButtonText: 'Delete' }); await roleApi.delete(role.id); await load(); ElMessage.success('Role deleted') } catch { /* cancelled */ } }
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
.dialog-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
@media (max-width: 980px) { .role-grid, .dialog-grid { grid-template-columns: 1fr; } }
</style>
