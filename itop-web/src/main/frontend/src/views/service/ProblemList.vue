<template>
  <div class="problem-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>问题管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="问题类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable @change="loadData">
            <el-option label="软件" value="SOFTWARE" />
            <el-option label="硬件" value="HARDWARE" />
            <el-option label="网络" value="NETWORK" />
            <el-option label="流程" value="PROCESS" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="problemNumber" label="问题编号" width="150" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="problemType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.problemType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
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
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
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
      width="700px"
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
            <el-form-item label="问题类型" prop="problemType">
              <el-select v-model="form.problemType" placeholder="请选择类型">
                <el-option label="软件" value="SOFTWARE" />
                <el-option label="硬件" value="HARDWARE" />
                <el-option label="网络" value="NETWORK" />
                <el-option label="流程" value="PROCESS" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>

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

        <el-form-item label="根因分析" prop="rootCause">
          <el-input v-model="form.rootCause" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="临时方案" prop="workAround">
          <el-input v-model="form.workAround" type="textarea" :rows="2" />
        </el-form-item>

        <el-form-item label="影响分析" prop="impactAnalysis">
          <el-input v-model="form.impactAnalysis" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import request from '@/utils/request'

interface Problem {
  id?: number
  problemNumber?: string
  problemType: string
  title: string
  description?: string
  organizationId?: number
  impact?: string
  urgency?: string
  priority?: string
  status?: string
  rootCause?: string
  workAround?: string
  impactAnalysis?: string
}

const loading = ref(false)
const tableData = ref<Problem[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const searchForm = reactive({
  type: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const form = reactive<Problem>({
  problemType: 'SOFTWARE',
  title: '',
  description: '',
  impact: '2',
  urgency: '2',
  priority: '2',
  rootCause: '',
  workAround: '',
  impactAnalysis: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  problemType: [{ required: true, message: '请选择类型', trigger: 'change' }]
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
    if (searchForm.type) {
      params.type = searchForm.type
    }
    const res = await request.get('/problems', { params })
    tableData.value = res.content
    pagination.total = res.totalElements
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    SOFTWARE: '软件',
    HARDWARE: '硬件',
    NETWORK: '网络',
    PROCESS: '流程'
  }
  return map[type] || type
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
    RESOLVED: 'success',
    CLOSED: 'info'
  }
  return map[status] || ''
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    NEW: '新建',
    ASSIGNED: '已分配',
    RESOLVED: '已解决',
    CLOSED: '已关闭'
  }
  return map[status] || status
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

const handleAdd = () => {
  dialogTitle.value = '新增问题'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: Problem) => {
  dialogTitle.value = '编辑问题'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row: Problem) => {
  try {
    await ElMessageBox.confirm('确定要删除该问题吗？', '提示', { type: 'warning' })
    await request.delete(`/problems/${row.id}`)
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
      ? await request.put(`/problems/${form.id}`, form)
      : await request.post('/problems', form)

    ElMessage.success(form.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    problemType: 'SOFTWARE',
    title: '',
    description: '',
    impact: '2',
    urgency: '2',
    priority: '2',
    rootCause: '',
    workAround: '',
    impactAnalysis: ''
  })
}
</script>

<style scoped lang="scss">
.problem-list {
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
</style>