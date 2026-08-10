<template>
  <div class="ci-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>配置项列表</span>
          <el-button type="primary" @click="showDetail(null)">
            <el-icon><Plus /></el-icon>
            新建配置项
          </el-button>
        </div>
      </template>

      <!-- Search -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="全部类型" clearable style="width: 150px">
            <el-option v-for="type in ciTypes" :key="type" :label="type" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="活跃" value="active" />
            <el-option label="停用" value="inactive" />
            <el-option label="废弃" value="obsolete" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <!-- Table -->
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="名称" width="200">
          <template #default="scope">
            <el-link type="primary" @click="showDetail(scope.row)">{{ scope.row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="finalClass" label="类型" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assetNumber" label="资产编号" width="120" />
        <el-table-column prop="businessCriticity" label="重要性" width="80">
          <template #default="scope">
            <el-tag :type="getCriticityType(scope.row.businessCriticity)" size="small">
              {{ scope.row.businessCriticity }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="organizationId" label="组织ID" width="80" />
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
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

    <!-- Detail Dialog -->
    <el-dialog v-model="dialogVisible" title="配置项详情" width="600px">
      <el-descriptions :column="2" border v-if="currentCI">
        <el-descriptions-item label="名称">{{ currentCI.name }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentCI.finalClass }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentCI.status)">{{ currentCI.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="资产编号">{{ currentCI.assetNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务重要性">
          <el-tag :type="getCriticityType(currentCI.businessCriticity)" size="small">
            {{ currentCI.businessCriticity }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="冗余">{{ currentCI.redundancy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="组织ID">{{ currentCI.organizationId }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentCI.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentCI.description || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToEdit" v-if="currentCI">编辑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { formatDateTime } from '@/utils/format'

interface CI {
  id: number
  name: string
  finalClass: string
  status: string
  assetNumber?: string
  businessCriticity?: string
  redundancy?: string
  organizationId: number
  description?: string
  createdAt: string
}

const router = useRouter()
const loading = ref(false)
const tableData = ref<CI[]>([])
const ciTypes = ref<string[]>([])
const dialogVisible = ref(false)
const currentCI = ref<CI | null>(null)

const searchForm = reactive({
  type: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    active: 'success',
    inactive: 'info',
    obsolete: 'danger'
  }
  return map[status] || 'info'
}

const getCriticityType = (criticity: string) => {
  const map: Record<string, string> = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  }
  return map[criticity] || 'info'
}


const loadCITypes = async () => {
  try {
    const data = await request.get('/cis/types') as string[]
    ciTypes.value = data || []
  } catch (error) {
    console.error('Failed to load CI types:', error)
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

    const data = await request.get('/cis', { params }) as any
    tableData.value = data.content || []
    pagination.total = data.totalElements || 0
  } catch (error) {
    console.error('Failed to load CIs:', error)
    ElMessage.error('加载配置项列表失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const showDetail = (ci: CI | null) => {
  if (ci) {
    currentCI.value = ci
    dialogVisible.value = true
  } else {
    ElMessage.info('请从具体类型（服务器、应用等）创建配置项')
  }
}

const goToEdit = () => {
  if (currentCI.value) {
    const finalClass = currentCI.value.finalClass.toLowerCase()
    if (finalClass === 'server') {
      router.push(`/cmdb/servers?id=${currentCI.value.id}`)
    } else {
      ElMessage.info('此类型配置项暂不支持编辑')
    }
    dialogVisible.value = false
  }
}

onMounted(() => {
  loadCITypes()
  loadData()
})
</script>

<style scoped lang="scss">
.ci-list {
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