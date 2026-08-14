<template>
  <div class="admin-page">
    <PageHeader
      eyebrow="Admin Console"
      title="Code Tables"
      description="Manage request form dropdown values, roles, permissions and routing rules."
    >
      <template #actions>
        <el-button v-if="isCodeTableTab" type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>
          New Item
        </el-button>
      </template>
    </PageHeader>

    <el-tabs v-model="activeTab" class="section-tabs" @tab-change="onTabChange">
      <el-tab-pane label="Request Types" name="REQUEST_TYPE" />
      <el-tab-pane label="Affected Services" name="AFFECTED_SERVICE" />
      <el-tab-pane label="Roles" name="roles" />
      <el-tab-pane label="Permissions" name="permissions" />
      <el-tab-pane label="Routing Rules" name="routing-rules" />
    </el-tabs>

    <el-card v-if="activeTab === 'roles'" class="surface-card" shadow="never">
      <RoleList embedded />
    </el-card>
    <el-card v-else-if="activeTab === 'permissions'" class="surface-card" shadow="never">
      <PermissionList embedded />
    </el-card>
    <el-card v-else-if="activeTab === 'routing-rules'" class="surface-card" shadow="never">
      <RoutingRules embedded />
    </el-card>
    <el-card v-else v-loading="loading" class="surface-card" shadow="never">
      <div class="table-context">
        <div>
          <strong>{{ currentMeta.title }}</strong>
          <span>{{ currentMeta.description }}</span>
        </div>
        <el-tag effect="light">{{ items.length }} items</el-tag>
      </div>

      <el-table :data="pagedItems" row-key="id" highlight-current-row>
        <el-table-column label="Name" min-width="220">
          <template #default="{ row }">
            <div class="main-cell">
              <strong>{{ row.name }}</strong>
              <span v-if="row.description">{{ row.description }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="Code" min-width="180" show-overflow-tooltip />
        <el-table-column label="Status" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status?.toLowerCase() === 'active' ? 'success' : 'info'" size="small">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text @click="openEdit(row)">Edit</el-button>
            <el-button text type="danger" @click="removeItem(row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="items.length > itemPageSize"
        v-model:current-page="itemCurrentPage"
        :page-size="itemPageSize"
        :total="items.length"
        layout="total, prev, pager, next"
        class="table-pagination"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? 'Edit Code Table Item' : 'New Code Table Item'" width="620px">
        <el-form label-position="top" class="dialog-grid">
        <el-form-item label="Table">
          <el-input :model-value="currentMeta.label" disabled />
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="form.status">
            <el-option label="Active" value="active" />
            <el-option label="Inactive" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item label="Name">
          <el-input v-model="form.name" placeholder="Displayed label" />
        </el-form-item>
        <el-form-item label="Code">
          <el-input v-model="form.code" placeholder="Stable value, for example ERP" @blur="normalizeCode" />
        </el-form-item>
        <el-form-item label="Description" class="span-2">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="Optional admin note" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="saveItem">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import RoleList from './RoleList.vue'
import PermissionList from './PermissionList.vue'
import RoutingRules from './RoutingRules.vue'
import { codeTableApi } from '@/api/system'
import { useCodeTableStore } from '@/stores/codeTables'
import type { CodeTableCode, CodeTableItem } from '@/types/system'

const tableMetas: Array<{
  code: CodeTableCode
  label: string
  title: string
  description: string
}> = [
  {
    code: 'REQUEST_TYPE',
    label: 'Request Types',
    title: 'Request Types',
    description: 'Values shown in the Request Type dropdown on the New Request form.'
  },
  {
    code: 'AFFECTED_SERVICE',
    label: 'Affected Services / Systems',
    title: 'Affected Services / Systems',
    description: 'Values shown in the Affected Service / System dropdown on the New Request form.'
  }
]

const activeTab = ref<string>('REQUEST_TYPE')
const isCodeTableTab = computed(() => tableMetas.some((m) => m.code === activeTab.value))
const activeTable = computed(() => (isCodeTableTab.value ? (activeTab.value as CodeTableCode) : undefined))

const items = ref<CodeTableItem[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()
const codeTableStore = useCodeTableStore()

const itemPageSize = 15
const itemCurrentPage = ref(1)
const pagedItems = computed(() => {
  const start = (itemCurrentPage.value - 1) * itemPageSize
  return items.value.slice(start, start + itemPageSize)
})

const form = reactive({
  name: '',
  code: '',
  status: 'active',
  description: '',
  sortOrder: 0
})

const currentMeta = computed(() => tableMetas.find((meta) => meta.code === activeTab.value) || tableMetas[0])

const formatStatus = (status?: string) =>
  status ? status.charAt(0).toUpperCase() + status.slice(1).toLowerCase() : 'Unknown'

const normalizeCode = () => {
  form.code = form.code.trim().replace(/\s+/g, '_')
}

const resetForm = () => Object.assign(form, {
  name: '',
  code: '',
  status: 'active',
  description: '',
  sortOrder: items.value.length ? Math.max(...items.value.map((item) => item.sortOrder || 0)) + 10 : 10
})

const onTabChange = (name: string | number) => {
  if (tableMetas.some((m) => m.code === name)) {
    loadItems()
  }
}

const loadItems = async () => {
  if (!activeTable.value) return
  loading.value = true
  try {
    items.value = await codeTableApi.list(activeTable.value)
    itemCurrentPage.value = 1
  } catch {
    ElMessage.error('Unable to load code table items')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editingId.value = undefined
  resetForm()
  dialogVisible.value = true
}

const openEdit = (item: CodeTableItem) => {
  editingId.value = item.id
  Object.assign(form, {
    name: item.name,
    code: item.code,
    status: item.status || 'active',
    description: item.description || '',
    sortOrder: item.sortOrder || 0
  })
  dialogVisible.value = true
}

const saveItem = async () => {
  normalizeCode()
  if (!form.name.trim() || !form.code.trim()) {
    ElMessage.warning('Name and code are required')
    return
  }

  saving.value = true
  try {
    const payload = {
      name: form.name.trim(),
      code: form.code.trim(),
      status: form.status,
      description: form.description.trim(),
      sortOrder: form.sortOrder
    }
    if (editingId.value) {
      await codeTableApi.update(activeTable.value!, editingId.value, payload)
    } else {
      await codeTableApi.create(activeTable.value!, payload)
    }
    dialogVisible.value = false
    await loadItems()
    await codeTableStore.loadTable(activeTable.value!, true)
    ElMessage.success('Code table item saved')
  } finally {
    saving.value = false
  }
}

const removeItem = async (item: CodeTableItem) => {
  try {
    await ElMessageBox.confirm(`Delete ${item.name}?`, 'Delete Code Table Item', {
      type: 'warning',
      confirmButtonText: 'Delete'
    })
  } catch {
    return
  }

  try {
    await codeTableApi.delete(activeTable.value!, item.id)
    await loadItems()
    await codeTableStore.loadTable(activeTable.value!, true)
    ElMessage.success('Code table item deleted')
  } catch {
    ElMessage.error('Unable to delete code table item')
  }
}

watch(activeTab, (newTab) => {
  if (tableMetas.some((m) => m.code === newTab)) {
    loadItems()
  }
})
onMounted(loadItems)
</script>

<style scoped lang="scss">
.admin-page {
  display: grid;
  gap: 16px;
}

.section-tabs {
  margin-bottom: 4px;
}

.table-context {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #edf0f3;
}

.table-context strong,
.main-cell strong {
  color: #111827;
  font-size: 13px;
}

.table-context span,
.main-cell span {
  display: block;
  margin-top: 4px;
  color: #667085;
  font-size: 12px;
}

.main-cell {
  display: grid;
  gap: 4px;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.span-2 {
  grid-column: 1 / -1;
}

.table-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

@media (max-width: 720px) {
  .dialog-grid {
    grid-template-columns: 1fr;
  }

  .span-2 {
    grid-column: auto;
  }
}
</style>
