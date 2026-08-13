<template>
  <div class="workspace-page">
    <PageHeader
      eyebrow="IT Workspace"
      title="Workflow Overview"
      description="Monitor request intake, assignment, handling and internal testing from one operational view."
    >
      <template #actions>
        <el-button v-if="canAssignRequests" @click="router.push('/workspace/assignment')">Open Assignment</el-button>
        <el-button type="primary" @click="router.push('/workspace/team-queue')">
          <el-icon><Tickets /></el-icon>
          Team Queue
        </el-button>
      </template>
    </PageHeader>

    <div class="metric-grid">
      <div v-for="metric in metrics" :key="metric.label" class="metric-card">
        <span>{{ metric.label }}</span>
        <strong>{{ metric.value }}</strong>
        <small>{{ metric.note }}</small>
      </div>
    </div>

    <div class="overview-grid">
      <el-card v-loading="loading" class="surface-card" shadow="never">
        <template #header>
          <div class="card-title-row">
            <div>
              <strong>Active Team Queue</strong>
              <span>Open requests across IT teams</span>
            </div>
          </div>
        </template>
        <RequestTable
          :requests="activeRequests"
          detail-base="/workspace/requests"
          workspace-mode
          empty-text="No active requests"
        />
      </el-card>

      <el-card v-loading="loading" class="surface-card" shadow="never">
        <template #header>
          <div class="card-title-row">
            <div>
              <strong>Status Mix</strong>
              <span>Current workflow distribution</span>
            </div>
          </div>
        </template>
        <div class="status-stack">
          <div v-for="item in statusMix" :key="item.status" class="status-row">
            <RequestStatusTag :status="item.status" />
            <div class="status-bar">
              <span :style="{ width: item.width }"></span>
            </div>
            <strong>{{ item.count }}</strong>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Tickets } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import RequestStatusTag from '@/components/RequestStatusTag.vue'
import RequestTable from '@/components/RequestTable.vue'
import { requestApi } from '@/api/requests'
import { requestStatuses } from '@/data/requestOptions'
import { useUserStore } from '@/stores/user'
import type { WorkflowRequest } from '@/types/requests'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const requests = ref<WorkflowRequest[]>([])
const canAssignRequests = computed(() => userStore.hasPermission('request:assign'))
const activeRequests = computed(() => requests.value.filter((item) => item.status !== 'Closed'))
const testingCount = computed(() => requests.value.filter((item) => item.status === 'Testing').length)
const failedCount = computed(() => requests.value.filter((item) => item.status === 'User Test Failed').length)

const metrics = computed(() => [
  { label: 'Open Requests', value: activeRequests.value.length, note: 'Not closed' },
  { label: 'In Testing', value: testingCount.value, note: 'Internal test queue' },
  { label: 'User Failed', value: failedCount.value, note: 'Needs rework' },
  { label: 'Unassigned', value: requests.value.filter((item) => !item.assignee).length, note: 'Needs owner' }
])

const statusMix = computed(() => {
  const max = Math.max(...requestStatuses.map((status) => requests.value.filter((item) => item.status === status).length), 1)
  return requestStatuses.map((status) => {
    const count = requests.value.filter((item) => item.status === status).length
    return {
      status,
      count,
      width: `${Math.max((count / max) * 100, count ? 12 : 0)}%`
    }
  })
})

const loadRequests = async () => {
  loading.value = true
  try {
    const response = await requestApi.list({ page: 0, size: 100 })
    requests.value = response.content
  } catch {
    ElMessage.error('Unable to load workspace overview')
  } finally {
    loading.value = false
  }
}

onMounted(loadRequests)
</script>

<style scoped lang="scss">
.workspace-page {
  display: grid;
  gap: 16px;
  width: 100%;
}

.overview-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 16px;
  align-items: start;
}

.status-stack {
  display: grid;
  gap: 12px;
}

.status-row {
  display: grid;
  grid-template-columns: 150px minmax(0, 1fr) 28px;
  gap: 10px;
  align-items: center;
}

.status-bar {
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: #eef2f6;
}

.status-bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #000080;
}

.status-row strong {
  text-align: right;
  color: #111827;
}

@media (max-width: 1180px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>
