<template>
  <div class="workspace-page">
    <PageHeader
      eyebrow="IT Workspace"
      title="My Tasks"
      description="Technician-focused view for assigned requests, handoff to testing and rework after user test failure."
    />

    <div v-loading="loading" class="task-status-list">
      <section
        v-for="section in statusSections"
        :key="section.status"
        class="task-status-section"
      >
        <header class="task-status-header">
          <div>
            <span>{{ section.status }}</span>
            <strong>{{ section.items.length }}</strong>
          </div>
        </header>

        <article
          v-for="request in section.items"
          :key="request.id"
          class="task-card"
        >
          <div class="task-card__top">
            <a class="task-no-link" @click="router.push(`/workspace/requests/${request.id}`)">{{ request.requestNo }}</a>
            <PriorityTag :priority="request.priority" />
          </div>
          <h3>{{ request.title }}</h3>
          <p>{{ request.requester }} · {{ request.assignedTeam || 'Unassigned team' }}</p>
          <RequestStatusTag :status="request.status" />
        </article>
        <div v-if="!section.items.length" class="empty-column">No requests</div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PriorityTag from '@/components/PriorityTag.vue'
import RequestStatusTag from '@/components/RequestStatusTag.vue'
import { requestApi } from '@/api/requests'
import { useUserStore } from '@/stores/user'
import type { WorkflowRequest } from '@/types/requests'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const requests = ref<WorkflowRequest[]>([])
const priorityRank: Record<WorkflowRequest['priority'], number> = {
  Critical: 0,
  High: 1,
  Medium: 2,
  Low: 3
}

const statusOrder: WorkflowRequest['status'][] = [
  'Assigned',
  'In Progress',
  'To be test',
  'Testing',
  'Resolved',
  'User Test Failed',
  'Closed'
]

const visibleTasks = computed(() => requests.value
  .filter((item) => statusOrder.includes(item.status))
  .sort((left, right) => {
    const leftStatusRank = statusOrder.indexOf(left.status)
    const rightStatusRank = statusOrder.indexOf(right.status)
    if (leftStatusRank !== rightStatusRank) return leftStatusRank - rightStatusRank

    const priorityDifference = priorityRank[left.priority] - priorityRank[right.priority]
    if (priorityDifference !== 0) return priorityDifference

    return new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime()
  }))

const statusSections = computed(() => statusOrder.map((status) => ({
  status,
  items: visibleTasks.value.filter((item) => item.status === status)
})))

const loadTasks = async () => {
  loading.value = true
  try {
    if (!userStore.user) {
      await userStore.fetchCurrentUser()
    }
    const response = await requestApi.list({
      page: 0,
      size: 100,
      assigneeId: userStore.user?.id
    })
    requests.value = response.content
  } catch {
    ElMessage.error('Unable to load my tasks')
  } finally {
    loading.value = false
  }
}

onMounted(loadTasks)
</script>

<style scoped lang="scss">
.workspace-page {
  display: grid;
  gap: 16px;
}

.task-status-list {
  display: grid;
  gap: 14px;
}

.task-status-section {
  display: grid;
  gap: 10px;
}

.task-status-header {
  padding-bottom: 6px;
}

.task-status-header div {
  display: flex;
  align-items: center;
  gap: 8px;
}

.task-status-header span {
  color: #111827;
  font-size: 13px;
  font-weight: 800;
}

.task-status-header strong {
  display: grid;
  place-items: center;
  min-width: 22px;
  height: 22px;
  padding: 0 7px;
  border-radius: 999px;
  background: #eef2ff;
  color: #000080;
  font-size: 12px;
}

.task-card {
  display: grid;
  gap: 10px;
  margin-bottom: 12px;
  padding: 14px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  cursor: default;
}

.task-card:hover {
  border-color: #e5e7eb;
  background: #f8fafc;
}

.task-card__top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.task-card__top .task-no-link,
.task-card h3 {
  color: #111827;
}

.task-no-link {
  font-weight: 600;
  cursor: pointer;
  color: #2563eb;
}

.task-no-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

.task-card h3 {
  margin: 0;
  font-size: 14px;
  line-height: 1.35;
}

.task-card p,
.empty-column {
  margin: 0;
  color: #667085;
  font-size: 12px;
}

.empty-column {
  padding: 2px 0 10px;
  text-align: left;
}

</style>
