<template>
  <div class="portal-page">
    <PageHeader
      eyebrow="User Portal"
      title="New Request"
      description="Create a request with enough context for IT to route and handle it without a service desk step."
    />

    <div class="request-create-layout">
      <el-card class="surface-card request-form-card" shadow="never">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <section class="form-section">
            <div class="section-heading">
              <strong>Request Basics</strong>
              <span>Keep it short, then add enough context for IT routing.</span>
            </div>

            <div class="form-grid">
              <el-form-item label="Request Title" prop="title">
                <el-input v-model="form.title" placeholder="Briefly describe the issue or request" />
              </el-form-item>

              <el-form-item label="Request Type" prop="type">
                <el-select v-model="form.type" placeholder="Select type">
                  <el-option v-for="type in requestTypes" :key="type" :label="type" :value="type" />
                </el-select>
              </el-form-item>

              <el-form-item label="Affected Service / System" prop="affectedService">
                <el-select v-model="form.affectedService" filterable placeholder="Select affected service">
                  <el-option
                    v-for="service in affectedServiceOptions"
                    :key="service"
                    :label="service"
                    :value="service"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="Organization" prop="organizationId">
                <el-select
                  v-model="form.organizationId"
                  :loading="orgLoading"
                  filterable
                  placeholder="Select organization"
                >
                  <el-option
                    v-for="org in organizationOptions"
                    :key="org.id"
                    :label="org.name"
                    :value="org.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="Priority" prop="priority">
                <el-radio-group v-model="form.priority" class="priority-radio">
                  <el-radio-button
                    v-for="priority in priorityOptions"
                    :key="priority"
                    :label="priority"
                    :value="priority"
                  />
                </el-radio-group>
              </el-form-item>

              <el-form-item label="Occurrence Time" prop="occurrenceTime">
                <el-date-picker
                  v-model="form.occurrenceTime"
                  type="datetime"
                  format="YYYY-MM-DD HH:mm"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  placeholder="When did it happen?"
                />
              </el-form-item>

              <el-form-item label="Requested Resolution Time" prop="requestedResolutionTime">
                <el-date-picker
                  v-model="form.requestedResolutionTime"
                  type="datetime"
                  format="YYYY-MM-DD HH:mm"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  placeholder="When do you hope it is resolved?"
                />
              </el-form-item>
            </div>
          </section>

          <section class="form-section">
            <div class="section-heading">
              <strong>Details & Evidence</strong>
              <span>Use the editor for context, screenshots and expected results.</span>
            </div>

            <el-form-item label="Description" prop="description">
              <div class="rich-editor-shell">
                <div class="editor-toolbar" aria-label="Description toolbar">
                  <el-button size="small" @click="applyFormat('bold')">B</el-button>
                  <el-button size="small" @click="applyFormat('insertUnorderedList')">Bullets</el-button>
                  <el-button size="small" @click="applyFormat('insertOrderedList')">Numbered</el-button>
                  <el-button size="small" @click="insertCodeBlock">Code</el-button>
                </div>

                <div
                  ref="editorRef"
                  class="rich-editor"
                  contenteditable="true"
                  data-placeholder="What happened, who is affected, what did you expect, and what actually happened?"
                  @input="syncDescription"
                  @blur="syncDescription"
                  @paste="handleEditorPaste"
                />

                <div class="editor-help">
                  <span>Paste screenshots directly into the editor.</span>
                  <span v-if="inlineImageCount">
                    {{ inlineImageCount }} inline image{{ inlineImageCount > 1 ? 's' : '' }}
                  </span>
                </div>
              </div>
            </el-form-item>

            <div class="attachment-dropzone" @dragover.prevent @drop.prevent="handleAttachmentDrop">
              <input
                ref="fileInputRef"
                class="file-input"
                type="file"
                multiple
                @change="handleFileInput"
              >
              <el-icon><UploadFilled /></el-icon>
              <div>
                <strong>Attachments</strong>
                <span>Drop logs, documents or screenshots here, or choose files.</span>
              </div>
              <el-button @click="fileInputRef?.click()">Choose Files</el-button>
            </div>

            <div v-if="form.attachments.length" class="attachment-list">
              <article v-for="attachment in form.attachments" :key="attachment.id" class="attachment-item">
                <el-icon><Document /></el-icon>
                <div>
                  <strong>{{ attachment.originalName }}</strong>
                  <span>{{ attachment.contentType || 'Unknown type' }} - {{ formatFileSize(attachment.fileSize) }}</span>
                </div>
                <el-button text @click="removeAttachment(attachment.id)">Remove</el-button>
              </article>
            </div>
          </section>

          <div class="form-footer">
            <el-button @click="router.push('/portal')">Cancel</el-button>
            <el-button type="primary" :loading="submitting" @click="submitRequest">
              <el-icon><Plus /></el-icon>
              Submit Request
            </el-button>
          </div>
        </el-form>
      </el-card>

      <aside class="request-summary-panel">
        <el-card class="surface-card" shadow="never">
          <template #header>
            <div class="card-title-row">
              <div>
                <strong>Request Summary</strong>
                <span>Lightweight routing preview</span>
              </div>
            </div>
          </template>

          <div class="summary-list">
            <div>
              <span>Type</span>
              <strong>{{ form.type }}</strong>
            </div>
            <div>
              <span>Service</span>
              <strong>{{ form.affectedService || 'Not selected' }}</strong>
            </div>
            <div>
              <span>Suggested Team</span>
              <strong>{{ routingPreview.team }}</strong>
            </div>
            <div>
              <span>Evidence</span>
              <strong>{{ evidenceSummary }}</strong>
            </div>
          </div>

          <div class="completeness-box">
            <strong>Before submitting</strong>
            <span>{{ completenessText }}</span>
          </div>
        </el-card>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Document, Plus, UploadFilled } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { organizationApi } from '@/api/cmdb'
import { requestApi } from '@/api/requests'
import { attachmentApi } from '@/api/attachments'
import { priorityOptions, requestTypes, sanitizeRequestHtml } from '@/data/requestOptions'
import type { Organization } from '@/types/cmdb'
import type { RequestAttachment, RequestPriority } from '@/types/requests'

const router = useRouter()
const formRef = ref<FormInstance>()
const editorRef = ref<HTMLElement>()
const fileInputRef = ref<HTMLInputElement>()
const inlineImageCount = ref(0)
const orgLoading = ref(false)
const submitting = ref(false)
const organizationOptions = ref<Organization[]>([])
const affectedServiceOptions = [
  'ERP',
  'Email',
  'Network',
  'CRM',
  'VPN',
  'Printer',
  'Finance System',
  'HR System',
  'Other'
]

interface RequestDraftAttachment extends RequestAttachment {
  file?: File
}

const form = reactive({
  title: '',
  type: 'Application Issue',
  affectedService: '',
  priority: 'Medium' as RequestPriority,
  organizationId: undefined as number | undefined,
  occurrenceTime: '',
  requestedResolutionTime: '',
  description: '',
  descriptionHtml: '',
  attachments: [] as RequestDraftAttachment[]
})

const rules: FormRules = {
  title: [{ required: true, message: 'Please enter request title', trigger: 'blur' }],
  type: [{ required: true, message: 'Please select request type', trigger: 'change' }],
  affectedService: [{ required: true, message: 'Please select affected service', trigger: 'change' }],
  organizationId: [{ required: true, message: 'Please select organization', trigger: 'change' }],
  description: [{ required: true, message: 'Please enter description', trigger: 'blur' }]
}

const routingPreview = computed(() => {
  if (form.type === 'Network Issue' || ['Network', 'VPN'].includes(form.affectedService)) {
    return { team: 'Network Operations' }
  }
  if (form.type === 'Hardware Issue' || form.affectedService === 'Printer') {
    return { team: 'System Operations' }
  }
  if (form.type === 'Account Access') {
    return { team: 'System Operations' }
  }
  return { team: 'Application Operations' }
})

const evidenceSummary = computed(() => {
  const items = []
  if (inlineImageCount.value) items.push(`${inlineImageCount.value} inline image${inlineImageCount.value > 1 ? 's' : ''}`)
  if (form.attachments.length) items.push(`${form.attachments.length} attachment${form.attachments.length > 1 ? 's' : ''}`)
  return items.length ? items.join(', ') : 'Description only'
})

const completenessText = computed(() => {
  const missing = []
  if (!form.title.trim()) missing.push('title')
  if (!form.affectedService) missing.push('service')
  if (!form.description.trim()) missing.push('description')
  return missing.length ? `Missing ${missing.join(', ')}.` : 'Ready to submit.'
})

const syncDescription = () => {
  if (!editorRef.value) return
  form.description = editorRef.value.innerText.trim()
  form.descriptionHtml = editorRef.value.innerHTML
  inlineImageCount.value = editorRef.value.querySelectorAll('img').length
}

const applyFormat = (command: string) => {
  editorRef.value?.focus()
  document.execCommand(command)
  syncDescription()
}

const insertCodeBlock = () => {
  editorRef.value?.focus()
  document.execCommand('insertHTML', false, '<pre><code>Paste error message or log snippet here</code></pre>')
  syncDescription()
}

const insertHtmlAtCursor = (html: string) => {
  editorRef.value?.focus()
  document.execCommand('insertHTML', false, html)
  nextTick(syncDescription)
}

const handleEditorPaste = (event: ClipboardEvent) => {
  const files = Array.from(event.clipboardData?.files || [])
  const imageFiles = files.filter((file) => file.type.startsWith('image/'))
  if (!imageFiles.length) return

  event.preventDefault()
  imageFiles.forEach((file) => {
    const attachmentId = Date.now() + Math.random()
    form.attachments.push({
      id: attachmentId,
      originalName: file.name,
      fileSize: file.size,
      contentType: file.type,
      file
    })
    const reader = new FileReader()
    reader.onload = () => {
      insertHtmlAtCursor(`<img data-local-attachment-id="${attachmentId}" src="${reader.result}" alt="${file.name}" />`)
    }
    reader.readAsDataURL(file)
  })
}

const addAttachments = (files: File[]) => {
  files.forEach((file) => {
    form.attachments.push({
      id: Date.now() + Math.random(),
      originalName: file.name,
      fileSize: file.size,
      contentType: file.type,
      file
    })
  })
}

const handleFileInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  addAttachments(Array.from(target.files || []))
  target.value = ''
}

const handleAttachmentDrop = (event: DragEvent) => {
  addAttachments(Array.from(event.dataTransfer?.files || []))
}

const removeAttachment = (id: number) => {
  const index = form.attachments.findIndex((attachment) => attachment.id === id)
  if (index >= 0) form.attachments.splice(index, 1)
}

const loadOrganizations = async () => {
  orgLoading.value = true
  try {
    const response = await organizationApi.getList({ page: 0, size: 100, sort: 'name' })
    organizationOptions.value = response.content.filter((org) => org.status?.toUpperCase() === 'ACTIVE')
    const storedOrgId = Number(localStorage.getItem('organizationId'))
    if (storedOrgId && organizationOptions.value.some((org) => org.id === storedOrgId)) {
      form.organizationId = storedOrgId
    } else {
      form.organizationId = organizationOptions.value[0]?.id
    }
  } catch (error) {
    ElMessage.error('Unable to load organizations')
  } finally {
    orgLoading.value = false
  }
}

const formatFileSize = (size: number) => {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

const submitRequest = async () => {
  if (!formRef.value) return
  syncDescription()
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (!form.organizationId) return
    submitting.value = true
    try {
      const draftHtml = sanitizeRequestHtml(form.descriptionHtml)
        .replace(/\s+src="data:image\/[^"]*"/gi, '')
      const created = await requestApi.create({
        title: form.title.trim(),
        description: form.description.trim(),
        descriptionHtml: draftHtml,
        type: form.type,
        affectedService: form.affectedService,
        priority: form.priority,
        organizationId: form.organizationId,
        occurrenceTime: form.occurrenceTime || undefined,
        requestedResolutionTime: form.requestedResolutionTime || undefined,
        origin: 'portal'
      })
      let persistedHtml = draftHtml
      for (const draft of form.attachments) {
        if (!draft.file) continue
        const uploaded = await attachmentApi.upload(draft.file, 'REQUEST', created.id)
        persistedHtml = persistedHtml.replace(
          new RegExp(`data-local-attachment-id="${draft.id}"`, 'g'),
          `data-attachment-id="${uploaded.id}"`
        )
      }
      if (persistedHtml !== draftHtml) {
        await requestApi.updateDescription(created.id, {
          description: form.description.trim(),
          descriptionHtml: persistedHtml
        })
      }
      ElMessage.success('Request submitted')
      router.push(`/portal/requests/${created.id}`)
    } finally {
      submitting.value = false
    }
  })
}

onMounted(loadOrganizations)
</script>

<style scoped lang="scss">
.portal-page {
  display: grid;
  gap: 16px;
}

.request-create-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
  align-items: start;
}

.request-form-card {
  min-width: 0;
}

.form-section {
  display: grid;
  gap: 14px;
}

.form-section + .form-section {
  margin-top: 12px;
  padding-top: 16px;
  border-top: 1px solid #edf0f3;
}

.section-heading {
  display: grid;
  gap: 4px;
}

.section-heading strong {
  color: #111827;
  font-size: 15px;
}

.section-heading span {
  color: #667085;
  font-size: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px 18px;
}

.form-grid :deep(.el-date-editor.el-input) {
  width: 100%;
}

.priority-radio {
  width: 100%;
}

.rich-editor-shell {
  overflow: hidden;
  width: 100%;
  border: 1px solid #d8dee8;
  border-radius: 12px;
  background: #fff;
}

.editor-toolbar {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  padding: 8px;
  background: #f8fafc;
  border-bottom: 1px solid #edf0f3;
}

.editor-toolbar :deep(.el-button) {
  border-radius: 8px;
  font-weight: 700;
}

.rich-editor {
  min-height: 172px;
  padding: 12px;
  color: #111827;
  line-height: 1.6;
  outline: none;
}

.rich-editor:empty::before {
  content: attr(data-placeholder);
  color: #98a2b3;
}

.rich-editor :deep(img),
.rich-editor img {
  display: block;
  max-width: 100%;
  margin: 10px 0;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
}

.rich-editor :deep(pre),
.rich-editor pre {
  overflow: auto;
  padding: 10px;
  border-radius: 10px;
  background: #111827;
  color: #f8fafc;
}

.editor-help {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 8px 12px;
  color: #667085;
  font-size: 12px;
  border-top: 1px solid #edf0f3;
  background: #fbfcfd;
}

.attachment-dropzone {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding: 14px;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
  background: #f8fafc;
}

.attachment-dropzone > .el-icon {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  color: #000080;
  background: #eef2ff;
}

.attachment-dropzone strong,
.attachment-item strong,
.summary-list strong,
.completeness-box strong {
  color: #111827;
  font-size: 13px;
}

.attachment-dropzone span,
.attachment-item span,
.summary-list span,
.completeness-box span {
  display: block;
  color: #667085;
  font-size: 12px;
}

.file-input {
  display: none;
}

.attachment-list {
  display: grid;
  gap: 8px;
}

.attachment-item {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  padding: 10px 12px;
  border: 1px solid #edf0f3;
  border-radius: 10px;
  background: #fff;
}

.attachment-item > .el-icon {
  color: #000080;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 14px;
}

.request-summary-panel {
  position: sticky;
  top: 18px;
}

.summary-list {
  display: grid;
  gap: 10px;
}

.summary-list div {
  display: grid;
  gap: 4px;
  padding-bottom: 10px;
  border-bottom: 1px solid #edf0f3;
}

.completeness-box {
  display: grid;
  gap: 5px;
  margin-top: 14px;
  padding: 12px;
  border-radius: 10px;
  border: 1px solid #c7d2fe;
  background: #eef2ff;
}

@media (max-width: 1080px) {
  .request-create-layout {
    grid-template-columns: 1fr;
  }

  .request-summary-panel {
    position: static;
  }
}

@media (max-width: 860px) {
  .form-grid,
  .attachment-dropzone {
    grid-template-columns: 1fr;
  }
}
</style>
