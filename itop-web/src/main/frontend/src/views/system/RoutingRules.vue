<template>
  <div class="admin-page">
    <PageHeader
      v-if="!embedded"
      eyebrow="Admin Console"
      title="Routing Rules"
      description="Keep routing rules as optional overrides. Requests go to ITMD by default."
    >
      <template #actions>
        <el-button type="primary" @click="openCreate">New Rule</el-button>
      </template>
    </PageHeader>
    <div v-if="embedded" class="embedded-actions"><el-button type="primary" @click="openCreate">New Rule</el-button></div>

    <el-alert
      class="routing-note"
      type="info"
      show-icon
      :closable="false"
      title="Default routing points to ITMD. Enable a rule only when you need to override the default route."
    />

    <el-card v-loading="loading" class="surface-card" shadow="never">
      <el-table :data="pagedRules" row-key="id" highlight-current-row>
        <el-table-column label="Rule" min-width="240">
          <template #default="{ row }">
            <div class="main-cell">
              <strong>{{ row.name }}</strong>
              <span>{{ affectedServiceLabel(row.affectedService) }} · {{ requestTypeLabel(row.requestType) }} · {{ row.priority || 'Any priority' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="Order" width="90" />
        <el-table-column prop="priority" label="Priority" width="120">
          <template #default="{ row }">{{ row.priority || 'Any' }}</template>
        </el-table-column>
        <el-table-column prop="teamName" label="Target Team" min-width="190" />
        <el-table-column label="Enabled" width="110">
          <template #default="{ row }">
            <el-switch :model-value="row.enabled" @change="(value) => toggleRule(row, Boolean(value))" />
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text @click="openEdit(row)">Edit</el-button>
            <el-button text type="danger" @click="removeRule(row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="rules.length > rulePageSize"
        v-model:current-page="ruleCurrentPage"
        :page-size="rulePageSize"
        :total="rules.length"
        layout="total, prev, pager, next"
        class="table-pagination"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? 'Edit Routing Rule' : 'New Routing Rule'" width="660px">
        <el-form label-position="top" class="dialog-grid">
        <el-form-item label="Name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="Order"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="Affected Service">
          <el-select v-model="form.affectedService" :loading="codeTableStore.loading.AFFECTED_SERVICE" clearable>
            <el-option
              v-for="service in affectedServiceOptions"
              :key="service.code"
              :label="service.name"
              :value="service.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Request Type">
          <el-select v-model="form.requestType" :loading="codeTableStore.loading.REQUEST_TYPE" clearable>
            <el-option
              v-for="type in requestTypeOptions"
              :key="type.code"
              :label="type.name"
              :value="type.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Priority">
          <el-select v-model="form.priority" clearable>
            <el-option v-for="priority in priorityOptions" :key="priority" :label="priority" :value="priority" />
          </el-select>
        </el-form-item>
        <el-form-item label="Target Team">
          <el-select v-model="form.teamId" filterable>
            <el-option v-for="team in teams" :key="team.id" :label="team.name" :value="team.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Fallback Rule"><el-switch v-model="form.isFallback" /></el-form-item>
        <el-form-item label="Enabled"><el-switch v-model="form.enabled" /></el-form-item>
        <el-form-item label="Description" class="span-2">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="saveRule">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'

defineProps<{ embedded?: boolean }>()
import { routingRuleApi, teamApi } from '@/api/system'
import { priorityOptions } from '@/data/requestOptions'
import { useCodeTableStore } from '@/stores/codeTables'
import type { RoutingRule, Team } from '@/types/system'

const rules = ref<RoutingRule[]>([])
const teams = ref<Team[]>([])
const codeTableStore = useCodeTableStore()
const loading = ref(false)

const rulePageSize = 15
const ruleCurrentPage = ref(1)
const pagedRules = computed(() => {
  const start = (ruleCurrentPage.value - 1) * rulePageSize
  return rules.value.slice(start, start + rulePageSize)
})
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()
const form = reactive({
  name: '',
  description: '',
  affectedService: '',
  requestType: '',
  priority: '',
  teamId: undefined as number | undefined,
  enabled: false,
  sortOrder: 0,
  isFallback: false
})
const requestTypeOptions = computed(() => codeTableStore.itemsFor('REQUEST_TYPE'))
const affectedServiceOptions = computed(() => codeTableStore.itemsFor('AFFECTED_SERVICE'))
const requestTypeLabel = (value?: string) =>
  value ? codeTableStore.labelFor('REQUEST_TYPE', value, value) : 'Any type'
const affectedServiceLabel = (value?: string) =>
  value ? codeTableStore.labelFor('AFFECTED_SERVICE', value, value) : 'Any service'

const load = async () => {
  loading.value = true
  try {
    const [ruleList, teamPage] = await Promise.all([
      routingRuleApi.list(),
      teamApi.list({ page: 0, size: 100 }),
      codeTableStore.loadTable('REQUEST_TYPE'),
      codeTableStore.loadTable('AFFECTED_SERVICE')
    ])
    rules.value = ruleList
    teams.value = teamPage.content
    ruleCurrentPage.value = 1
  } catch {
    ElMessage.error('Unable to load routing rules')
  } finally {
    loading.value = false
  }
}

const resetForm = () =>
  Object.assign(form, {
    name: '',
    description: '',
    affectedService: '',
    requestType: '',
    priority: '',
    teamId: undefined,
    enabled: false,
    sortOrder: rules.value.length,
    isFallback: false
  })

const openCreate = () => {
  editingId.value = undefined
  resetForm()
  dialogVisible.value = true
}

const openEdit = (rule: RoutingRule) => {
  editingId.value = rule.id
  Object.assign(form, {
    name: rule.name,
    description: rule.description || '',
    affectedService: rule.affectedService || '',
    requestType: rule.requestType || '',
    priority: rule.priority || '',
    teamId: rule.teamId,
    enabled: rule.enabled ?? true,
    sortOrder: rule.sortOrder || 0,
    isFallback: rule.isFallback ?? false
  })
  dialogVisible.value = true
}

const saveRule = async () => {
  if (!form.name || !form.teamId) {
    ElMessage.warning('Name and target team are required')
    return
  }

  saving.value = true
  try {
    const payload = {
      ...form,
      affectedService: form.affectedService || undefined,
      requestType: form.requestType || undefined,
      priority: form.priority || undefined
    }
    if (editingId.value) await routingRuleApi.update(editingId.value, payload)
    else await routingRuleApi.create(payload)
    dialogVisible.value = false
    await load()
    ElMessage.success('Routing rule saved')
  } finally {
    saving.value = false
  }
}

const toggleRule = async (rule: RoutingRule, enabled: boolean) => {
  const updated = await routingRuleApi.setEnabled(rule.id, enabled)
  rule.enabled = updated.enabled
  ElMessage.success('Routing rule updated')
}

const removeRule = async (rule: RoutingRule) => {
  try {
    await ElMessageBox.confirm(`Delete ${rule.name}?`, 'Delete Routing Rule', {
      type: 'warning',
      confirmButtonText: 'Delete'
    })
    await routingRuleApi.delete(rule.id)
    await load()
    ElMessage.success('Routing rule deleted')
  } catch {
    // Cancelled.
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.admin-page {
  display: grid;
  gap: 16px;
}
.embedded-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.routing-note {
  border-radius: 12px;
}

.main-cell {
  display: grid;
  gap: 4px;
}

.main-cell strong {
  color: #111827;
  font-size: 13px;
}

.main-cell span {
  color: #667085;
  font-size: 12px;
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

@media (max-width: 760px) {
  .dialog-grid {
    grid-template-columns: 1fr;
  }

  .span-2 {
    grid-column: auto;
  }
}
</style>
