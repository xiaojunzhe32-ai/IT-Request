<template>
  <div class="portal-page">
    <PageHeader
      eyebrow="User Portal"
      title="Request Center"
      description="Submit new IT requests, track your own items and view other requests in read-only mode."
    >
      <template #actions>
        <el-button type="primary" @click="router.push('/portal/new-request')">
          <el-icon><Plus /></el-icon>
          New Request
        </el-button>
      </template>
    </PageHeader>

    <div class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.note }}</small>
      </div>
    </div>

    <div class="portal-grid">
      <el-card v-loading="loading" class="surface-card" shadow="never">
        <template #header>
          <div class="card-title-row">
            <div>
              <strong>My Active Requests</strong>
              <span>Owned requests that still need progress or confirmation</span>
            </div>
            <el-button text @click="router.push('/portal/requests/ongoing')">View all</el-button>
          </div>
        </template>
        <RequestTable
          :requests="myActiveRequests"
          detail-base="/portal/requests"
          :show-requester="false"
          empty-text="No active requests"
        />
      </el-card>

      <aside class="side-panel">
        <section class="compact-panel">
          <div class="panel-heading">
            <strong>Next Actions</strong>
            <span>Requester responsibilities</span>
          </div>
          <button class="action-row" type="button" @click="router.push('/portal/new-request')">
            <el-icon><Plus /></el-icon>
            <span>Create request</span>
          </button>
          <button class="action-row" type="button" @click="router.push('/portal/requests')">
            <el-icon><Tickets /></el-icon>
            <span>View all requests</span>
          </button>
          <button class="action-row" type="button" @click="router.push('/portal/requests/closed')">
            <el-icon><CircleCheck /></el-icon>
            <span>Review closed items</span>
          </button>
        </section>

        <section class="compact-panel">
          <div class="panel-heading">
            <strong>Workflow Rule</strong>
            <span>User testing failure</span>
          </div>
          <p class="panel-copy">
            When a resolved request fails requester validation, mark it as User Test Failed.
            IT will continue from In Progress after review.
          </p>
        </section>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck, Plus, Tickets } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import RequestTable from '@/components/RequestTable.vue'
import { requestApi } from '@/api/requests'
import { useUserStore } from '@/stores/user'
import type { WorkflowRequest } from '@/types/requests'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const myRequests = ref<WorkflowRequest[]>([])
const visibleRequests = ref<WorkflowRequest[]>([])

const myActiveRequests = computed(() => myRequests.value.filter((item) => item.status !== 'Closed').slice(0, 5))

const metrics = computed(() => [
  {
    label: 'My Open',
    value: myRequests.value.filter((item) => item.status !== 'Closed').length,
    note: 'Owned by me'
  },
  {
    label: 'Waiting For Test',
    value: myRequests.value.filter((item) => item.status === 'Resolved').length,
    note: 'Need confirmation'
  },
  {
    label: 'Visible Requests',
    value: visibleRequests.value.length,
    note: 'Read-only access'
  },
  {
    label: 'Closed',
    value: myRequests.value.filter((item) => item.status === 'Closed').length,
    note: 'Confirmed'
  }
])

const loadDashboard = async () => {
  loading.value = true
  try {
    if (!userStore.user) {
      await userStore.fetchCurrentUser()
    }
    const userId = userStore.user?.id
    const [mine, visible] = await Promise.all([
      requestApi.list({ page: 0, size: 100, callerId: userId }),
      requestApi.list({ page: 0, size: 100 })
    ])
    myRequests.value = mine.content
    visibleRequests.value = visible.content
  } catch {
    ElMessage.error('Unable to load request dashboard')
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>

<style scoped lang="scss">
.portal-page {
  display: grid;
  gap: 16px;
}

.portal-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 16px;
  align-items: start;
}

.side-panel {
  display: grid;
  gap: 16px;
}

.compact-panel {
  padding: 18px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.panel-heading strong,
.panel-heading span {
  display: block;
}

.panel-heading strong {
  color: #111827;
  font-size: 15px;
}

.panel-heading span,
.panel-copy {
  margin-top: 5px;
  color: #667085;
  font-size: 12px;
  line-height: 1.6;
}

.action-row {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  color: #1f2937;
  cursor: pointer;
  font-weight: 700;
}

.action-row:hover {
  border-color: #f59e0b;
  background: #fff7ed;
}

@media (max-width: 1180px) {
  .portal-grid {
    grid-template-columns: 1fr;
  }
}
</style>
