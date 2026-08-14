<template>
  <div class="workspace-page">
    <PageHeader
      eyebrow="IT Workspace"
      title="My Tasks"
      description="Technician-focused view for assigned requests, handoff to testing and rework after user test failure."
    />

    <div v-loading="loading" class="task-modules">
      <section
        v-for="module in taskModules"
        :key="module.title"
        class="task-module"
        :style="{ '--module-accent': module.accent }"
      >
        <header class="task-module__header">
          <div>
            <strong>{{ module.title }}</strong>
            <span>{{ module.description }}</span>
          </div>
          <em>{{ module.total }}</em>
        </header>

        <div class="task-module__lanes">
          <section v-for="lane in module.lanes" :key="lane.title" class="task-lane">
            <header class="task-lane__header">
              <span>{{ lane.title }}</span>
              <strong>{{ lane.items.length }}</strong>
            </header>
            <article
              v-for="request in lane.items"
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
            <div v-if="!lane.items.length" class="empty-column">No requests</div>
          </section>
        </div>
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

const taskModules = computed(() => {
  const makeLane = (title: string, statuses: WorkflowRequest['status'][], source = assignedToMe.value) => ({
    title,
    items: source.filter((item) => statuses.includes(item.status))
  })

  const completedRequests = requests.value
    .filter((item) => item.status === 'Resolved' || item.status === 'Closed')
    .sort((left, right) => new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime())

  return [
    {
      title: 'Work in Hand',
      description: 'Assigned and active implementation',
      accent: '#000080',
      lanes: [
        makeLane('Assigned', ['Assigned']),
        makeLane('In Progress', ['In Progress'])
      ]
    },
    {
      title: 'Test Readiness',
      description: 'Ready for test and internal validation',
      accent: '#4f46e5',
      lanes: [
        makeLane('To be test', ['To be test']),
        makeLane('Testing', ['Testing'])
      ]
    },
    {
      title: 'Feedback & Closure',
      description: 'Resolved, returned or closed requests',
      accent: '#0f766e',
      lanes: [
        makeLane('Resolved', ['Resolved'], completedRequests),
        makeLane('User Test Failed', ['User Test Failed']),
        makeLane('Closed', ['Closed'], completedRequests)
      ]
    }
  ].map((module) => ({
    ...module,
    total: module.lanes.reduce((sum, lane) => sum + lane.items.length, 0)
  }))
})

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

.task-modules {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  align-items: start;
}

.task-module {
  min-height: 520px;
  padding: 14px;
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.9), #fff);
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.task-module__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
  margin-bottom: 12px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.task-module__header div {
  display: grid;
  gap: 4px;
}

.task-module__header strong {
  color: #111827;
  font-size: 15px;
}

.task-module__header span {
  color: #667085;
  font-size: 12px;
}

.task-module__header em {
  display: grid;
  place-items: center;
  min-width: 30px;
  height: 30px;
  padding: 0 9px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--module-accent) 12%, white);
  color: var(--module-accent);
  font-size: 12px;
  font-style: normal;
  font-weight: 800;
}

.task-module__lanes {
  display: grid;
  gap: 12px;
}

.task-lane {
  display: grid;
  gap: 10px;
  padding: 10px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.task-lane__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.task-lane__header span {
  color: #344054;
  font-size: 12px;
  font-weight: 700;
}

.task-lane__header strong {
  display: grid;
  place-items: center;
  min-width: 24px;
  height: 24px;
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
  padding: 18px;
  text-align: center;
  border: 1px dashed #d8dee6;
  border-radius: 10px;
}

@media (max-width: 1180px) {
  .task-modules {
    grid-template-columns: 1fr;
  }
}
</style>
