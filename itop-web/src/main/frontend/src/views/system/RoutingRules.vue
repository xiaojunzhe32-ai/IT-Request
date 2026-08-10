<template>
  <div class="admin-page">
    <PageHeader eyebrow="Admin Console" title="Routing Rules" description="Configure automatic assignment from request type and organization to owning IT team.">
      <template #actions><el-button type="primary" @click="openCreate">New Rule</el-button></template>
    </PageHeader>
    <el-card v-loading="loading" class="surface-card" shadow="never">
      <el-table :data="rules" row-key="id" highlight-current-row>
        <el-table-column label="Rule" min-width="240"><template #default="{ row }"><div class="main-cell"><strong>{{ row.name }}</strong><span>{{ row.requestType || 'Any type' }} · {{ row.organizationName || 'Any organization' }}</span></div></template></el-table-column>
        <el-table-column prop="sortOrder" label="Order" width="90" />
        <el-table-column prop="priority" label="Priority" width="120"><template #default="{ row }">{{ row.priority || 'Any' }}</template></el-table-column>
        <el-table-column prop="teamName" label="Target Team" min-width="190" />
        <el-table-column label="Enabled" width="110"><template #default="{ row }"><el-switch :model-value="row.enabled" @change="(value) => toggleRule(row, Boolean(value))" /></template></el-table-column>
        <el-table-column label="Actions" width="150" fixed="right"><template #default="{ row }"><el-button text @click="openEdit(row)">Edit</el-button><el-button text type="danger" @click="removeRule(row)">Delete</el-button></template></el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? 'Edit Routing Rule' : 'New Routing Rule'" width="660px">
      <el-form label-position="top" class="dialog-grid">
        <el-form-item label="Name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="Order"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="Request Type"><el-select v-model="form.requestType" clearable><el-option v-for="type in requestTypes" :key="type" :label="type" :value="type" /></el-select></el-form-item>
        <el-form-item label="Priority"><el-select v-model="form.priority" clearable><el-option v-for="priority in priorityOptions" :key="priority" :label="priority" :value="priority" /></el-select></el-form-item>
        <el-form-item label="Organization"><el-select v-model="form.organizationId" clearable filterable><el-option v-for="org in organizations" :key="org.id" :label="org.name" :value="org.id" /></el-select></el-form-item>
        <el-form-item label="Target Team"><el-select v-model="form.teamId" filterable><el-option v-for="team in teams" :key="team.id" :label="team.name" :value="team.id" /></el-select></el-form-item>
        <el-form-item label="Fallback Rule"><el-switch v-model="form.isFallback" /></el-form-item>
        <el-form-item label="Enabled"><el-switch v-model="form.enabled" /></el-form-item>
        <el-form-item label="Description" class="span-2"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">Cancel</el-button><el-button type="primary" :loading="saving" @click="saveRule">Save</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { organizationApi } from '@/api/cmdb'
import { routingRuleApi, teamApi } from '@/api/system'
import { priorityOptions, requestTypes } from '@/data/requestOptions'
import type { Organization } from '@/types/cmdb'
import type { RoutingRule, Team } from '@/types/system'

const rules = ref<RoutingRule[]>([])
const teams = ref<Team[]>([])
const organizations = ref<Organization[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()
const form = reactive({ name: '', description: '', organizationId: undefined as number | undefined, requestType: '', priority: '', teamId: undefined as number | undefined, enabled: true, sortOrder: 0, isFallback: false })
const load = async () => { loading.value = true; try { const [ruleList, teamPage, orgPage] = await Promise.all([routingRuleApi.list(), teamApi.list({ page: 0, size: 100 }), organizationApi.getList({ page: 0, size: 100 })]); rules.value = ruleList; teams.value = teamPage.content; organizations.value = orgPage.content } catch { ElMessage.error('Unable to load routing rules') } finally { loading.value = false } }
const resetForm = () => Object.assign(form, { name: '', description: '', organizationId: undefined, requestType: '', priority: '', teamId: undefined, enabled: true, sortOrder: rules.value.length, isFallback: false })
const openCreate = () => { editingId.value = undefined; resetForm(); dialogVisible.value = true }
const openEdit = (rule: RoutingRule) => { editingId.value = rule.id; Object.assign(form, { name: rule.name, description: rule.description || '', organizationId: rule.organizationId, requestType: rule.requestType || '', priority: rule.priority || '', teamId: rule.teamId, enabled: rule.enabled ?? true, sortOrder: rule.sortOrder || 0, isFallback: rule.isFallback ?? false }); dialogVisible.value = true }
const saveRule = async () => { if (!form.name || !form.teamId) { ElMessage.warning('Name and target team are required'); return }; saving.value = true; try { const payload = { ...form, requestType: form.requestType || undefined, priority: form.priority || undefined }; if (editingId.value) await routingRuleApi.update(editingId.value, payload); else await routingRuleApi.create(payload); dialogVisible.value = false; await load(); ElMessage.success('Routing rule saved') } finally { saving.value = false } }
const toggleRule = async (rule: RoutingRule, enabled: boolean) => { await routingRuleApi.setEnabled(rule.id, enabled); rule.enabled = enabled; ElMessage.success('Routing rule updated') }
const removeRule = async (rule: RoutingRule) => { try { await ElMessageBox.confirm(`Delete ${rule.name}?`, 'Delete Routing Rule', { type: 'warning', confirmButtonText: 'Delete' }); await routingRuleApi.delete(rule.id); await load(); ElMessage.success('Routing rule deleted') } catch { /* cancelled */ } }
onMounted(load)
</script>

<style scoped lang="scss">
.admin-page { display: grid; gap: 16px; }
.main-cell { display: grid; gap: 4px; }
.main-cell strong { color: #111827; font-size: 13px; }
.main-cell span { color: #667085; font-size: 12px; }
.dialog-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 16px; }
.span-2 { grid-column: 1 / -1; }
@media (max-width: 760px) { .dialog-grid { grid-template-columns: 1fr; } .span-2 { grid-column: auto; } }
</style>
