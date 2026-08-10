<template>
  <div class="service-catalog">
    <el-row :gutter="20">
      <!-- Left: Service Family Tree -->
      <el-col :span="6">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>服务目录</span>
              <el-button type="primary" link @click="handleAddFamily">
                <el-icon><Plus /></el-icon>
              </el-button>
            </div>
          </template>
          <el-tree
            :data="treeData"
            :props="treeProps"
            node-key="id"
            :expand-on-click-node="false"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="tree-node">
                <el-icon v-if="data.icon"><component :is="data.icon" /></el-icon>
                <span>{{ node.label }}</span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <!-- Right: Service List -->
      <el-col :span="18">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>{{ currentTitle }}</span>
              <div>
                <el-button
                  v-if="selectedNode && selectedNode.type === 'family'"
                  type="success"
                  @click="handleAddSubfamily"
                >
                  新增子类
                </el-button>
                <el-button
                  v-if="selectedNode && selectedNode.type === 'subfamily'"
                  type="primary"
                  @click="handleAddService"
                >
                  新增服务
                </el-button>
              </div>
            </div>
          </template>

          <!-- Subfamily View -->
          <el-table
            v-if="selectedNode?.type === 'family'"
            :data="subfamilyData"
            v-loading="loading"
            stripe
          >
            <el-table-column prop="name" label="子类名称" width="200" />
            <el-table-column prop="code" label="代码" width="150" />
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
                <el-button type="primary" link @click="handleEditSubfamily(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteSubfamily(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- Service View -->
          <el-table
            v-if="selectedNode?.type === 'subfamily'"
            :data="serviceData"
            v-loading="loading"
            stripe
          >
            <el-table-column prop="name" label="服务名称" width="200" />
            <el-table-column prop="code" label="代码" width="150" />
            <el-table-column prop="serviceType" label="类型" width="120">
              <template #default="{ row }">
                <el-tag>{{ row.serviceType === 'USER_REQUEST' ? '用户请求' : '事件' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="slaName" label="SLA" width="150" />
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
                <el-button type="primary" link @click="handleEditService(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteService(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- Empty State -->
          <el-empty v-if="!selectedNode" description="请选择左侧服务目录" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Add/Edit Service Dialog -->
    <el-dialog
      v-model="serviceDialogVisible"
      :title="serviceDialogTitle"
      width="600px"
      @close="resetServiceForm"
    >
      <el-form :model="serviceForm" :rules="serviceRules" ref="serviceFormRef" label-width="100px">
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="serviceForm.name" placeholder="请输入服务名称" />
        </el-form-item>
        <el-form-item label="代码" prop="code">
          <el-input v-model="serviceForm.code" placeholder="请输入代码" />
        </el-form-item>
        <el-form-item label="服务类型" prop="serviceType">
          <el-select v-model="serviceForm.serviceType" placeholder="请选择">
            <el-option label="用户请求" value="USER_REQUEST" />
            <el-option label="事件" value="INCIDENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="SLA" prop="slaId">
          <el-select v-model="serviceForm.slaId" placeholder="请选择SLA" clearable>
            <el-option
              v-for="sla in slaOptions"
              :key="sla.id"
              :label="sla.name"
              :value="sla.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="serviceForm.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="serviceForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="serviceForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="serviceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitService">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import request from '@/utils/request'

interface TreeNode {
  id: number
  label: string
  type: 'family' | 'subfamily'
  icon?: string
  children?: TreeNode[]
}

interface ServiceFamily {
  id: number
  name: string
  code: string
  icon?: string
  status: string
  sortOrder?: number
  description?: string
}

interface ServiceSubfamily {
  id: number
  name: string
  code: string
  familyId: number
  status: string
  sortOrder?: number
  description?: string
}

interface Service {
  id?: number
  name: string
  code: string
  subfamilyId: number
  serviceType: string
  slaId?: number
  status: string
  sortOrder?: number
  description?: string
}

const loading = ref(false)
const treeData = ref<TreeNode[]>([])
const subfamilyData = ref<ServiceSubfamily[]>([])
const serviceData = ref<Service[]>([])
const slaOptions = ref<any[]>([])
const selectedNode = ref<TreeNode | null>(null)
const serviceDialogVisible = ref(false)
const serviceDialogTitle = ref('')
const serviceFormRef = ref<FormInstance>()

const serviceForm = reactive<Service>({
  name: '',
  code: '',
  subfamilyId: 0,
  serviceType: 'USER_REQUEST',
  slaId: undefined,
  status: 'active',
  sortOrder: 0,
  description: ''
})

const treeProps = {
  children: 'children',
  label: 'label'
}

const currentTitle = computed(() => {
  if (!selectedNode.value) return '服务列表'
  return selectedNode.value.type === 'family'
    ? '服务子类列表'
    : '服务项列表'
})

const serviceRules = {
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入代码', trigger: 'blur' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }]
}

onMounted(() => {
  loadTreeData()
  loadSLAOptions()
})

const loadTreeData = async () => {
  try {
    const [familyRes, subfamilyRes] = await Promise.all([
      request.get('/service-families'),
      request.get('/service-subfamilies')
    ])

    const families = familyRes.content
    const subfamilies = subfamilyRes.content

    treeData.value = families.map((f: ServiceFamily) => ({
      id: f.id,
      label: f.name,
      type: 'family',
      icon: f.icon,
      children: subfamilies
        .filter((s: ServiceSubfamily) => s.familyId === f.id)
        .map((s: ServiceSubfamily) => ({
          id: s.id,
          label: s.name,
          type: 'subfamily'
        }))
    }))
  } catch (error) {
    ElMessage.error('加载服务目录失败')
  }
}

const loadSLAOptions = async () => {
  try {
    const res = await request.get('/slas')
    slaOptions.value = res.content
  } catch (error) {
    console.error('加载SLA失败', error)
  }
}

const handleNodeClick = async (node: TreeNode) => {
  selectedNode.value = node
  loading.value = true

  try {
    if (node.type === 'family') {
      const res = await request.get('/service-subfamilies', {
        params: { familyId: node.id }
      })
      subfamilyData.value = res.content
    } else {
      const res = await request.get('/services', {
        params: { subfamilyId: node.id }
      })
      serviceData.value = res.content
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAddFamily = () => {
  // Navigate to service family management
  ElMessage.info('请到服务系列管理页面新增')
}

const handleAddSubfamily = () => {
  ElMessage.info('请在服务子类管理页面新增')
}

const handleAddService = () => {
  if (!selectedNode.value) return
  serviceDialogTitle.value = '新增服务'
  resetServiceForm()
  serviceForm.subfamilyId = selectedNode.value.id
  serviceDialogVisible.value = true
}

const handleEditService = (row: Service) => {
  serviceDialogTitle.value = '编辑服务'
  Object.assign(serviceForm, row)
  serviceDialogVisible.value = true
}

const handleDeleteService = async (row: Service) => {
  try {
    await ElMessageBox.confirm('确定要删除该服务吗？', '提示', { type: 'warning' })
    await request.delete(`/services/${row.id}`)
    ElMessage.success('删除成功')
    handleNodeClick(selectedNode.value!)
  } catch (error) {
    // User cancelled
  }
}

const handleSubmitService = async () => {
  const valid = await serviceFormRef.value?.validate()
  if (!valid) return

  try {
    const res = serviceForm.id
      ? await request.put(`/services/${serviceForm.id}`, serviceForm)
      : await request.post('/services', serviceForm)

    ElMessage.success(serviceForm.id ? '更新成功' : '创建成功')
    serviceDialogVisible.value = false
    handleNodeClick(selectedNode.value!)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const resetServiceForm = () => {
  serviceFormRef.value?.resetFields()
  Object.assign(serviceForm, {
    name: '',
    code: '',
    subfamilyId: 0,
    serviceType: 'USER_REQUEST',
    slaId: undefined,
    status: 'active',
    sortOrder: 0,
    description: ''
  })
}
</script>

<style scoped lang="scss">
.service-catalog {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 5px;
}
</style>