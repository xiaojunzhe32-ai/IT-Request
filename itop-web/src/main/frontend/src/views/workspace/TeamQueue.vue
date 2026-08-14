<template>
  <div class="workspace-page">
    <PageHeader
      eyebrow="IT Workspace"
      title="Team Queue"
      description="Shared queue for technicians and technical leads. Requests can be transferred within IT scope."
    />

    <el-card v-loading="loading" class="surface-card" shadow="never">
      <div class="toolbar-row">
        <el-input v-model="keyword" placeholder="Search request number, title or requester" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="teamFilter"
          multiple
          collapse-tags
          collapse-tags-tooltip
          filterable
          clearable
          placeholder="Team"
        >
          <el-option v-for="team in teams" :key="team.id" :label="team.name" :value="team.id" />
        </el-select>
        <el-select
          v-model="statusFilter"
          multiple
          collapse-tags
          collapse-tags-tooltip
          filterable
          clearable
          placeholder="Status"
        >
          <el-option v-for="status in requestStatuses" :key="status" :label="status" :value="status" />
        </el-select>
        <el-select
          v-model="assigneeFilter"
          multiple
          collapse-tags
          collapse-tags-tooltip
          filterable
          clearable
          placeholder="Assignee"
        >
          <el-option label="Unassigned" :value="0" />
          <el-option v-for="user in users" :key="user.id" :label="displayName(user)" :value="user.id" />
        </el-select>
      </div>

      <RequestTable
        :requests="filteredRequests"
        detail-base="/workspace/requests"
        workspace-mode
        empty-text="No requests in this queue"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import RequestTable from '@/components/RequestTable.vue'
import { requestApi } from '@/api/requests'
import { teamApi, userApi } from '@/api/system'
import { requestStatuses } from '@/data/requestOptions'
import type { RequestStatus, WorkflowRequest } from '@/types/requests'
import type { SystemUser, Team } from '@/types/system'

const keyword = ref('')
const teamFilter = ref<number[]>([])
const statusFilter = ref<RequestStatus[]>([])
const assigneeFilter = ref<number[]>([])
const loading = ref(false)
const requests = ref<WorkflowRequest[]>([])
const teams = ref<Team[]>([])
const users = ref<SystemUser[]>([])

const displayName = (user: SystemUser) => {
  const name = [user.firstName, user.lastName].filter(Boolean).join(' ')
  return name || user.username
}

const filteredRequests = computed(() => {
  let result = requests.value.filter((request) => request.status !== 'Closed')

  if (teamFilter.value.length > 0) {
    result = result.filter((request) => request.teamId != null && teamFilter.value.includes(request.teamId))
  }

  if (statusFilter.value.length > 0) {
    result = result.filter((request) => statusFilter.value.includes(request.status))
  }

  if (assigneeFilter.value.length > 0) {
    result = result.filter((request) => {
      const assigneeId = request.agentId ?? 0
      return assigneeFilter.value.includes(assigneeId)
    })
  }

  return result
})

const loadRequests = async () => {
  loading.value = true
  try {
    const response = await requestApi.list({
      page: 0,
      size: 100,
      search: keyword.value || undefined
    })
    requests.value = response.content
  } catch {
    ElMessage.error('Unable to load team queue')
  } finally {
    loading.value = false
  }
}

const loadTeams = async () => {
  try {
    const response = await teamApi.list({ page: 0, size: 100, sort: 'name' })
    teams.value = response.content
  } catch {
    ElMessage.error('Unable to load teams')
  }
}

const loadUsers = async () => {
  try {
    const response = await userApi.list({ page: 0, size: 200, sort: 'username', status: 'active' })
    users.value = response.content
  } catch {
    ElMessage.error('Unable to load users')
  }
}

watch(keyword, loadRequests)
onMounted(() => {
  loadTeams()
  loadUsers()
  loadRequests()
})
</script>

<style scoped lang="scss">
.workspace-page {
  display: grid;
  gap: 16px;
}

.toolbar-row {
  display: grid;
  grid-template-columns: minmax(280px, 1fr) 220px 180px 200px;
  gap: 12px;
  margin-bottom: 14px;
}

@media (max-width: 960px) {
  .toolbar-row {
    grid-template-columns: 1fr;
  }
}
</style>
