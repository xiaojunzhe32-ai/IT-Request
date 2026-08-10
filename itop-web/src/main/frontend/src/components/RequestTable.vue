<template>
  <div class="table-shell request-table-shell">
    <el-table
      class="request-table"
      :data="requests"
      :empty-text="emptyText"
      row-key="id"
      highlight-current-row
      @row-click="openRequest"
    >
      <el-table-column label="Request" min-width="280" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="request-title-cell">
            <strong>{{ row.requestNo }}</strong>
            <span>{{ row.title }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="Type" width="150" show-overflow-tooltip />
      <el-table-column prop="priority" label="Priority" width="110">
        <template #default="{ row }">
          <PriorityTag :priority="row.priority" />
        </template>
      </el-table-column>
      <el-table-column prop="status" label="Status" width="150">
        <template #default="{ row }">
          <RequestStatusTag :status="row.status" />
        </template>
      </el-table-column>
      <el-table-column v-if="showRequester" prop="requester" label="Requester" width="140" />
      <el-table-column prop="assignedTeam" label="Team" min-width="170" show-overflow-tooltip />
      <el-table-column prop="assignee" label="Assignee" width="130">
        <template #default="{ row }">
          <span v-if="row.assignee">{{ row.assignee }}</span>
          <span v-else class="muted">Unassigned</span>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="Updated" width="150" />
      <el-table-column label="Actions" width="86" fixed="right" align="center">
        <template #default="{ row }">
          <el-dropdown trigger="click" @command="handleCommand($event, row)">
            <el-button class="row-action-button" text circle @click.stop>
              <el-icon><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="open">Open Details</el-dropdown-item>
                <el-dropdown-item v-if="workspaceMode" command="assign">Assign / Transfer</el-dropdown-item>
                <el-dropdown-item v-if="workspaceMode" command="comment">Add Internal Note</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MoreFilled } from '@element-plus/icons-vue'
import type { WorkflowRequest } from '@/types/requests'
import PriorityTag from '@/components/PriorityTag.vue'
import RequestStatusTag from '@/components/RequestStatusTag.vue'

const props = withDefaults(defineProps<{
  requests: WorkflowRequest[]
  detailBase: string
  showRequester?: boolean
  workspaceMode?: boolean
  emptyText?: string
}>(), {
  showRequester: true,
  workspaceMode: false,
  emptyText: 'No requests found'
})

const router = useRouter()

const openRequest = (row: WorkflowRequest) => {
  router.push(`${props.detailBase}/${row.id}`)
}

const handleCommand = (command: string, row: WorkflowRequest) => {
  if (command === 'open') {
    openRequest(row)
    return
  }
  ElMessage.info(`${command === 'assign' ? 'Assignment' : 'Comment'} action is available in the detail view.`)
}
</script>

<style scoped lang="scss">
.request-table {
  width: 100%;
}

.request-table-shell {
  border-radius: 8px;
}

.request-title-cell {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.request-title-cell strong {
  color: #111827;
  font-size: 13px;
}

.request-title-cell span {
  color: #667085;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.muted {
  color: #98a2b3;
}

.row-action-button {
  width: 30px;
  height: 30px;
  color: #667085;
}
</style>
