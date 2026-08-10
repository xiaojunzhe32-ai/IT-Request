<template>
  <div class="admin-page">
    <PageHeader
      eyebrow="Admin Console"
      title="Admin Overview"
      description="Maximum-permission technical lead console for organization, access, routing and request governance."
    />

    <div class="metric-grid">
      <div v-for="metric in metrics" :key="metric.label" class="metric-card">
        <span>{{ metric.label }}</span>
        <strong>{{ metric.value }}</strong>
        <small>{{ metric.note }}</small>
      </div>
    </div>

    <div class="admin-grid">
      <el-card class="surface-card" shadow="never">
        <template #header>
          <div class="card-title-row">
            <div>
              <strong>Admin Scope</strong>
              <span>Only request workflow administration is retained</span>
            </div>
          </div>
        </template>
        <div class="module-grid">
          <article v-for="item in modules" :key="item.title" class="module-tile" @click="router.push(item.path)">
            <el-icon><component :is="item.icon" /></el-icon>
            <div>
              <strong>{{ item.title }}</strong>
              <span>{{ item.description }}</span>
            </div>
          </article>
        </div>
      </el-card>

      <el-card class="surface-card" shadow="never">
        <template #header>
          <div class="card-title-row">
            <div>
              <strong>Recent Audit</strong>
              <span>Request and administration changes</span>
            </div>
          </div>
        </template>
        <div class="audit-mini-list">
          <article v-for="log in recentAuditLogs" :key="log.id" class="audit-mini-item">
            <strong>{{ log.action }}</strong>
            <span>{{ log.username || 'System' }} · {{ log.entityType }} {{ log.entityId || '' }}</span>
            <small>{{ formatDateTime(log.createdAt) }}</small>
          </article>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { formatDateTime } from '@/utils/format'
import { Connection, Document, Key, OfficeBuilding, Tickets, User, UserFilled } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { dashboardApi } from '@/api/system'
import type { DashboardStats } from '@/types/system'

const router = useRouter()
const stats = ref<DashboardStats>({})
const recentAuditLogs = computed(() => stats.value.recentAuditLogs || [])

const metrics = computed(() => [
  { label: 'Open Requests', value: (stats.value.totalTickets || 0) - (stats.value.closedTickets || 0), note: 'Across all teams' },
  { label: 'Users', value: stats.value.activeUsers || 0, note: 'Active accounts' },
  { label: 'Teams', value: stats.value.activeTeams || 0, note: 'Active IT teams' },
  { label: 'Routing Rules', value: stats.value.enabledRoutingRules || 0, note: 'Enabled rules' }
])

const modules = [
  { title: 'Organizations', description: 'Visibility boundaries and departments', path: '/organizations', icon: OfficeBuilding },
  { title: 'Users', description: 'Accounts, roles and status', path: '/users', icon: User },
  { title: 'Roles', description: 'Permission bundles by role', path: '/roles', icon: Key },
  { title: 'Teams', description: 'IT ownership and members', path: '/teams', icon: UserFilled },
  { title: 'Routing Rules', description: 'Automatic request assignment', path: '/routing-rules', icon: Connection },
  { title: 'Requests', description: 'Global request control view', path: '/requests', icon: Tickets },
  { title: 'Audit Logs', description: 'Request and admin event history', path: '/audit-logs', icon: Document }
]

onMounted(async () => {
  try { stats.value = await dashboardApi.stats() }
  catch { ElMessage.error('Unable to load admin overview') }
})
</script>

<style scoped lang="scss">
.admin-page {
  display: grid;
  gap: 16px;
}

.admin-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 16px;
  align-items: start;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.module-tile {
  display: flex;
  gap: 12px;
  padding: 14px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  cursor: pointer;
}

.module-tile:hover {
  border-color: #f59e0b;
  background: #fff;
}

.module-tile .el-icon {
  color: #d97706;
  font-size: 20px;
}

.module-tile strong,
.audit-mini-item strong {
  display: block;
  color: #111827;
  font-size: 13px;
}

.module-tile span,
.audit-mini-item span,
.audit-mini-item small {
  display: block;
  margin-top: 4px;
  color: #667085;
  font-size: 12px;
}

.audit-mini-list {
  display: grid;
  gap: 10px;
}

.audit-mini-item {
  padding: 12px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
}

@media (max-width: 1180px) {
  .admin-grid,
  .module-grid {
    grid-template-columns: 1fr;
  }
}
</style>
