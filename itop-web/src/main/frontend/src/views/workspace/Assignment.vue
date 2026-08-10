<template>
  <div class="workspace-page">
    <PageHeader
      eyebrow="IT Workspace"
      title="Assignment Desk"
      description="Technical leads and admins assign new requests to teams and technicians."
    />

    <div class="assignment-layout">
      <el-card v-loading="loading" class="surface-card" shadow="never">
        <template #header>
          <div class="card-title-row">
            <div>
              <strong>Needs Assignment</strong>
              <span>New and assigned requests requiring owner review</span>
            </div>
          </div>
        </template>
        <RequestTable
          :requests="assignmentCandidates"
          detail-base="/workspace/requests"
          workspace-mode
          empty-text="No assignment candidates"
        />
      </el-card>

      <el-card v-if="canReadRoutingRules" class="surface-card" shadow="never">
        <template #header>
          <div class="card-title-row">
            <div>
              <strong>Routing Rules</strong>
              <span>Suggested assignment defaults</span>
            </div>
          </div>
        </template>
        <div class="rule-list">
          <article v-for="rule in routingRules" :key="rule.id" class="rule-item">
            <strong>{{ rule.name }}</strong>
            <span>{{ rule.requestType || 'Any request type' }} · {{ rule.teamName || 'No team' }}</span>
            <small>{{ rule.organizationName || 'Any organization' }}</small>
          </article>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import RequestTable from '@/components/RequestTable.vue'
import { requestApi } from '@/api/requests'
import { routingRuleApi } from '@/api/system'
import { useUserStore } from '@/stores/user'
import type { WorkflowRequest } from '@/types/requests'
import type { RoutingRule } from '@/types/system'

const loading = ref(false)
const requests = ref<WorkflowRequest[]>([])
const routingRules = ref<RoutingRule[]>([])
const userStore = useUserStore()
const canReadRoutingRules = computed(() => userStore.hasPermission('routing:read'))
const priorityRank: Record<WorkflowRequest['priority'], number> = {
  Critical: 0,
  High: 1,
  Medium: 2,
  Low: 3
}

const assignmentCandidates = computed(() =>
  requests.value
    .filter((request) => request.status === 'New' || request.status === 'Assigned')
    .sort((left, right) => {
      const assignmentDifference = Number(Boolean(left.agentId)) - Number(Boolean(right.agentId))
      if (assignmentDifference !== 0) return assignmentDifference
      const priorityDifference = priorityRank[left.priority] - priorityRank[right.priority]
      if (priorityDifference !== 0) return priorityDifference
      return new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime()
    })
)

const loadCandidates = async () => {
  loading.value = true
  try {
    const response = await requestApi.list({ page: 0, size: 100 })
    requests.value = response.content
  } catch {
    ElMessage.error('Unable to load assignment candidates')
  } finally {
    loading.value = false
  }
}

const loadRoutingRules = async () => {
  if (!canReadRoutingRules.value) return
  try {
    const rules = await routingRuleApi.list()
    routingRules.value = rules.filter((rule) => rule.enabled)
  } catch {
    ElMessage.error('Unable to load routing rules')
  }
}

onMounted(() => {
  loadCandidates()
  loadRoutingRules()
})
</script>

<style scoped lang="scss">
.workspace-page {
  display: grid;
  gap: 16px;
}

.assignment-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 16px;
  align-items: start;
}

.rule-list {
  display: grid;
  gap: 10px;
}

.rule-item {
  display: grid;
  gap: 5px;
  padding: 12px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
}

.rule-item strong {
  color: #111827;
  font-size: 13px;
}

.rule-item span,
.rule-item small {
  color: #667085;
  font-size: 12px;
}

@media (max-width: 1180px) {
  .assignment-layout {
    grid-template-columns: 1fr;
  }
}
</style>
