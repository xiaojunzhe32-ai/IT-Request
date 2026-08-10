<template>
  <div class="known-error-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>已知错误库</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="搜索">
          <el-input v-model="searchForm.search" placeholder="搜索症状、解决方案..." clearable @keyup.enter="loadData" style="width: 300px" />
        </el-form-item>
        <el-form-item label="错误类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable @change="loadData">
            <el-option label="软件" value="SOFTWARE" />
            <el-option label="硬件" value="HARDWARE" />
            <el-option label="网络" value="NETWORK" />
            <el-option label="配置" value="CONFIGURATION" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="errorCode" label="错误代码" width="150" />
        <el-table-column prop="symptoms" label="症状" min-width="200" show-overflow-tooltip />
        <el-table-column prop="errorType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.errorType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="severity" label="严重程度" width="100">
          <template #default="{ row }">
            <el-tag :type="getSeverityType(row.severity)">
              {{ getSeverityLabel(row.severity) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="occurrenceCount" label="发生次数" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
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
            <el-form-item label="错误代码" prop="errorCode">
              <el-input v-model="form.errorCode" placeholder="请输入错误代码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="错误类型" prop="errorType">
              <el-select v-model="form.errorType" placeholder="请选择类型">
                <el-option label="软件" value="SOFTWARE" />
                <el-option label="硬件" value="HARDWARE" />
                <el-option label="网络" value="NETWORK" />
                <el-option label="配置" value="CONFIGURATION" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="症状描述" prop="symptoms">
          <el-input v-model="form.symptoms" type="textarea" :rows="3" placeholder="详细描述问题症状" />
        </el-form-item>

        <el-form-item label="根因" prop="cause">
          <el-input v-model="form.cause" type="textarea" :rows="2" />
        </el-form-item>

        <el-form-item label="临时解决方案" prop="workaround">
          <el-input v-model="form.workaround" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="最终解决方案" prop="solution">
          <el-input v-model="form.solution" type="textarea" :rows="3" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="严重程度" prop="severity">
              <el-select v-model="form.severity" placeholder="请选择">
                <el-option label="低" value="LOW" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="高" value="HIGH" />
                <el-option label="紧急" value="CRITICAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="应用到所有">
              <el-switch v-model="form.applyToAll" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog
      v-model="viewDialogVisible"
      title="已知错误详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="错误代码">{{ viewData.errorCode }}</el-descriptions-item>
        <el-descriptions-item label="错误类型">{{ getTypeLabel(viewData.errorType) }}</el-descriptions-item>
        <el-descriptions-item label="严重程度">
          <el-tag :type="getSeverityType(viewData.severity)">{{ getSeverityLabel(viewData.severity) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发生次数">{{ viewData.occurrenceCount }}</el-descriptions-item>
        <el-descriptions-item label="症状" :span="2">{{ viewData.symptoms }}</el-descriptions-item>
        <el-descriptions-item label="根因" :span="2">{{ viewData.cause || '-' }}</el-descriptions-item>
        <el-descriptions-item label="临时方案" :span="2">{{ viewData.workaround || '-' }}</el-descriptions-item>
        <el-descriptions-item label="解决方案" :span="2">{{ viewData.solution || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import request from '@/utils/request'

interface KnownError {
  id?: number
  errorCode: string
  errorType: string
  symptoms: string
  cause?: string
  workaround?: string
  solution?: string
  severity?: string
  applyToAll?: boolean
  occurrenceCount?: number
}

const loading = ref(false)
const tableData = ref<KnownError[]>([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const viewData = ref<KnownError>({} as KnownError)

const searchForm = reactive({
  search: '',
  type: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const form = reactive<KnownError>({
  errorCode: '',
  errorType: 'SOFTWARE',
  symptoms: '',
  cause: '',
  workaround: '',
  solution: '',
  severity: 'MEDIUM',
  applyToAll: false
})

const rules = {
  errorCode: [{ required: true, message: '请输入错误代码', trigger: 'blur' }],
  symptoms: [{ required: true, message: '请输入症状描述', trigger: 'blur' }]
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
    if (searchForm.search) {
      params.search = searchForm.search
    }
    if (searchForm.type) {
      params.type = searchForm.type
    }
    const res = await request.get('/known-errors', { params })
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
    CONFIGURATION: '配置'
  }
  return map[type] || type
}

const getSeverityType = (severity: string) => {
  const map: Record<string, string> = {
    CRITICAL: 'danger',
    HIGH: 'warning',
    MEDIUM: '',
    LOW: 'info'
  }
  return map[severity] || ''
}

const getSeverityLabel = (severity: string) => {
  const map: Record<string, string> = {
    CRITICAL: '紧急',
    HIGH: '高',
    MEDIUM: '中',
    LOW: '低'
  }
  return map[severity] || severity
}

const handleAdd = () => {
  dialogTitle.value = '新增已知错误'
  resetForm()
  dialogVisible.value = true
}

const handleView = (row: KnownError) => {
  viewData.value = row
  viewDialogVisible.value = true
}

const handleEdit = (row: KnownError) => {
  dialogTitle.value = '编辑已知错误'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row: KnownError) => {
  try {
    await ElMessageBox.confirm('确定要删除该已知错误吗？', '提示', { type: 'warning' })
    await request.delete(`/known-errors/${row.id}`)
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
      ? await request.put(`/known-errors/${form.id}`, form)
      : await request.post('/known-errors', form)

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
    errorCode: '',
    errorType: 'SOFTWARE',
    symptoms: '',
    cause: '',
    workaround: '',
    solution: '',
    severity: 'MEDIUM',
    applyToAll: false
  })
}
</script>

<style scoped lang="scss">
.known-error-list {
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