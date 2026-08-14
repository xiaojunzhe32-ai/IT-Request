<template>
  <div class="admin-page">
    <PageHeader eyebrow="Admin Console" title="Requests" description="Global request control view for admins and technical leads.">
      <template #actions>
        <el-button type="primary" @click="router.push('/workspace/new-request')">
          <el-icon><Plus /></el-icon>
          New Request
        </el-button>
      </template>
    </PageHeader>
    <el-card v-loading="loading" class="surface-card" shadow="never">
      <div class="toolbar-row">
        <el-input v-model="keyword" placeholder="Search request number, title or requester" clearable>
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
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
      <RequestTable :requests="filteredRequests" detail-base="/workspace/requests" workspace-mode empty-text="No matching requests" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import RequestTable from '@/components/RequestTable.vue'
import { requestApi } from '@/api/requests'
import { teamApi, userApi } from '@/api/system'
import { requestStatuses } from '@/data/requestOptions'
import type { RequestStatus, WorkflowRequest } from '@/types/requests'
import type { SystemUser, Team } from '@/types/system'

const router = useRouter()
const keyword = ref('')
const statusFilter = ref<RequestStatus[]>([])
const teamFilter = ref<number[]>([])
const assigneeFilter = ref<number[]>([])
const loading = ref(false)
const filteredRequests = ref<WorkflowRequest[]>([])
const teams = ref<Team[]>([])
const users = ref<SystemUser[]>([])

const displayName = (user: SystemUser) => {
  const name = [user.firstName, user.lastName].filter(Boolean).join(' ')
  return name || user.username
}

const loadRequests = async () => {
  loading.value = true
  try {
    const response = await requestApi.list({
      page: 0,
      size: 100,
      search: keyword.value || undefined,
      status: statusFilter.value.length ? statusFilter.value : undefined,
      teamId: teamFilter.value.length ? teamFilter.value : undefined,
      assigneeId: assigneeFilter.value.length ? assigneeFilter.value : undefined
    })
    filteredRequests.value = response.content
  } catch {
    ElMessage.error('Unable to load requests')
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
    ElMessage.error('Unable to load assignees')
  }
}

watch([keyword, statusFilter, teamFilter, assigneeFilter], loadRequests)
onMounted(() => {
  loadTeams()
  loadUsers()
  loadRequests()
})
</script>

<style scoped lang="scss">
.admin-page { display: grid; gap: 16px; }
.toolbar-row { display: grid; grid-template-columns: minmax(280px, 1fr) 180px 220px 220px; gap: 12px; margin-bottom: 14px; }
@media (max-width: 960px) { .toolbar-row { grid-template-columns: 1fr; } }
</style>
