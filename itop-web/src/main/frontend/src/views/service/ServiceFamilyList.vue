<template>
  <div class="service-family-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>服务系列管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="name" label="名称" width="200" />
        <el-table-column prop="code" label="代码" width="150" />
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
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
      width="500px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="代码" prop="code">
          <el-input v-model="form.code" placeholder="请输入代码" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="图标名称 (如: Monitor)" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
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

interface ServiceFamily {
  id?: number
  name: string
  code: string
  icon?: string
  status: string
  sortOrder?: number
  description?: string
}

const loading = ref(false)
const tableData = ref<ServiceFamily[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const form = reactive<ServiceFamily>({
  name: '',
  code: '',
  icon: '',
  status: 'active',
  sortOrder: 0,
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入代码', trigger: 'blur' }]
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/service-families', {
      params: {
        page: pagination.page - 1,
        size: pagination.size
      }
    })
    if (res.data.success) {
      tableData.value = res.data.data.content
      pagination.total = res.data.data.totalElements
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增服务系列'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: ServiceFamily) => {
  dialogTitle.value = '编辑服务系列'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row: ServiceFamily) => {
  try {
    await ElMessageBox.confirm('确定要删除该服务系列吗？', '提示', {
      type: 'warning'
    })
    const res = await request.delete(`/api/service-families/${row.id}`)
    if (res.data.success) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    // User cancelled
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  try {
    const res = form.id
      ? await request.put(`/api/service-families/${form.id}`, form)
      : await request.post('/api/service-families', form)

    if (res.data.success) {
      ElMessage.success(form.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    name: '',
    code: '',
    icon: '',
    status: 'active',
    sortOrder: 0,
    description: ''
  })
}
</script>

<style scoped lang="scss">
.service-family-list {
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