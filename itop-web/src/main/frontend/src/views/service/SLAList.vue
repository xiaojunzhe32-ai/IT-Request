<template>
  <div class="sla-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>SLA 管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="name" label="名称" width="200" />
        <el-table-column prop="code" label="代码" width="150" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)">
              {{ getPriorityLabel(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ttoHours" label="响应时间(小时)" width="120" />
        <el-table-column prop="ttrHours" label="解决时间(小时)" width="120" />
        <el-table-column prop="isDefault" label="默认" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success">是</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
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
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="代码" prop="code">
          <el-input v-model="form.code" placeholder="请输入代码" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" placeholder="请选择优先级">
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
            <el-option label="紧急" value="critical" />
          </el-select>
        </el-form-item>
        <el-form-item label="响应时间" prop="ttoHours">
          <el-input-number v-model="form.ttoHours" :min="1" :max="72" />
          <span style="margin-left: 10px;">小时</span>
        </el-form-item>
        <el-form-item label="解决时间" prop="ttrHours">
          <el-input-number v-model="form.ttrHours" :min="1" :max="168" />
          <span style="margin-left: 10px;">小时</span>
        </el-form-item>
        <el-form-item label="设为默认" prop="isDefault">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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

interface SLA {
  id?: number
  name: string
  code: string
  priority: string
  ttoHours?: number
  ttrHours?: number
  isDefault?: boolean
  status: string
  description?: string
}

const loading = ref(false)
const tableData = ref<SLA[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const form = reactive<SLA>({
  name: '',
  code: '',
  priority: 'medium',
  ttoHours: 4,
  ttrHours: 8,
  isDefault: false,
  status: 'active',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入代码', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/slas', {
      params: {
        page: pagination.page - 1,
        size: pagination.size
      }
    })
    tableData.value = res.content
    pagination.total = res.totalElements
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const getPriorityType = (priority: string) => {
  const map: Record<string, string> = {
    critical: 'danger',
    high: 'warning',
    medium: '',
    low: 'info'
  }
  return map[priority] || ''
}

const getPriorityLabel = (priority: string) => {
  const map: Record<string, string> = {
    critical: '紧急',
    high: '高',
    medium: '中',
    low: '低'
  }
  return map[priority] || priority
}

const handleAdd = () => {
  dialogTitle.value = '新增 SLA'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: SLA) => {
  dialogTitle.value = '编辑 SLA'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row: SLA) => {
  try {
    await ElMessageBox.confirm('确定要删除该 SLA 吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/slas/${row.id}`)
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
      ? await request.put(`/slas/${form.id}`, form)
      : await request.post('/slas', form)

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
    name: '',
    code: '',
    priority: 'medium',
    ttoHours: 4,
    ttrHours: 8,
    isDefault: false,
    status: 'active',
    description: ''
  })
}
</script>

<style scoped lang="scss">
.sla-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>