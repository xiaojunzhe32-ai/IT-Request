<template>
  <div class="portal-page">
    <PageHeader
      eyebrow="User Portal"
      :title="pageTitle"
      description="Track owned requests and inspect other submitted requests in read-only mode."
    >
      <template #actions>
        <el-button type="primary" @click="router.push('/portal/new-request')">
          <el-icon><Plus /></el-icon>
          New Request
        </el-button>
      </template>
    </PageHeader>

    <el-card v-loading="loading" class="surface-card" shadow="never">
      <div class="toolbar-row">
        <el-input v-model="keyword" placeholder="Search request number or title" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="Status" clearable>
          <el-option v-for="status in requestStatuses" :key="status" :label="status" :value="status" />
        </el-select>
        <el-select v-model="ownership" placeholder="Visibility">
          <el-option label="My Requests" value="mine" />
          <el-option label="All Requests" value="all" />
        </el-select>
      </div>

      <RequestTable
        :requests="filteredRequests"
        detail-base="/portal/requests"
        :show-requester="ownership === 'all'"
        empty-text="No matching requests"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import RequestTable from '@/components/RequestTable.vue'
import { requestApi } from '@/api/requests'
import { requestStatuses } from '@/data/requestOptions'
import { useUserStore } from '@/stores/user'
import type { RequestStatus, WorkflowRequest } from '@/types/requests'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const keyword = ref('')
const statusFilter = ref<RequestStatus | ''>('')
const ownership = ref<'mine' | 'all'>('mine')
const loading = ref(false)
const requests = ref<WorkflowRequest[]>([])

const routeMode = computed(() => String(route.name || ''))
const pageTitle = computed(() => {
  if (routeMode.value === 'PortalOngoingRequests') return 'Ongoing Requests'
  if (routeMode.value === 'PortalClosedRequests') return 'Closed Requests'
  return 'All Requests'
})

const filteredRequests = computed(() =>
  requests.value.filter((request) => {
    if (routeMode.value === 'PortalOngoingRequests') return request.status !== 'Closed'
    if (routeMode.value === 'PortalClosedRequests') return request.status === 'Closed'
    return true
  })
)

const loadRequests = async () => {
  loading.value = true
  try {
    if (!userStore.user) {
      await userStore.fetchCurrentUser()
    }
    const response = await requestApi.list({
      page: 0,
      size: 100,
      search: keyword.value || undefined,
      status: statusFilter.value || (routeMode.value === 'PortalClosedRequests' ? 'Closed' : undefined),
      callerId: ownership.value === 'mine' ? userStore.user?.id : undefined
    })
    requests.value = response.content
  } catch {
    ElMessage.error('Unable to load requests')
  } finally {
    loading.value = false
  }
}

watch([keyword, statusFilter, ownership, routeMode], loadRequests)
onMounted(loadRequests)
</script>

<style scoped lang="scss">
.portal-page {
  display: grid;
  gap: 16px;
}

.toolbar-row {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) 180px 180px;
  gap: 12px;
  margin-bottom: 14px;
}

@media (max-width: 920px) {
  .toolbar-row {
    grid-template-columns: 1fr;
  }
}
</style>
