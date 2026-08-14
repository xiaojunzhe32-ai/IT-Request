<template>
  <div class="admin-page">
    <PageHeader
      eyebrow="Admin Console"
      title="Teams"
      description="Configure IT teams, ownership and the people who can handle requests."
    >
      <template #actions>
        <el-button type="primary" @click="openCreate">New Team</el-button>
      </template>
    </PageHeader>

    <el-card v-loading="loading" class="surface-card" shadow="never">
      <el-table :data="pagedTeams" row-key="id" highlight-current-row>
        <el-table-column label="Team" min-width="220">
          <template #default="{ row }">
            <div class="main-cell">
              <strong>{{ row.name }}</strong>
              <span>{{ row.teamCode || '-' }} · {{ row.teamType === 'IT_TEAM' ? 'IT Team' : 'User Team' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="Lead" width="200">
          <template #default="{ row }">
            <el-tag v-for="name in row.leaderNames || []" :key="name" size="small" type="warning" class="member-tag">
              {{ name }}
            </el-tag>
            <span v-if="!row.leaderNames?.length">-</span>
          </template>
        </el-table-column>
        <el-table-column label="Members" min-width="260">
          <template #default="{ row }">
            <el-tag v-for="member in row.memberNames || []" :key="member" size="small" class="member-tag">
              {{ member }}
            </el-tag>
            <span v-if="!row.memberNames?.length">No members</span>
          </template>
        </el-table-column>
        <el-table-column label="Open Requests" width="140">
          <template #default="{ row }">{{ countByTeam(row.id) }}</template>
        </el-table-column>
        <el-table-column label="Status" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status?.toLowerCase() === 'active' ? 'success' : 'info'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text @click="openEdit(row)">Edit</el-button>
            <el-button text type="danger" @click="removeTeam(row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="teams.length > teamPageSize"
        v-model:current-page="teamCurrentPage"
        :page-size="teamPageSize"
        :total="teams.length"
        layout="total, prev, pager, next"
        class="table-pagination"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? 'Edit Team' : 'New Team'" width="660px">
      <el-form label-position="top" class="dialog-grid">
        <el-form-item label="Name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="Team Code">
          <el-input v-model="form.teamCode" />
        </el-form-item>
        <el-form-item label="Team Type">
          <el-select v-model="form.teamType">
            <el-option label="IT Team" value="IT_TEAM" />
            <el-option label="User Team" value="USER_TEAM" />
          </el-select>
        </el-form-item>
        <el-form-item label="Team Lead">
          <el-select v-model="form.leaderIds" multiple filterable placeholder="Select from members">
            <el-option
              v-for="user in leadOptions"
              :key="user.id"
              :label="displayName(user)"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="form.status">
            <el-option label="Active" value="ACTIVE" />
            <el-option label="Inactive" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="Members" class="span-2">
          <el-select v-model="form.memberIds" multiple filterable>
            <el-option
              v-for="user in memberOptions"
              :key="user.id"
              :label="displayName(user)"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="saveTeam">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { requestApi } from '@/api/requests'
import { teamApi, userApi } from '@/api/system'
import type { WorkflowRequest } from '@/types/requests'
import type { SystemUser, Team } from '@/types/system'

const teams = ref<Team[]>([])
const users = ref<SystemUser[]>([])
const requests = ref<WorkflowRequest[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()
const form = reactive({
  name: '',
  teamCode: '',
  teamType: 'IT_TEAM',
  leaderIds: [] as number[],
  memberIds: [] as number[],
  status: 'ACTIVE'
})

const teamPageSize = 10
const teamCurrentPage = ref(1)
const pagedTeams = computed(() => {
  const start = (teamCurrentPage.value - 1) * teamPageSize
  return teams.value.slice(start, start + teamPageSize)
})

const memberOptions = computed(() =>
  users.value.filter((user) => user.status === 'active')
)
// Leader options: only users already selected as members
const leadOptions = computed(() =>
  memberOptions.value.filter((user) => form.memberIds.includes(user.id))
)
const displayName = (user: SystemUser) =>
  `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.username

// When members change, remove any leaders no longer in the member list
watch(() => form.memberIds, (newMembers) => {
  form.leaderIds = form.leaderIds.filter(id => newMembers.includes(id))
})

const countByTeam = (teamId: number) =>
  requests.value.filter((request) => request.teamId === teamId && request.status !== 'Closed').length

const load = async () => {
  loading.value = true
  try {
    const [teamPage, userPage, requestPage] = await Promise.all([
      teamApi.list({ page: 0, size: 100, sort: 'name' }),
      userApi.list({ page: 0, size: 100, status: 'active' }),
      requestApi.list({ page: 0, size: 100 })
    ])
    teams.value = teamPage.content
    users.value = userPage.content
    requests.value = requestPage.content
  } catch {
    ElMessage.error('Unable to load teams')
  } finally {
    loading.value = false
  }
}

const resetForm = () => Object.assign(form, {
  name: '',
  teamCode: '',
  teamType: 'IT_TEAM',
  leaderIds: [],
  memberIds: [],
  status: 'ACTIVE'
})

const openCreate = () => {
  editingId.value = undefined
  resetForm()
  dialogVisible.value = true
}

const openEdit = (team: Team) => {
  editingId.value = team.id
  Object.assign(form, {
    name: team.name,
    teamCode: team.teamCode || '',
    teamType: team.teamType || 'IT_TEAM',
    leaderIds: [...(team.leaderIds || [])],
    memberIds: [...(team.memberIds || [])],
    status: team.status || 'ACTIVE'
  })
  dialogVisible.value = true
}

const saveTeam = async () => {
  if (!form.name || !form.teamCode) {
    ElMessage.warning('Name and code are required')
    return
  }
  saving.value = true
  try {
    if (editingId.value) await teamApi.update(editingId.value, form)
    else await teamApi.create(form)
    dialogVisible.value = false
    await load()
    ElMessage.success('Team saved')
  } finally {
    saving.value = false
  }
}

const removeTeam = async (team: Team) => {
  try {
    await ElMessageBox.confirm(`Delete ${team.name}?`, 'Delete Team', {
      type: 'warning',
      confirmButtonText: 'Delete'
    })
    await teamApi.delete(team.id)
    await load()
    ElMessage.success('Team deleted')
  } catch {
    // Cancelled.
  }
}

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
.table-pagination { margin-top: 16px; justify-content: flex-end; }
@media (max-width: 760px) {
  .dialog-grid { grid-template-columns: 1fr; }
  .span-2 { grid-column: auto; }
}
</style>
