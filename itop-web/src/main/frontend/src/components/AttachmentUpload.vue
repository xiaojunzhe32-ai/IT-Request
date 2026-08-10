<template>
  <div class="attachment-upload">
    <el-upload
      ref="uploadRef"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :data="uploadData"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      :show-file-list="false"
      :multiple="false"
      :limit="1"
    >
      <el-button type="primary">
        <el-icon><Upload /></el-icon>
        上传附件
      </el-button>
    </el-upload>

    <el-table :data="attachments" v-loading="loading" stripe style="margin-top: 15px">
      <el-table-column prop="originalName" label="文件名" min-width="200" show-overflow-tooltip />
      <el-table-column prop="fileSize" label="大小" width="100">
        <template #default="{ row }">
          {{ formatSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column prop="contentType" label="类型" width="150" show-overflow-tooltip />
      <el-table-column prop="downloadCount" label="下载次数" width="100" />
      <el-table-column prop="createdAt" label="上传时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleDownload(row)">下载</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

interface Attachment {
  id: number
  entityType: string
  entityId: number
  fileName: string
  originalName: string
  filePath: string
  fileSize: number
  contentType: string
  description?: string
  uploaderId?: number
  uploaderName?: string
  downloadCount: number
  createdAt: string
}

const props = defineProps<{
  entityType: string
  entityId: number
}>()

const emit = defineEmits<{
  uploaded: [attachment: Attachment]
  deleted: [id: number]
}>()

const loading = ref(false)
const attachments = ref<Attachment[]>([])

const uploadUrl = computed(() => '/api/attachments/upload')
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))
const uploadData = computed(() => ({
  entityType: props.entityType,
  entityId: props.entityId
}))

watch(() => props.entityId, () => {
  if (props.entityId) {
    loadAttachments()
  }
})

onMounted(() => {
  if (props.entityId) {
    loadAttachments()
  }
})

const loadAttachments = async () => {
  loading.value = true
  try {
    const res = await request.get('/attachments/by-entity', {
      params: {
        entityType: props.entityType,
        entityId: props.entityId
      }
    })
    attachments.value = res || []
  } catch (error) {
    ElMessage.error('加载附件失败')
  } finally {
    loading.value = false
  }
}

const beforeUpload = (file: File) => {
  const maxSize = 50 * 1024 * 1024 // 50MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过50MB')
    return false
  }
  return true
}

const handleSuccess = (response: any) => {
  if (response.success) {
    ElMessage.success('上传成功')
    attachments.value.unshift(response.data)
    emit('uploaded', response.data)
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleError = () => {
  ElMessage.error('上传失败')
}

const handleDownload = (row: Attachment) => {
  window.open(`/api/attachments/download/${row.id}`, '_blank')
}

const handleDelete = async (row: Attachment) => {
  try {
    await ElMessageBox.confirm('确定要删除该附件吗？', '提示', { type: 'warning' })
    await request.delete(`/attachments/${row.id}`)
    ElMessage.success('删除成功')
    attachments.value = attachments.value.filter(a => a.id !== row.id)
    emit('deleted', row.id)
  } catch (error) {
    // User cancelled
  }
}

const formatSize = (size: number) => {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / (1024 * 1024)).toFixed(2) + ' MB'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style scoped lang="scss">
.attachment-upload {
  padding: 10px 0;
}
</style>