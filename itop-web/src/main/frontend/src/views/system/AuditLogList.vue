<template>
  <div class="admin-page">
    <PageHeader eyebrow="Admin Console" title="Audit Logs" description="Trace request creation, assignment, status changes, comments and administration activity." />
    <el-card v-loading="loading" class="surface-card" shadow="never">
      <div class="toolbar-row"><el-input v-model="entityType" placeholder="Entity type" clearable /><el-input v-model="action" placeholder="Action" clearable /><el-button @click="loadLogs">Apply Filters</el-button></div>
      <el-table :data="logs" row-key="id" highlight-current-row>
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { formatDateTime } from '@/utils/format'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { auditLogApi } from '@/api/system'
import type { AuditLog } from '@/types/system'


const logs = ref<AuditLog[]>([])
const loading = ref(false)
const entityType = ref('')
const action = ref('')
const loadLogs = async () => { loading.value = true; try { logs.value = (await auditLogApi.list({ page: 0, size: 100, entityType: entityType.value || undefined, action: action.value || undefined })).content } catch { ElMessage.error('Unable to load audit logs') } finally { loading.value = false } }
onMounted(loadLogs)
</script>

<style scoped lang="scss">
.admin-page { display: grid; gap: 16px; }
.toolbar-row { display: grid; grid-template-columns: 220px 220px auto; gap: 12px; margin-bottom: 14px; justify-content: start; }
@media (max-width: 760px) { .toolbar-row { grid-template-columns: 1fr; } }
</style>
