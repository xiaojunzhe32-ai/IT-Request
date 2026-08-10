<template>
  <div class="admin-page">
    <PageHeader eyebrow="Admin Console" title="Organizations" description="Manage requester visibility boundaries and reporting ownership.">
      <template #actions><el-button type="primary" @click="openCreate">New Organization</el-button></template>
    </PageHeader>

    <el-card v-loading="loading" class="surface-card" shadow="never">
      <el-table :data="organizations" row-key="id" highlight-current-row>
        <el-table-column label="Organization" min-width="220">
          <template #default="{ row }">
            <div class="main-cell"><strong>{{ row.name }}</strong><span>{{ row.code || '-' }} · {{ row.type || 'Organization' }}</span></div>
          </template>
        </el-table-column>
        <el-table-column label="Parent" min-width="180">
          <template #default="{ row }">{{ organizationName(row.parentId) }}</template>
        </el-table-column>
        <el-table-column prop="email" label="Email" min-width="210" />
        <el-table-column prop="phone" label="Phone" width="160" />
        <el-table-column label="Status" width="110">
          <template #default="{ row }"><el-tag :type="row.status?.toLowerCase() === 'active' ? 'success' : 'info'" size="small">{{ formatStatus(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="Actions" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text @click="openEdit(row)">Edit</el-button>
            <el-button text type="danger" @click="removeOrganization(row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? 'Edit Organization' : 'New Organization'" width="620px">
      <el-form label-position="top" class="dialog-grid">
        <el-form-item label="Name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="Code"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="Type"><el-input v-model="form.type" placeholder="COMPANY, DEPARTMENT, IT_DEPT" /></el-form-item>
        <el-form-item label="Parent Organization">
          <el-select v-model="form.parentId" clearable filterable>
            <el-option v-for="org in parentOptions" :key="org.id" :label="org.name" :value="org.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Email"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="Phone"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="Status"><el-select v-model="form.status"><el-option label="Active" value="ACTIVE" /><el-option label="Inactive" value="INACTIVE" /></el-select></el-form-item>
        <el-form-item label="Description" class="span-2"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="saveOrganization">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { organizationApi } from '@/api/cmdb'
import type { Organization } from '@/types/cmdb'

const organizations = ref<Organization[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()
const form = reactive({ name: '', code: '', type: 'DEPARTMENT', parentId: undefined as number | undefined, email: '', phone: '', status: 'ACTIVE', description: '' })
const parentOptions = computed(() => organizations.value.filter((org) => org.id !== editingId.value))

const resetForm = () => Object.assign(form, { name: '', code: '', type: 'DEPARTMENT', parentId: undefined, email: '', phone: '', status: 'ACTIVE', description: '' })
const formatStatus = (status?: string) => status ? status.charAt(0).toUpperCase() + status.slice(1).toLowerCase() : 'Unknown'
const organizationName = (id?: number) => id ? organizations.value.find((org) => org.id === id)?.name || `Organization ${id}` : 'Root'

const loadOrganizations = async () => {
  loading.value = true
  try {
    organizations.value = (await organizationApi.getList({ page: 0, size: 100, sort: 'name' })).content
  } catch { ElMessage.error('Unable to load organizations') }
  finally { loading.value = false }
}

const openCreate = () => { editingId.value = undefined; resetForm(); dialogVisible.value = true }
const openEdit = (org: Organization) => {
  editingId.value = org.id
  Object.assign(form, { name: org.name, code: org.code || '', type: org.type || 'DEPARTMENT', parentId: org.parentId, email: org.email || '', phone: org.phone || '', status: org.status || 'ACTIVE', description: org.description || '' })
  dialogVisible.value = true
}

const saveOrganization = async () => {
  if (!form.name.trim() || !form.code.trim()) { ElMessage.warning('Name and code are required'); return }
  saving.value = true
  try {
    const payload = { ...form, name: form.name.trim(), code: form.code.trim() }
    if (editingId.value) await organizationApi.update(editingId.value, payload)
    else await organizationApi.create(payload)
    dialogVisible.value = false
    await loadOrganizations()
    ElMessage.success('Organization saved')
  } finally { saving.value = false }
}

const removeOrganization = async (org: Organization) => {
  try {
    await ElMessageBox.confirm(`Delete ${org.name}?`, 'Delete Organization', {
      type: 'warning',
      confirmButtonText: 'Delete'
    })
  } catch {
    return
  }

  try {
    await organizationApi.delete(org.id)
    await loadOrganizations()
    ElMessage.success('Organization deleted')
  } catch {
    ElMessage.error('Unable to delete organization')
  }
}

onMounted(loadOrganizations)
</script>

<style scoped lang="scss">
.admin-page { display: grid; gap: 16px; }
.main-cell { display: grid; gap: 4px; }
.main-cell strong { color: #111827; font-size: 13px; }
.main-cell span { color: #667085; font-size: 12px; }
.dialog-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 16px; }
.span-2 { grid-column: 1 / -1; }
@media (max-width: 720px) { .dialog-grid { grid-template-columns: 1fr; } .span-2 { grid-column: auto; } }
</style>
