<template>
  <div class="change-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>变更管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="变更类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable @change="loadData">
            <el-option label="普通变更" value="NORMAL" />
            <el-option label="标准变更" value="STANDARD" />
            <el-option label="紧急变更" value="EMERGENCY" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更分类">
          <el-select v-model="searchForm.category" placeholder="全部" clearable @change="loadData">
            <el-option label="应用" value="APPLICATION" />
            <el-option label="基础设施" value="INFRASTRUCTURE" />
            <el-option label="文档" value="DOCUMENTATION" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="changeNumber" label="变更编号" width="150" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="changeType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.changeType)">
              {{ getTypeLabel(row.changeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeCategory" label="分类" width="100">
          <template #default="{ row }">
            {{ getCategoryLabel(row.changeCategory) }}
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)">
              {{ getPriorityLabel(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="plannedStartDate" label="计划开始" width="180">
          <template #default="{ row }">
            {{ formatDate(row.plannedStartDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleTasks(row)">任务</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="变更类型" prop="changeType">
              <el-select v-model="form.changeType" placeholder="请选择类型">
                <el-option label="普通变更" value="NORMAL" />
                <el-option label="标准变更" value="STANDARD" />
                <el-option label="紧急变更" value="EMERGENCY" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="变更分类" prop="changeCategory">
              <el-select v-model="form.changeCategory" placeholder="请选择分类">
                <el-option label="应用" value="APPLICATION" />
                <el-option label="基础设施" value="INFRASTRUCTURE" />
                <el-option label="文档" value="DOCUMENTATION" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="变更负责人" prop="changeOwnerId">
              <el-input v-model="form.changeOwnerId" placeholder="负责人ID" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="变更原因" prop="changeReason">
          <el-input v-model="form.changeReason" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="风险评估" prop="riskAssessment">
          <el-input v-model="form.riskAssessment" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="实施计划" prop="implementationPlan">
          <el-input v-model="form.implementationPlan" type="textarea" :rows="4" />
        </el-form-item>

        <el-form-item label="回退计划" prop="rollbackPlan">
          <el-input v-model="form.rollbackPlan" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="测试计划" prop="testPlan">
          <el-input v-model="form.testPlan" type="textarea" :rows="3" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划开始时间" prop="plannedStartDate">
              <el-date-picker
                v-model="form.plannedStartDate"
                type="datetime"
                placeholder="选择日期时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束时间" prop="plannedEndDate">
              <el-date-picker
                v-model="form.plannedEndDate"
                type="datetime"
                placeholder="选择日期时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="影响" prop="impact">
              <el-select v-model="form.impact" placeholder="请选择">
                <el-option label="高" value="1" />
                <el-option label="中" value="2" />
                <el-option label="低" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="紧急度" prop="urgency">
              <el-select v-model="form.urgency" placeholder="请选择">
                <el-option label="高" value="1" />
                <el-option label="中" value="2" />
                <el-option label="低" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority" placeholder="请选择">
                <el-option label="高" value="1" />
                <el-option label="中" value="2" />
                <el-option label="低" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Tasks Dialog -->
    <el-dialog
      v-model="tasksDialogVisible"
      title="变更任务"
      width="900px"
    >
      <div class="tasks-header">
        <span>变更编号: {{ currentChange?.changeNumber }}</span>
        <el-button type="primary" size="small" @click="handleAddTask">
          <el-icon><Plus /></el-icon>
          新增任务
        </el-button>
      </div>

      <el-table :data="tasks" v-loading="tasksLoading" stripe>
        <el-table-column prop="name" label="任务名称" min-width="150" />
        <el-table-column prop="taskType" label="类型" width="100">
          <template #default="{ row }">
            {{ getTaskTypeLabel(row.taskType) }}
          </template>
        </el-table-column>
        <el-table-column prop="taskStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getTaskStatusType(row.taskStatus)">
              {{ getTaskStatusLabel(row.taskStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="plannedStartDate" label="计划开始" width="160">
          <template #default="{ row }">
            {{ formatDate(row.plannedStartDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="actualStartDate" label="实际开始" width="160">
          <template #default="{ row }">
            {{ formatDate(row.actualStartDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button
              v-if="row.taskStatus === 'PENDING'"
              type="success"
              link
              @click="handleStartTask(row)"
            >开始</el-button>
            <el-button
              v-if="row.taskStatus === 'IN_PROGRESS'"
              type="primary"
              link
              @click="handleCompleteTask(row)"
            >完成</el-button>
            <el-button type="danger" link @click="handleDeleteTask(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- Add Task Dialog -->
    <el-dialog
      v-model="taskDialogVisible"
      title="新增任务"
      width="600px"
    >
      <el-form :model="taskForm" :rules="taskRules" ref="taskFormRef" label-width="120px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务类型" prop="taskType">
          <el-select v-model="taskForm.taskType" placeholder="请选择类型">
            <el-option label="规划" value="PLANNING" />
            <el-option label="评审" value="REVIEW" />
            <el-option label="实施" value="IMPLEMENTATION" />
            <el-option label="测试" value="TESTING" />
            <el-option label="回退" value="ROLLBACK" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行说明" prop="instructions">
          <el-input v-model="taskForm.instructions" type="textarea" :rows="4" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划开始" prop="plannedStartDate">
              <el-date-picker
                v-model="taskForm.plannedStartDate"
                type="datetime"
                placeholder="选择日期时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束" prop="plannedEndDate">
              <el-date-picker
                v-model="taskForm.plannedEndDate"
                type="datetime"
                placeholder="选择日期时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitTask">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import request from '@/utils/request'

interface ChangeRequest {
  id?: number
  changeNumber?: string
  changeType: string
  changeCategory: string
  title: string
  changeReason?: string
  riskAssessment?: string
  implementationPlan?: string
  rollbackPlan?: string
  testPlan?: string
  changeOwnerId?: number
  plannedStartDate?: string
  plannedEndDate?: string
  impact?: string
  urgency?: string
  priority?: string
  status?: string
  organizationId?: number
}

interface ChangeTask {
  id?: number
  changeId?: number
  name: string
  taskType: string
  taskStatus?: string
  instructions?: string
  plannedStartDate?: string
  plannedEndDate?: string
  actualStartDate?: string
}

const loading = ref(false)
const tableData = ref<ChangeRequest[]>([])
const dialogVisible = ref(false)
const tasksDialogVisible = ref(false)
const taskDialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const taskFormRef = ref<FormInstance>()

const currentChange = ref<ChangeRequest | null>(null)
const tasks = ref<ChangeTask[]>([])
const tasksLoading = ref(false)

const searchForm = reactive({
  type: '',
  category: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const form = reactive<ChangeRequest>({
  changeType: 'NORMAL',
  changeCategory: 'OTHER',
  title: '',
  changeReason: '',
  riskAssessment: '',
  implementationPlan: '',
  rollbackPlan: '',
  testPlan: '',
  impact: '2',
  urgency: '2',
  priority: '2',
  organizationId: 1
})

const taskForm = reactive<ChangeTask>({
  name: '',
  taskType: 'IMPLEMENTATION',
  instructions: '',
  plannedStartDate: '',
  plannedEndDate: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  changeType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  changeCategory: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const taskRules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }]
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page - 1,
      size: pagination.size
    }
    if (searchForm.type) params.type = searchForm.type
    if (searchForm.category) params.category = searchForm.category

    const res = await request.get('/changes', { params })
    tableData.value = res.content
    pagination.total = res.totalElements
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadTasks = async (changeId: number) => {
  tasksLoading.value = true
  try {
    const res = await request.get(`/change-tasks/by-change/${changeId}`)
    tasks.value = res || []
  } catch (error) {
    ElMessage.error('加载任务失败')
  } finally {
    tasksLoading.value = false
  }
}

const getTypeTag = (type: string) => {
  const map: Record<string, string> = {
    EMERGENCY: 'danger',
    NORMAL: 'warning',
    STANDARD: 'success'
  }
  return map[type] || ''
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    NORMAL: '普通',
    STANDARD: '标准',
    EMERGENCY: '紧急'
  }
  return map[type] || type
}

const getCategoryLabel = (category: string) => {
  const map: Record<string, string> = {
    APPLICATION: '应用',
    INFRASTRUCTURE: '基础设施',
    DOCUMENTATION: '文档',
    OTHER: '其他'
  }
  return map[category] || category
}

const getPriorityType = (priority: string) => {
  const map: Record<string, string> = {
    '1': 'danger',
    '2': 'warning',
    '3': 'info'
  }
  return map[priority] || ''
}

const getPriorityLabel = (priority: string) => {
  const map: Record<string, string> = {
    '1': '高',
    '2': '中',
    '3': '低'
  }
  return map[priority] || priority
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    NEW: 'info',
    ASSIGNED: 'warning',
    APPROVED: 'success',
    DISPATCHED: 'primary',
    RESOLVED: 'success',
    CLOSED: 'info'
  }
  return map[status] || ''
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    NEW: '新建',
    ASSIGNED: '已分配',
    APPROVED: '已批准',
    DISPATCHED: '进行中',
    RESOLVED: '已完成',
    CLOSED: '已关闭'
  }
  return map[status] || status
}

const getTaskTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    PLANNING: '规划',
    REVIEW: '评审',
    IMPLEMENTATION: '实施',
    TESTING: '测试',
    ROLLBACK: '回退'
  }
  return map[type] || type
}

const getTaskStatusType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'info',
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger',
    SKIPPED: ''
  }
  return map[status] || ''
}

const getTaskStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待处理',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    FAILED: '失败',
    SKIPPED: '跳过'
  }
  return map[status] || status
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

const handleAdd = () => {
  dialogTitle.value = '新增变更'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: ChangeRequest) => {
  dialogTitle.value = '编辑变更'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleTasks = (row: ChangeRequest) => {
  currentChange.value = row
  tasksDialogVisible.value = true
  loadTasks(row.id!)
}

const handleDelete = async (row: ChangeRequest) => {
  try {
    await ElMessageBox.confirm('确定要删除该变更吗？', '提示', { type: 'warning' })
    await request.delete(`/changes/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    // User cancelled
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  try {
    const res = form.id
      ? await request.put(`/changes/${form.id}`, form)
      : await request.post('/changes', form)

    ElMessage.success(form.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleAddTask = () => {
  taskDialogVisible.value = true
}

const handleSubmitTask = async () => {
  const valid = await taskFormRef.value?.validate()
  if (!valid) return

  try {
    const res = await request.post('/change-tasks', {
      ...taskForm,
      changeId: currentChange.value?.id
    })

    ElMessage.success('任务创建成功')
    taskDialogVisible.value = false
    loadTasks(currentChange.value?.id!)
    resetTaskForm()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleStartTask = async (row: ChangeTask) => {
  try {
    await request.post(`/change-tasks/${row.id}/start`)
    ElMessage.success('任务已开始')
    loadTasks(currentChange.value?.id!)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleCompleteTask = async (row: ChangeTask) => {
  try {
    await request.post(`/change-tasks/${row.id}/complete`)
    ElMessage.success('任务已完成')
    loadTasks(currentChange.value?.id!)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDeleteTask = async (row: ChangeTask) => {
  try {
    await ElMessageBox.confirm('确定要删除该任务吗？', '提示', { type: 'warning' })
    await request.delete(`/change-tasks/${row.id}`)
    ElMessage.success('删除成功')
    loadTasks(currentChange.value?.id!)
  } catch (error) {
    // User cancelled
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    changeType: 'NORMAL',
    changeCategory: 'OTHER',
    title: '',
    changeReason: '',
    riskAssessment: '',
    implementationPlan: '',
    rollbackPlan: '',
    testPlan: '',
    impact: '2',
    urgency: '2',
    priority: '2',
    organizationId: 1
  })
}

const resetTaskForm = () => {
  taskFormRef.value?.resetFields()
  Object.assign(taskForm, {
    name: '',
    taskType: 'IMPLEMENTATION',
    instructions: '',
    plannedStartDate: '',
    plannedEndDate: ''
  })
}
</script>

<style scoped lang="scss">
.change-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.tasks-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}
</style>