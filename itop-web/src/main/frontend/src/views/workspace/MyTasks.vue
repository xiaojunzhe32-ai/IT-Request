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

        <div class="task-module__body">
          <article
            v-for="request in pagedModuleItems(module.title, module.items)"
            :key="request.id"
            class="task-card"
          >
            <div class="task-card__top">
              <a href="javascript:void(0)" class="task-no-link" @click="router.push(`/workspace/requests/${request.id}`)">{{ request.requestNo }}</a>
              <PriorityTag :priority="request.priority" />
            </div>
            <h3>{{ request.title }}</h3>
            <p>{{ request.requester }} · {{ request.assignedTeam || 'Unassigned team' }}</p>
            <RequestStatusTag :status="request.status" />
          </article>
          <div v-if="!module.items.length" class="empty-column">No requests</div>
        </div>

        <el-pagination
          v-if="module.items.length > modulePageSize"
          v-model:current-page="modulePages[module.title]"
          :page-size="modulePageSize"
          :total="module.items.length"
          layout="prev, pager, next"
          small
          class="module-pagination"
        />
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
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
const modulePageSize = 5
const modulePages = reactive<Record<string, number>>({})

const pagedModuleItems = (title: string, items: WorkflowRequest[]) => {
  const page = modulePages[title] || 1
  const start = (page - 1) * modulePageSize
  return items.slice(start, start + modulePageSize)
}
const priorityRank: Record<WorkflowRequest['priority'], number> = {
  Critical: 0,
  High: 1,
  Medium: 2,
  Low: 3
}

const statusRank: Record<WorkflowRequest['status'], number> = {
  New: 0,
  Assigned: 1,
  'In Progress': 2,
  'To be test': 3,
  Testing: 4,
  Resolved: 5,
  'User Test Failed': 6,
  Closed: 7
}

const sortTasks = (items: WorkflowRequest[]) => [...items].sort((left, right) => {
  const statusDifference = statusRank[left.status] - statusRank[right.status]
  if (statusDifference !== 0) return statusDifference

  const priorityDifference = priorityRank[left.priority] - priorityRank[right.priority]
  if (priorityDifference !== 0) return priorityDifference

  return right.id - left.id
})

const taskModules = computed(() => {
  const makeModule = (
    title: string,
    description: string,
    accent: string,
    statuses: WorkflowRequest['status'][]
  ) => {
    const items = sortTasks(requests.value.filter((item) => statuses.includes(item.status)))

    return {
      title,
      description,
      accent,
      items,
      total: items.length
    }
  }

  return [
    makeModule('Work in Hand', 'Assigned and active implementation', '#000080', ['Assigned', 'In Progress']),
    makeModule('Test Readiness', 'Ready for test and internal validation', '#4f46e5', ['To be test', 'Testing']),
    makeModule('Feedback & Closure', 'Resolved, returned or closed requests', '#0f766e', ['Resolved', 'User Test Failed', 'Closed'])
  ]
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
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.95), #fff);
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.task-module__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--module-accent);
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
  line-height: 1.4;
}

.task-module__header em {
  display: grid;
  place-items: center;
  min-width: 30px;
  height: 30px;
  padding: 0 9px;
  border-radius: 999px;
  background: var(--module-accent);
  color: #fff;
  font-size: 13px;
  font-style: normal;
  font-weight: 800;
}

.task-module__body {
  display: grid;
  gap: 12px;
}

.module-pagination {
  margin-top: 12px;
  justify-content: center;
}

.task-card {
  display: grid;
  gap: 10px;
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

@media (max-width: 1180px) {
  .task-modules {
    grid-template-columns: 1fr;
  }
}

</style>
