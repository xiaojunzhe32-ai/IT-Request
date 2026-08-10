<template>
  <div class="admin-page">
    <PageHeader eyebrow="Admin Console" title="Teams" description="Configure IT teams, ownership and the people who can handle requests.">
      <template #actions><el-button type="primary" @click="openCreate">New Team</el-button></template>
    </PageHeader>
    <el-card v-loading="loading" class="surface-card" shadow="never">
      <el-table :data="teams" row-key="id" highlight-current-row>
        <el-table-column label="Team" min-width="220"><template #default="{ row }"><div class="main-cell"><strong>{{ row.name }}</strong><span>{{ row.teamCode || '-' }} · {{ row.organizationName || '-' }}</span></div></template></el-table-column>
        <el-table-column prop="leaderName" label="Lead" width="170" />
        <el-table-column label="Members" min-width="260"><template #default="{ row }"><el-tag v-for="member in row.memberNames || []" :key="member" size="small" class="member-tag">{{ member }}</el-tag><span v-if="!row.memberNames?.length">No members</span></template></el-table-column>
        <el-table-column label="Open Requests" width="140"><template #default="{ row }">{{ countByTeam(row.id) }}</template></el-table-column>
        <el-table-column label="Status" width="110"><template #default="{ row }"><el-tag :type="row.status?.toLowerCase() === 'active' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag></template></el-table-column>
        <el-table-column label="Actions" width="150" fixed="right"><template #default="{ row }"><el-button text @click="openEdit(row)">Edit</el-button><el-button text type="danger" @click="removeTeam(row)">Delete</el-button></template></el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? 'Edit Team' : 'New Team'" width="660px">
      <el-form label-position="top" class="dialog-grid">
        <el-form-item label="Name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="Team Code"><el-input v-model="form.teamCode" /></el-form-item>
        <el-form-item label="Organization"><el-select v-model="form.organizationId" filterable><el-option v-for="org in organizations" :key="org.id" :label="org.name" :value="org.id" /></el-select></el-form-item>
        <el-form-item label="Team Type"><el-select v-model="form.teamType"><el-option label="Support" value="SUPPORT" /><el-option label="Test" value="TEST" /><el-option label="Operations" value="OPERATIONS" /></el-select></el-form-item>
        <el-form-item label="Team Lead"><el-select v-model="form.leaderId" clearable filterable><el-option v-for="user in leadOptions" :key="user.id" :label="displayName(user)" :value="user.id" /></el-select></el-form-item>
        <el-form-item label="Status"><el-select v-model="form.status"><el-option label="Active" value="ACTIVE" /><el-option label="Inactive" value="INACTIVE" /></el-select></el-form-item>
        <el-form-item label="Members" class="span-2"><el-select v-model="form.memberIds" multiple filterable><el-option v-for="user in memberOptions" :key="user.id" :label="`${displayName(user)} - ${user.organizationName || 'No organization'}`" :value="user.id" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">Cancel</el-button><el-button type="primary" :loading="saving" @click="saveTeam">Save</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { organizationApi } from '@/api/cmdb'
import { requestApi } from '@/api/requests'
import { teamApi, userApi } from '@/api/system'
import type { Organization } from '@/types/cmdb'
import type { WorkflowRequest } from '@/types/requests'
import type { SystemUser, Team } from '@/types/system'

const teams = ref<Team[]>([])
const users = ref<SystemUser[]>([])
const organizations = ref<Organization[]>([])
const requests = ref<WorkflowRequest[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()
const form = reactive({ name: '', teamCode: '', organizationId: undefined as number | undefined, teamType: 'SUPPORT', leaderId: undefined as number | undefined, memberIds: [] as number[], status: 'ACTIVE' })
const memberOptions = computed(() => users.value.filter((user) => user.status === 'active' && user.roleCodes?.some((role) => ['ADMIN', 'TEAM_LEAD', 'TECHNICIAN', 'TESTER'].includes(role))))
const leadOptions = computed(() => memberOptions.value.filter((user) => user.roleCodes?.some((role) => ['ADMIN', 'TEAM_LEAD'].includes(role))))
const displayName = (user: SystemUser) => `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.username
const countByTeam = (teamId: number) => requests.value.filter((request) => request.teamId === teamId && request.status !== 'Closed').length

const load = async () => { loading.value = true; try { const [teamPage, userPage, orgPage, requestPage] = await Promise.all([teamApi.list({ page: 0, size: 100, sort: 'name' }), userApi.list({ page: 0, size: 100, status: 'active' }), organizationApi.getList({ page: 0, size: 100 }), requestApi.list({ page: 0, size: 100 })]); teams.value = teamPage.content; users.value = userPage.content; organizations.value = orgPage.content; requests.value = requestPage.content } catch { ElMessage.error('Unable to load teams') } finally { loading.value = false } }
const resetForm = () => Object.assign(form, { name: '', teamCode: '', organizationId: organizations.value[0]?.id, teamType: 'SUPPORT', leaderId: undefined, memberIds: [], status: 'ACTIVE' })
const openCreate = () => { editingId.value = undefined; resetForm(); dialogVisible.value = true }
const openEdit = (team: Team) => { editingId.value = team.id; Object.assign(form, { name: team.name, teamCode: team.teamCode || '', organizationId: team.organizationId, teamType: team.teamType || 'SUPPORT', leaderId: team.leaderId, memberIds: [...(team.memberIds || [])], status: team.status || 'ACTIVE' }); dialogVisible.value = true }
const saveTeam = async () => { if (!form.name || !form.teamCode || !form.organizationId) { ElMessage.warning('Name, code and organization are required'); return }; saving.value = true; try { if (editingId.value) await teamApi.update(editingId.value, form); else await teamApi.create(form); dialogVisible.value = false; await load(); ElMessage.success('Team saved') } finally { saving.value = false } }
const removeTeam = async (team: Team) => { try { await ElMessageBox.confirm(`Delete ${team.name}?`, 'Delete Team', { type: 'warning', confirmButtonText: 'Delete' }); await teamApi.delete(team.id); await load(); ElMessage.success('Team deleted') } catch { /* cancelled */ } }
onMounted(load)
</script>

<style scoped lang="scss">
.admin-page { display: grid; gap: 16px; }
.main-cell { display: grid; gap: 4px; }
.main-cell strong { color: #111827; font-size: 13px; }
.main-cell span { color: #667085; font-size: 12px; }
.member-tag { margin-right: 6px; }
.dialog-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 16px; }
.span-2 { grid-column: 1 / -1; }
@media (max-width: 760px) { .dialog-grid { grid-template-columns: 1fr; } .span-2 { grid-column: auto; } }
</style>
