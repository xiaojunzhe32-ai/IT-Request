<template>
  <div class="workspace-page">
    <PageHeader
      eyebrow="IT Workspace"
      title="My Tasks"
      description="Technician-focused view for assigned requests, handoff to testing and rework after user test failure."
    />

    <div v-loading="loading" class="task-columns">
      <section v-for="column in columns" :key="column.title" class="task-column">
        <header>
          <strong>{{ column.title }}</strong>
          <span>{{ column.items.length }}</span>
        </header>
        <article
          v-for="request in column.items"
          :key="request.id"
          class="task-card"
          @click="router.push(`/workspace/requests/${request.id}`)"
        >
          <div class="task-card__top">
            <strong>{{ request.requestNo }}</strong>
            <PriorityTag :priority="request.priority" />
          </div>
          <h3>{{ request.title }}</h3>
          <p>{{ request.requester }} · {{ request.requesterOrg }}</p>
          <RequestStatusTag :status="request.status" />
        </article>
        <div v-if="!column.items.length" class="empty-column">No requests</div>
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

const assignedToMe = computed(() => requests.value
  .filter((item) => item.status !== 'Closed')
  .sort((left, right) => {
    const priorityDifference = priorityRank[left.priority] - priorityRank[right.priority]
    if (priorityDifference !== 0) return priorityDifference
    return new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime()
  }))

const columns = computed(() => [
  {
    title: 'Assigned',
    items: assignedToMe.value.filter((item) => item.status === 'Assigned')
  },
  {
    title: 'In Progress',
    items: assignedToMe.value.filter((item) => item.status === 'In Progress' || item.status === 'User Test Failed')
  },
  {
    title: 'Testing',
    items: assignedToMe.value.filter((item) => item.status === 'Testing')
  }
])

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

.task-columns {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.task-column {
  min-height: 520px;
  padding: 14px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.task-column header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.task-column header strong {
  color: #111827;
}

.task-column header span {
  display: grid;
  place-items: center;
  width: 26px;
  height: 26px;
  border-radius: 999px;
  background: #fff7ed;
  color: #b45309;
  font-size: 12px;
  font-weight: 800;
}

.task-card {
  display: grid;
  gap: 10px;
  margin-bottom: 12px;
  padding: 14px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  cursor: pointer;
}

.task-card:hover {
  border-color: #f59e0b;
  background: #fff;
}

.task-card__top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.task-card__top strong,
.task-card h3 {
  color: #111827;
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
  padding: 18px;
  text-align: center;
  border: 1px dashed #d8dee6;
  border-radius: 10px;
}

@media (max-width: 1080px) {
  .task-columns {
    grid-template-columns: 1fr;
  }
}
</style>
