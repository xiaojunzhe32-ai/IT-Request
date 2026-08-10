<template>
  <div class="ticket-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>工单列表</span>
          <div>
            <el-button type="primary" @click="showDialog('UserRequest')">
              <el-icon><Plus /></el-icon>
              新建服务请求
            </el-button>
            <el-button type="danger" @click="showDialog('Incident')">
              <el-icon><Plus /></el-icon>
              新建事件
            </el-button>
          </div>
        </div>
      </template>

      <!-- Filters -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="全部类型" clearable style="width: 150px">
            <el-option label="服务请求" value="UserRequest" />
            <el-option label="事件" value="Incident" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="新建" value="NEW" />
            <el-option label="已分派" value="ASSIGNED" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <!-- Table -->
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="#" width="80" />
        <el-table-column prop="finalClass" label="类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.finalClass === 'Incident' ? 'danger' : 'primary'">
              {{ scope.row.finalClass === 'UserRequest' ? '服务请求' : '事件' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="scope">
            <el-tag :type="getPriorityType(scope.row.priority)" size="small">
              P{{ scope.row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="showDetail(scope.row)">查看</el-button>
            <el-button
              size="small"
              type="success"
              @click="handleAssign(scope.row)"
              v-if="scope.row.status === 'NEW'"
            >
              分派
            </el-button>
            <el-button
              size="small"
              type="warning"
              @click="handleResolve(scope.row)"
              v-if="scope.row.status === 'ASSIGNED'"
            >
              解决
            </el-button>
            <el-button
              size="small"
              type="info"
              @click="handleClose(scope.row)"
              v-if="scope.row.status === 'RESOLVED'"
            >
              关闭
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- Create Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" />
        </el-form-item>

        <el-form-item label="组织" prop="organizationId">
          <el-select v-model="formData.organizationId" style="width: 100%">
            <el-option
              v-for="org in organizations"
              :key="org.id"
              :label="org.name"
              :value="org.id"
            />
          </el-select>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="影响">
              <el-select v-model="formData.impact" style="width: 100%">
                <el-option label="高" value="1" />
                <el-option label="中" value="2" />
                <el-option label="低" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="紧急度">
              <el-select v-model="formData.urgency" style="width: 100%">
                <el-option label="高" value="1" />
                <el-option label="中" value="2" />
                <el-option label="低" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="优先级">
              <el-select v-model="formData.priority" style="width: 100%">
                <el-option label="高" value="1" />
                <el-option label="中" value="2" />
                <el-option label="低" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailDialogVisible" title="工单详情" width="750px">
      <el-descriptions :column="2" border v-if="currentTicket">
        <el-descriptions-item label="工单ID">{{ currentTicket.id }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="currentTicket.finalClass === 'Incident' ? 'danger' : 'primary'">
            {{ currentTicket.finalClass === 'UserRequest' ? '服务请求' : '事件' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ currentTicket.title }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentTicket.status)">
            {{ getStatusLabel(currentTicket.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityType(currentTicket.priority)" size="small">
            P{{ currentTicket.priority }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="影响">{{ getImpactLabel(currentTicket.impact) }}</el-descriptions-item>
        <el-descriptions-item label="紧急度">{{ getUrgencyLabel(currentTicket.urgency) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentTicket.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="最后更新">{{ formatDateTime(currentTicket.lastUpdateDate) }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ currentTicket.description || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="解决方案" :span="2" v-if="currentTicket.solution">
          {{ currentTicket.solution }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">附件</el-divider>
      <AttachmentUpload
        v-if="currentTicket"
        entity-type="TICKET"
        :entity-id="currentTicket.id"
      />

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Assign Dialog -->
    <el-dialog v-model="assignDialogVisible" title="分派工单" width="500px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="分派方式">
          <el-radio-group v-model="assignForm.assignType">
            <el-radio label="team">分派给团队</el-radio>
            <el-radio label="agent">分派给个人</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择团队" v-if="assignForm.assignType === 'team'">
          <el-select v-model="assignForm.teamId" placeholder="请选择团队" style="width: 100%">
            <el-option
              v-for="team in teams"
              :key="team.id"
              :label="team.name"
              :value="team.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处理人ID" v-if="assignForm.assignType === 'agent'">
          <el-input v-model="assignForm.agentId" placeholder="处理人ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- Resolve Dialog -->
    <el-dialog v-model="resolveDialogVisible" title="解决工单" width="500px">
      <el-form :model="resolveForm" label-width="80px">
        <el-form-item label="解决方案">
          <el-input v-model="resolveForm.solution" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResolve" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'
import AttachmentUpload from '@/components/AttachmentUpload.vue'
import { formatDateTime } from '@/utils/format'

interface Ticket {
  id: number
  title: string
  description?: string
  finalClass: string
  status: string
  priority: string
  impact?: string
  urgency?: string
  solution?: string
  createdAt: string
  lastUpdateDate?: string
}

interface Organization {
  id: number
  name: string
}

interface Team {
  id: number
  name: string
  teamCode: string
  teamType: string
}

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<Ticket[]>([])
const organizations = ref<Organization[]>([])
const teams = ref<Team[]>([])
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const assignDialogVisible = ref(false)
const resolveDialogVisible = ref(false)
const dialogTitle = ref('')
const currentTicket = ref<Ticket | null>(null)
const formRef = ref<FormInstance>()

const searchForm = reactive({
  type: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const formData = reactive({
  title: '',
  description: '',
  organizationId: 1,
  impact: '2',
  urgency: '2',
  priority: '2'
})

const assignForm = reactive({
  assignType: 'team',
  agentId: '',
  teamId: ''
})

const resolveForm = reactive({
  solution: ''
})

const ticketType = ref('UserRequest')

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  organizationId: [{ required: true, message: '请选择组织', trigger: 'change' }]
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    NEW: 'info',
    ASSIGNED: 'warning',
    RESOLVED: 'success',
    CLOSED: ''
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    NEW: '新建',
    ASSIGNED: '已分派',
    RESOLVED: '已解决',
    CLOSED: '已关闭'
  }
  return map[status] || status
}

const getPriorityType = (priority: string) => {
  const map: Record<string, string> = {
    '1': 'danger',
    '2': 'warning',
    '3': 'info'
  }
  return map[priority] || 'info'
}

const getImpactLabel = (impact: string) => {
  const map: Record<string, string> = {
    '1': '高',
    '2': '中',
    '3': '低'
  }
  return map[impact] || impact
}

const getUrgencyLabel = (urgency: string) => {
  const map: Record<string, string> = {
    '1': '高',
    '2': '中',
    '3': '低'
  }
  return map[urgency] || urgency
}


const loadOrganizations = async () => {
  try {
    const data = await request.get('/organizations', {
      params: { size: 100 }
    }) as any
    organizations.value = data.content || []
  } catch (error) {
    console.error('Failed to load organizations:', error)
  }
}

const loadTeams = async () => {
  try {
    const data = await request.get('/teams', {
      params: { size: 100 }
    }) as any
    teams.value = data.content || []
  } catch (error) {
    console.error('Failed to load teams:', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page - 1,
      size: pagination.size
    }

    if (searchForm.type) {
      params.type = searchForm.type
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }

    const data = await request.get('/tickets', { params }) as any
    tableData.value = data.content || []
    pagination.total = data.totalElements || 0
  } catch (error) {
    console.error('Failed to load tickets:', error)
    ElMessage.error('加载工单列表失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const showDialog = (type: string) => {
  ticketType.value = type
  dialogTitle.value = type === 'UserRequest' ? '新建服务请求' : '新建事件'
  Object.assign(formData, {
    title: '',
    description: '',
    organizationId: organizations.value[0]?.id || 1,
    impact: type === 'Incident' ? '1' : '2',
    urgency: type === 'Incident' ? '1' : '2',
    priority: type === 'Incident' ? '1' : '2'
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const endpoint = ticketType.value === 'UserRequest' ? '/tickets/user-requests' : '/tickets/incidents'
        await request.post(endpoint, formData)
        ElMessage.success('创建成功')
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('Failed to create ticket:', error)
        ElMessage.error('创建失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const showDetail = (ticket: Ticket) => {
  currentTicket.value = ticket
  detailDialogVisible.value = true
}

const handleAssign = (ticket: Ticket) => {
  currentTicket.value = ticket
  Object.assign(assignForm, {
    assignType: 'team',
    agentId: '',
    teamId: ''
  })
  assignDialogVisible.value = true
}

const submitAssign = async () => {
  if (!currentTicket.value) return

  submitting.value = true
  try {
    const payload: any = {}
    if (assignForm.assignType === 'team') {
      payload.teamId = Number(assignForm.teamId)
    } else {
      payload.agentId = Number(assignForm.agentId)
    }
    await request.put(`/tickets/${currentTicket.value.id}/assign`, payload)
    ElMessage.success('分派成功')
    assignDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('Failed to assign ticket:', error)
    ElMessage.error('分派失败')
  } finally {
    submitting.value = false
  }
}

const handleResolve = (ticket: Ticket) => {
  currentTicket.value = ticket
  resolveForm.solution = ''
  resolveDialogVisible.value = true
}

const submitResolve = async () => {
  if (!currentTicket.value) return

  submitting.value = true
  try {
    await request.put(`/tickets/${currentTicket.value.id}/resolve`, {
      solution: resolveForm.solution
    })
    ElMessage.success('解决成功')
    resolveDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('Failed to resolve ticket:', error)
    ElMessage.error('解决失败')
  } finally {
    submitting.value = false
  }
}

const handleClose = async (ticket: Ticket) => {
  try {
    await ElMessageBox.confirm('确定要关闭该工单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request.put(`/tickets/${ticket.id}/close`)
    ElMessage.success('关闭成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Failed to close ticket:', error)
      ElMessage.error('关闭失败')
    }
  }
}

onMounted(() => {
  loadOrganizations()
  loadTeams()
  loadData()
})
</script>

<style scoped lang="scss">
.ticket-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }
}
</style>