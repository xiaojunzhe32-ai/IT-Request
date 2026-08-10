<template>
  <div class="faq-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>FAQ 常见问题</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="搜索">
          <el-input v-model="searchForm.search" placeholder="搜索问题..." clearable @keyup.enter="loadData" style="width: 300px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="全部" clearable @change="loadData">
            <el-option label="账户管理" value="账户管理" />
            <el-option label="网络访问" value="网络访问" />
            <el-option label="支持服务" value="支持服务" />
            <el-option label="软件安装" value="软件安装" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="faq-expand">
              <h4>答案：</h4>
              <p>{{ row.answer }}</p>
              <div class="faq-meta">
                <span>浏览：{{ row.viewCount }}</span>
                <span>有帮助：{{ row.helpfulCount }}</span>
                <span>无帮助：{{ row.notHelpfulCount }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="question" label="问题" min-width="300" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column prop="helpfulCount" label="有帮助" width="80" />
        <el-table-column prop="isPublished" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isPublished ? 'success' : 'info'">
              {{ row.isPublished ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.isPublished" type="success" link @click="handleTogglePublish(row)">取消发布</el-button>
            <el-button v-else type="warning" link @click="handleTogglePublish(row)">发布</el-button>
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
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="问题" prop="question">
          <el-input v-model="form.question" placeholder="请输入问题" />
        </el-form-item>

        <el-form-item label="答案" prop="answer">
          <el-input v-model="form.answer" type="textarea" :rows="5" placeholder="请输入答案" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类" allow-create filterable>
                <el-option label="账户管理" value="账户管理" />
                <el-option label="网络访问" value="网络访问" />
                <el-option label="支持服务" value="支持服务" />
                <el-option label="软件安装" value="软件安装" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="关键词" prop="keywords">
          <el-input v-model="form.keywords" placeholder="多个关键词用逗号分隔" />
        </el-form-item>

        <el-form-item label="立即发布">
          <el-switch v-model="form.isPublished" />
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

interface FAQ {
  id?: number
  question: string
  answer: string
  category?: string
  keywords?: string
  viewCount?: number
  helpfulCount?: number
  notHelpfulCount?: number
  sortOrder?: number
  isPublished?: boolean
}

const loading = ref(false)
const tableData = ref<FAQ[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const searchForm = reactive({
  search: '',
  category: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const form = reactive<FAQ>({
  question: '',
  answer: '',
  category: '',
  keywords: '',
  sortOrder: 0,
  isPublished: true
})

const rules = {
  question: [{ required: true, message: '请输入问题', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入答案', trigger: 'blur' }]
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
    if (searchForm.category) {
      params.category = searchForm.category
    }
    const res = await request.get('/faqs', { params })
    tableData.value = res.content
    pagination.total = res.totalElements
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增 FAQ'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: FAQ) => {
  dialogTitle.value = '编辑 FAQ'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleTogglePublish = async (row: FAQ) => {
  try {
    const newStatus = !row.isPublished
    await request.put(`/faqs/${row.id}`, {
      ...row,
      isPublished: newStatus
    })
    ElMessage.success(newStatus ? '已发布' : '已取消发布')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row: FAQ) => {
  try {
    await ElMessageBox.confirm('确定要删除该 FAQ 吗？', '提示', { type: 'warning' })
    await request.delete(`/faqs/${row.id}`)
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
      ? await request.put(`/faqs/${form.id}`, form)
      : await request.post('/faqs', form)

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
    question: '',
    answer: '',
    category: '',
    keywords: '',
    sortOrder: 0,
    isPublished: true
  })
}
</script>

<style scoped lang="scss">
.faq-list {
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

.faq-expand {
  padding: 20px;

  h4 {
    margin-bottom: 10px;
  }

  p {
    line-height: 1.8;
    white-space: pre-wrap;
  }

  .faq-meta {
    margin-top: 15px;
    color: #909399;
    font-size: 13px;

    span {
      margin-right: 20px;
    }
  }
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>