<template>
  <div class="admin-page">
    <PageHeader eyebrow="Admin Console" title="Audit Logs" description="Trace request creation, assignment, status changes, comments and administration activity." />
    <el-card v-loading="loading" class="surface-card" shadow="never">
      <div class="toolbar-row"><el-input v-model="entityType" placeholder="Entity type" clearable /><el-input v-model="action" placeholder="Action" clearable /><el-button @click="loadLogs">Apply Filters</el-button></div>
      <el-table :data="pagedLogs" row-key="id" highlight-current-row>
        <el-table-column label="Time" width="200">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="username" label="Actor" width="170" />
        <el-table-column prop="entityType" label="Area" width="130" />
        <el-table-column prop="action" label="Action" width="180"><template #default="{ row }"><el-tag size="small" effect="light">{{ row.action }}</el-tag></template></el-table-column>
        <el-table-column label="Target" width="150"><template #default="{ row }">{{ row.entityType }} {{ row.entityId || '' }}</template></el-table-column>
        <el-table-column prop="description" label="Detail" min-width="260" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP" width="140" />
      </el-table>
      <el-pagination
        v-if="logs.length > logPageSize"
        v-model:current-page="logCurrentPage"
        :page-size="logPageSize"
        :total="logs.length"
        layout="total, prev, pager, next"
        class="table-pagination"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { formatDateTime } from '@/utils/format'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { auditLogApi } from '@/api/system'
import type { AuditLog } from '@/types/system'


const logs = ref<AuditLog[]>([])
const loading = ref(false)
const entityType = ref('')
const action = ref('')
const logPageSize = 20
const logCurrentPage = ref(1)
const pagedLogs = computed(() => {
  const start = (logCurrentPage.value - 1) * logPageSize
  return logs.value.slice(start, start + logPageSize)
})
const loadLogs = async () => {
  loading.value = true
  try {
    logs.value = (await auditLogApi.list({ page: 0, size: 100, entityType: entityType.value || undefined, action: action.value || undefined })).content
    logCurrentPage.value = 1
  } catch { ElMessage.error('Unable to load audit logs') } finally { loading.value = false }
}
onMounted(loadLogs)
</script>

<style scoped lang="scss">
.admin-page { display: grid; gap: 16px; }
.toolbar-row { display: grid; grid-template-columns: 220px 220px auto; gap: 12px; margin-bottom: 14px; justify-content: start; }
.table-pagination { margin-top: 16px; justify-content: flex-end; }
@media (max-width: 760px) { .toolbar-row { grid-template-columns: 1fr; } }
</style>
