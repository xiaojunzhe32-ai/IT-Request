<template>
  <div class="server-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>服务器列表</span>
          <el-button type="primary" @click="showDialog()">
            <el-icon><Plus /></el-icon>
            新建服务器
          </el-button>
        </div>
      </template>

      <!-- Search -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="名称">
          <el-input v-model="searchForm.name" placeholder="搜索服务器名称" clearable />
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input v-model="searchForm.ip" placeholder="IP地址" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <!-- Table -->
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="名称" width="150" />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="osFamily" label="操作系统" width="120" />
        <el-table-column prop="cpu" label="CPU" width="100" />
        <el-table-column prop="ram" label="内存" width="80" />
        <el-table-column prop="disk" label="磁盘" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="serverType" label="类型" width="100" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="showDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
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

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input v-model="formData.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="IP地址">
              <el-input v-model="formData.ipAddress" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="管理IP">
              <el-input v-model="formData.managementIp" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="操作系统">
              <el-input v-model="formData.osFamily" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="系统版本">
              <el-input v-model="formData.osVersion" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="CPU">
              <el-input v-model="formData.cpu" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="内存">
              <el-input v-model="formData.ram" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="磁盘">
              <el-input v-model="formData.disk" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="formData.brandName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号">
              <el-input v-model="formData.modelName" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="序列号">
              <el-input v-model="formData.serialNumber" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产编号">
              <el-input v-model="formData.assetNumber" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="服务器类型">
              <el-select v-model="formData.serverType" style="width: 100%">
                <el-option label="物理服务器" value="PHYSICAL" />
                <el-option label="虚拟机" value="VIRTUAL" />
                <el-option label="宿主机" value="HYPERVISOR" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务重要性">
              <el-select v-model="formData.businessCriticity" style="width: 100%">
                <el-option label="低" value="low" />
                <el-option label="中" value="medium" />
                <el-option label="高" value="high" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

interface Server {
  id?: number
  name: string
  organizationId: number
  status?: string
  description?: string
  assetNumber?: string
  brandName?: string
  modelName?: string
  serialNumber?: string
  cpu?: string
  ram?: string
  disk?: string
  osFamily?: string
  osVersion?: string
  ipAddress?: string
  macAddress?: string
  managementIp?: string
  isVirtual?: boolean
  serverType?: string
  businessCriticity?: string
}

interface Organization {
  id: number
  name: string
}

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<Server[]>([])
const organizations = ref<Organization[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新建服务器')
const formRef = ref<FormInstance>()

const searchForm = reactive({
  name: '',
  ip: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const formData = reactive<Server>({
  name: '',
  organizationId: 1,
  status: 'active',
  description: '',
  assetNumber: '',
  brandName: '',
  modelName: '',
  serialNumber: '',
  cpu: '',
  ram: '',
  disk: '',
  osFamily: '',
  osVersion: '',
  ipAddress: '',
  macAddress: '',
  managementIp: '',
  isVirtual: false,
  serverType: 'PHYSICAL',
  businessCriticity: 'medium'
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入服务器名称', trigger: 'blur' }],
  organizationId: [{ required: true, message: '请选择组织', trigger: 'change' }]
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

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page - 1,
      size: pagination.size
    }

    if (searchForm.name) {
      params.name = searchForm.name
    }
    if (searchForm.ip) {
      params.ip = searchForm.ip
    }

    const data = await request.get('/servers', { params }) as any
    tableData.value = data.content || []
    pagination.total = data.totalElements || 0
  } catch (error) {
    console.error('Failed to load servers:', error)
    ElMessage.error('加载服务器列表失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const showDialog = (row?: Server) => {
  if (row) {
    dialogTitle.value = '编辑服务器'
    Object.assign(formData, {
      id: row.id,
      name: row.name,
      organizationId: row.organizationId,
      status: row.status || 'active',
      description: row.description || '',
      assetNumber: row.assetNumber || '',
      brandName: row.brandName || '',
      modelName: row.modelName || '',
      serialNumber: row.serialNumber || '',
      cpu: row.cpu || '',
      ram: row.ram || '',
      disk: row.disk || '',
      osFamily: row.osFamily || '',
      osVersion: row.osVersion || '',
      ipAddress: row.ipAddress || '',
      macAddress: row.macAddress || '',
      managementIp: row.managementIp || '',
      isVirtual: row.isVirtual || false,
      serverType: row.serverType || 'PHYSICAL',
      businessCriticity: row.businessCriticity || 'medium'
    })
  } else {
    dialogTitle.value = '新建服务器'
    Object.assign(formData, {
      id: undefined,
      name: '',
      organizationId: organizations.value[0]?.id || 1,
      status: 'active',
      description: '',
      assetNumber: '',
      brandName: '',
      modelName: '',
      serialNumber: '',
      cpu: '',
      ram: '',
      disk: '',
      osFamily: '',
      osVersion: '',
      ipAddress: '',
      macAddress: '',
      managementIp: '',
      isVirtual: false,
      serverType: 'PHYSICAL',
      businessCriticity: 'medium'
    })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (formData.id) {
          await request.put(`/servers/${formData.id}`, formData)
          ElMessage.success('更新成功')
        } else {
          await request.post('/servers', formData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('Failed to save server:', error)
        ElMessage.error('操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该服务器吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request.delete(`/servers/${id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Failed to delete server:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadOrganizations()
  loadData()
})
</script>

<style scoped lang="scss">
.server-list {
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