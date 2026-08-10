<template>
  <div class="workspace-page">
    <PageHeader
      eyebrow="IT Workspace"
      title="Internal Test Queue"
      description="Internal testers verify technician work before the request is released to requester confirmation."
    />

    <el-card v-loading="loading" class="surface-card" shadow="never">
      <RequestTable
        :requests="testingRequests"
        detail-base="/workspace/requests"
        workspace-mode
        empty-text="No requests waiting for internal testing"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import RequestTable from '@/components/RequestTable.vue'
import { requestApi } from '@/api/requests'
import type { WorkflowRequest } from '@/types/requests'

const loading = ref(false)
const testingRequests = ref<WorkflowRequest[]>([])

const loadTestingRequests = async () => {
  loading.value = true
  try {
    const response = await requestApi.list({ page: 0, size: 100, status: 'Testing' })
    testingRequests.value = response.content
  } catch {
    ElMessage.error('Unable to load internal test queue')
  } finally {
    loading.value = false
  }
}

onMounted(loadTestingRequests)
</script>

<style scoped lang="scss">
.workspace-page {
  display: grid;
  gap: 16px;
}
</style>
