<template>
  <div v-loading="loading" class="detail-page">
    <PageHeader
      eyebrow="Request Detail"
      :title="request.requestNo"
      :description="request.title"
    >
      <template #actions>
        <el-button @click="goBack">Back</el-button>
        <el-button type="primary" :disabled="!hasUnsavedChanges" @click="saveAllChanges">
          Save
        </el-button>
      </template>
    </PageHeader>

    <div class="detail-layout">
      <main class="detail-main">
        <el-card class="surface-card" shadow="never">
          <template #header>
            <div class="card-title-row">
              <div>
                <strong>Workflow State</strong>
                <span>{{ workflowInstruction }}</span>
              </div>
              <RequestStatusTag :status="request.status" />
            </div>
          </template>

          <div class="state-editor">
            <div class="state-current">
              <span>Current Status</span>
              <RequestStatusTag :status="request.status" />
            </div>

            <el-form label-position="top" class="state-form">
              <el-form-item label="Change Status">
                <el-select v-model="statusDraft" placeholder="Select status">
                  <el-option
                    v-for="status in visibleStatusOptions"
                    :key="status"
                    :label="status"
                    :value="status"
                  />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
        </el-card>

        <el-card class="surface-card" shadow="never">
          <template #header>
            <div class="card-title-row">
              <div>
                <strong>Request Information</strong>
                <span>Requester-submitted details and ownership</span>
              </div>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="Type">{{ request.type }}</el-descriptions-item>
            <el-descriptions-item label="Priority">
              <PriorityTag :priority="request.priority" />
            </el-descriptions-item>
            <el-descriptions-item v-if="request.affectedService" label="Affected Service / System">
              {{ request.affectedService }}
            </el-descriptions-item>
            <el-descriptions-item v-if="request.occurrenceTime" label="Occurrence Time">
              {{ formatDateTime(request.occurrenceTime) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="request.requestedResolutionTime" label="Requested Resolution Time">
              {{ formatDateTime(request.requestedResolutionTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="Requester">{{ request.requester }}</el-descriptions-item>
            <el-descriptions-item label="Organization">{{ request.requesterOrg }}</el-descriptions-item>
            <el-descriptions-item label="Team">{{ request.assignedTeam }}</el-descriptions-item>
            <el-descriptions-item label="Assignee">{{ request.assignee || 'Unassigned' }}</el-descriptions-item>
            <el-descriptions-item label="Tester">{{ request.tester }}</el-descriptions-item>
            <el-descriptions-item label="Description" :span="2">
              <div v-if="safeDescriptionHtml" class="rich-description" v-html="safeDescriptionHtml" />
              <span v-else>{{ request.description }}</span>
            </el-descriptions-item>
            <el-descriptions-item v-if="request.attachments?.length" label="Attachments" :span="2">
              <div class="detail-attachment-list">
                <el-button
                  v-for="attachment in request.attachments"
                  :key="attachment.id"
                  text
                  @click="downloadAttachment(attachment)"
                >
                  {{ attachment.originalName }}
                </el-button>
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="surface-card" shadow="never">
          <template #header>
            <div class="card-title-row">
              <div>
                <strong>Comments</strong>
                <span>Messages are recorded as request history</span>
              </div>
            </div>
          </template>
          <div class="comment-list">
            <article v-for="comment in request.comments" :key="comment.id" class="comment-item">
              <div class="comment-avatar">{{ (comment.author || '?').slice(0, 1) }}</div>
              <div>
                <div class="comment-meta">
                  <strong>{{ comment.author }}</strong>
                  <span>{{ comment.role }} - {{ comment.time }}</span>
                  <el-tag v-if="comment.internal" size="small" type="warning" effect="light">Internal</el-tag>
                </div>
                <p>{{ comment.message }}</p>
                <div v-if="comment.attachments?.length" class="comment-attachments">
                  <el-button
                    v-for="attachment in comment.attachments"
                    :key="attachment.id"
                    text
                    @click="downloadAttachment(attachment)"
                  >
                    {{ attachment.originalName }}
                  </el-button>
                </div>
              </div>
            </article>
          </div>

          <div class="comment-box">
            <el-input
              v-model="commentText"
              type="textarea"
              :rows="3"
              placeholder="Leave a message or internal note"
            />
            <input
              ref="commentFileInputRef"
              class="file-input"
              type="file"
              multiple
              @change="handleCommentFiles"
            >
            <div v-if="commentFiles.length" class="pending-attachments">
              <span v-for="(file, index) in commentFiles" :key="`${file.name}-${index}`">
                {{ file.name }}
                <el-button text @click="commentFiles.splice(index, 1)">Remove</el-button>
              </span>
            </div>
            <div class="comment-actions">
              <el-checkbox v-model="internalComment" :disabled="portalMode">Internal note</el-checkbox>
              <div>
                <el-button @click="commentFileInputRef?.click()">Attach Files</el-button>
                <el-button type="primary" :loading="submittingComment" @click="submitComment">Add Comment</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </main>

      <aside class="detail-side">
        <el-card class="surface-card" shadow="never">
          <template #header>
            <div class="card-title-row">
              <div>
                <strong>Assignment</strong>
                <span>Select assignee, then save at page level</span>
              </div>
            </div>
          </template>
          <el-form label-position="top">
            <el-form-item label="Assignee">
              <el-select
                v-model="assignment.assigneeId"
                :disabled="portalMode"
                filterable
                placeholder="Select assignee"
              >
                <el-option
                  v-for="person in assigneeOptions"
                  :key="person.id"
                  :label="`${person.name} - ${person.organization}`"
                  :value="person.id"
                >
                  <div class="assignee-option">
                    <span>{{ person.name }}</span>
                    <small>{{ person.organization }}</small>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="surface-card" shadow="never">
          <template #header>
            <div class="card-title-row">
              <div>
                <strong>History</strong>
                <span>Audit trail for this request</span>
              </div>
            </div>
          </template>
          <el-timeline class="history-timeline">
            <el-timeline-item
              v-for="item in request.history"
              :key="item.id"
              :timestamp="formatDateTime(item.time)"
              placement="top"
            >
              <strong class="history-action">{{ formatHistoryAction(item.action) }}</strong>
              <div class="history-detail">
                <span class="history-chip actor">{{ item.actor }}</span>
                <template v-for="(part, index) in formatHistoryParts(item)" :key="`${item.id}-${index}`">
                  <span v-if="part.kind === 'text'" class="history-text">{{ part.text }}</span>
                  <span v-else class="history-chip" :class="part.kind">{{ part.text }}</span>
                </template>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </aside>
    </div>

    <el-dialog
      v-model="unsavedDialogVisible"
      title="Unsaved Changes"
      width="440px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <p class="unsaved-copy">
        You changed the workflow status or assignee but have not saved it yet.
      </p>
      <template #footer>
        <el-button @click="cancelPendingLeave">Cancel</el-button>
        <el-button @click="discardAndExit">Discard and Exit</el-button>
        <el-button type="primary" @click="saveAndExit">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import PriorityTag from '@/components/PriorityTag.vue'
import RequestStatusTag from '@/components/RequestStatusTag.vue'
import { requestApi } from '@/api/requests'
import { attachmentApi } from '@/api/attachments'
import { userApi } from '@/api/system'
import { requestStatuses, sanitizeRequestHtml } from '@/data/requestOptions'
import type { RequestAttachment, RequestHistoryItem, RequestStatus, WorkflowRequest } from '@/types/requests'
import type { SystemUser } from '@/types/system'
import { formatDateTime } from '@/utils/format'

interface AssigneeOption {
  id: number
  name: string
  organization: string
}

interface HistoryPart {
  text: string
  kind: 'text' | 'status' | 'target' | 'team'
}

const route = useRoute()
const router = useRouter()
const request = ref<WorkflowRequest>({
  id: Number(route.params.id) || 0,
  requestNo: 'Loading...',
  title: '',
  description: '',
  type: '',
  priority: 'Medium',
  status: 'New',
  requester: '',
  requesterOrg: '',
  assignedTeam: '',
  assignee: '',
  tester: '',
  createdAt: '',
  updatedAt: '',
  comments: [],
  history: []
})
const users = ref<SystemUser[]>([])
const loading = ref(false)
const commentText = ref('')
const internalComment = ref(false)
const commentFileInputRef = ref<HTMLInputElement>()
const commentFiles = ref<File[]>([])
const submittingComment = ref(false)
const statusDraft = ref<RequestStatus>('New')
const unsavedDialogVisible = ref(false)
const pendingLeaveAction = ref<(() => void) | null>(null)
let allowRouteLeave = false
let inlineBlobUrls: string[] = []

const portalMode = computed(() => route.path.startsWith('/portal'))
const visibleStatusOptions = computed<RequestStatus[]>(() => {
  if (!portalMode.value) return requestStatuses
  if (request.value.status === 'Closed') {
    return ['Closed', 'Resolved', 'User Test Failed']
  }
  if (request.value.status === 'Resolved') {
    return ['Resolved', 'Closed', 'User Test Failed']
  }
  return [request.value.status]
})
const workflowInstruction = computed(() => portalMode.value
  ? 'Confirm the result after the request is resolved'
  : 'Select any status, then save at page level')

const assignment = reactive({
  assigneeId: undefined as number | undefined
})

const displayUserName = (user: SystemUser) => {
  const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim()
  return fullName || user.username
}

const assigneeOptions = computed<AssigneeOption[]>(() => {
  const assignableRoles = new Set(['ADMIN', 'TEAM_LEAD', 'TECHNICIAN', 'TESTER'])
  const people = new Map<number, AssigneeOption>()

  users.value.forEach((user) => {
    const roles = user.roleCodes || []
    const canAssign = roles.length === 0 || roles.some((role) => assignableRoles.has(role))
    if (!canAssign || user.status?.toLowerCase() === 'inactive') return
    people.set(user.id, {
      id: user.id,
      name: displayUserName(user),
      organization: user.teamNames?.length ? user.teamNames.join(', ') : user.organizationName || 'Organization not set'
    })
  })

  if (request.value.agentId && !people.has(request.value.agentId)) {
    people.set(request.value.agentId, {
      id: request.value.agentId,
      name: request.value.assignee || `User ${request.value.agentId}`,
      organization: request.value.assignedTeam || 'Current assignment'
    })
  }

  return Array.from(people.values())
})

const selectedAssignee = computed(() =>
  assigneeOptions.value.find((person) => person.id === assignment.assigneeId)
)

const hasStatusChange = computed(() => statusDraft.value !== request.value.status)
const hasAssignmentChange = computed(() => assignment.assigneeId !== request.value.agentId)
const hasUnsavedChanges = computed(() => hasStatusChange.value || hasAssignmentChange.value)
const safeDescriptionHtml = ref('')

const formatHistoryAction = (action: string) => action
  .replace(/_/g, ' ')
  .toLowerCase()
  .replace(/\b\w/g, (letter) => letter.toUpperCase())

const formatHistoryParts = (item: RequestHistoryItem): HistoryPart[] => {
  const detail = item.detail || ''
  const statusMatch = detail.match(/^(.+?)\s*->\s*(.+?)\.?$/)
  if (statusMatch) {
    return [
      { text: 'changed from', kind: 'text' },
      { text: statusMatch[1].trim(), kind: 'status' },
      { text: 'to', kind: 'text' },
      { text: statusMatch[2].replace(/\.$/, '').trim(), kind: 'status' }
    ]
  }

  const assignmentMatch = detail.match(/^Assigned to (.+?)(?: in (.+?))?\.?$/)
  if (assignmentMatch) {
    const parts: HistoryPart[] = [
      { text: 'assigned to', kind: 'text' },
      { text: assignmentMatch[1].trim(), kind: 'target' }
    ]
    if (assignmentMatch[2]) {
      parts.push(
        { text: 'in', kind: 'text' },
        { text: assignmentMatch[2].replace(/\.$/, '').trim(), kind: 'team' }
      )
    }
    return parts
  }

  return [{ text: detail, kind: 'text' }]
}

const clearInlineBlobUrls = () => {
  inlineBlobUrls.forEach((url) => URL.revokeObjectURL(url))
  inlineBlobUrls = []
}

const hydrateDescriptionImages = async () => {
  clearInlineBlobUrls()
  const html = sanitizeRequestHtml(request.value.descriptionHtml || '')
  if (!html) {
    safeDescriptionHtml.value = ''
    return
  }
  const template = document.createElement('template')
  template.innerHTML = html
  const images = Array.from(template.content.querySelectorAll<HTMLImageElement>('img[data-attachment-id]'))
  await Promise.all(images.map(async (image) => {
    const attachmentId = Number(image.dataset.attachmentId)
    if (!attachmentId) return
    try {
      const blob = await attachmentApi.download(attachmentId)
      const url = URL.createObjectURL(blob)
      inlineBlobUrls.push(url)
      image.src = url
    } catch {
      image.replaceWith(document.createTextNode(`[Image attachment ${attachmentId} unavailable]`))
    }
  }))
  safeDescriptionHtml.value = template.innerHTML
}

const syncDraftsFromRequest = () => {
  statusDraft.value = request.value.status
  assignment.assigneeId = request.value.agentId
}

const loadRequest = async () => {
  loading.value = true
  try {
    request.value = await requestApi.getById(Number(route.params.id))
    syncDraftsFromRequest()
    await hydrateDescriptionImages()
  } catch {
    ElMessage.error('Unable to load request detail')
  } finally {
    loading.value = false
  }
}

const loadUsers = async () => {
  if (portalMode.value) return
  try {
    const response = await userApi.list({ page: 0, size: 100, sort: 'username', status: 'active' })
    users.value = response.content
  } catch {
    ElMessage.error('Unable to load assignee options')
  }
}

const goBack = () => {
  requestLeave(() => router.back())
}

const saveStatusChange = async () => {
  if (!hasStatusChange.value) return
  request.value = await requestApi.transition(request.value.id, { status: statusDraft.value })
}

const saveAssignmentChange = async () => {
  if (!hasAssignmentChange.value) return
  if (!selectedAssignee.value) {
    throw new Error('Please select a valid assignee')
  }
  request.value = await requestApi.assign(request.value.id, { agentId: selectedAssignee.value.id })
}

const saveAllChanges = async () => {
  const statusChanged = hasStatusChange.value
  const assignmentChanged = hasAssignmentChange.value

  if (!statusChanged && !assignmentChanged) {
    ElMessage.info('No changes to save')
    return false
  }

  loading.value = true
  try {
    await saveStatusChange()
    await saveAssignmentChange()
    await loadRequest()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : 'Unable to save changes')
    return false
  } finally {
    loading.value = false
  }

  if (statusChanged && assignmentChanged) {
    ElMessage.success('Status and assignment saved')
  } else if (statusChanged) {
    ElMessage.success(`Status saved as ${statusDraft.value}`)
  } else {
    ElMessage.success('Assignment saved')
  }
  return true
}

const resetDrafts = () => {
  syncDraftsFromRequest()
}

const requestLeave = (action: () => void) => {
  if (!hasUnsavedChanges.value) {
    action()
    return
  }
  pendingLeaveAction.value = action
  unsavedDialogVisible.value = true
}

const cancelPendingLeave = () => {
  pendingLeaveAction.value = null
  unsavedDialogVisible.value = false
}

const discardAndExit = () => {
  resetDrafts()
  unsavedDialogVisible.value = false
  const action = pendingLeaveAction.value
  pendingLeaveAction.value = null
  allowRouteLeave = true
  action?.()
}

const saveAndExit = async () => {
  if (!await saveAllChanges()) return
  unsavedDialogVisible.value = false
  const action = pendingLeaveAction.value
  pendingLeaveAction.value = null
  allowRouteLeave = true
  action?.()
}

const submitComment = async () => {
  if (!commentText.value.trim()) {
    ElMessage.warning('Please enter a comment')
    return
  }
  submittingComment.value = true
  try {
    const comment = await requestApi.addComment(request.value.id, {
      message: commentText.value.trim(),
      internal: !portalMode.value && internalComment.value
    })
    for (const file of commentFiles.value) {
      await attachmentApi.upload(file, 'REQUEST_COMMENT', comment.id)
    }
    commentText.value = ''
    internalComment.value = false
    commentFiles.value = []
    await loadRequest()
    ElMessage.success('Comment added')
  } catch {
    ElMessage.error('Unable to add comment')
  } finally {
    submittingComment.value = false
  }
}

const handleCommentFiles = (event: Event) => {
  const target = event.target as HTMLInputElement
  commentFiles.value.push(...Array.from(target.files || []))
  target.value = ''
}

const downloadAttachment = async (attachment: RequestAttachment) => {
  try {
    const blob = await attachmentApi.download(attachment.id)
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = attachment.originalName
    link.click()
    URL.revokeObjectURL(url)
  } catch {
    ElMessage.error('Unable to download attachment')
  }
}

onBeforeRouteLeave((to, _from, next) => {
  if (allowRouteLeave || !hasUnsavedChanges.value) {
    next()
    return
  }
  pendingLeaveAction.value = () => {
    router.push(to.fullPath)
  }
  unsavedDialogVisible.value = true
  next(false)
})

const handleBeforeUnload = (event: BeforeUnloadEvent) => {
  if (!hasUnsavedChanges.value) return
  event.preventDefault()
  event.returnValue = ''
}

onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload)
  loadRequest()
  loadUsers()
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
  clearInlineBlobUrls()
})
</script>

<style scoped lang="scss">
.detail-page {
  display: grid;
  gap: 16px;
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 16px;
  align-items: start;
}

.detail-main,
.detail-side {
  display: grid;
  gap: 16px;
}

.state-editor {
  display: grid;
  grid-template-columns: minmax(180px, 0.45fr) minmax(0, 1fr);
  gap: 16px;
  align-items: end;
}

.state-current {
  min-height: 84px;
  display: grid;
  align-content: center;
  gap: 10px;
  padding: 14px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
}

.state-current span {
  color: #667085;
  font-size: 12px;
  font-weight: 700;
}

.state-form {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
}

.state-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.assignee-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.assignee-option span {
  font-weight: 700;
}

.assignee-option small {
  color: #667085;
}

.rich-description {
  display: grid;
  gap: 8px;
  color: #111827;
  line-height: 1.6;
}

.rich-description :deep(img) {
  display: block;
  max-width: 100%;
  margin: 8px 0;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
}

.rich-description :deep(pre) {
  overflow: auto;
  padding: 10px;
  border-radius: 10px;
  background: #111827;
  color: #f8fafc;
}

.detail-attachment-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-attachment-list .el-button,
.comment-attachments .el-button {
  padding: 5px 9px;
  color: #475467;
  font-size: 12px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
}

.comment-attachments,
.pending-attachments {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.pending-attachments span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding-left: 9px;
  color: #475467;
  font-size: 12px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.file-input {
  display: none;
}

.comment-list {
  display: grid;
  gap: 12px;
}

.comment-item {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr);
  gap: 12px;
  padding: 12px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
}

.comment-avatar {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 999px;
  background: #1c2430;
  color: #fff;
  font-weight: 800;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.comment-meta strong {
  color: #111827;
  font-size: 13px;
}

.comment-meta span,
.comment-item p {
  color: #667085;
  font-size: 12px;
}

.comment-item p {
  margin: 6px 0 0;
  line-height: 1.6;
}

.comment-box {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #edf0f3;
}

.comment-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
}

.history-timeline {
  padding-left: 4px;
}

.history-action {
  display: inline-block;
  margin-bottom: 6px;
  color: #111827;
  font-size: 12px;
  letter-spacing: 0;
}

.history-detail {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 5px;
  color: #667085;
  font-size: 12px;
  line-height: 1.7;
}

.history-text {
  color: #667085;
}

.history-chip {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  padding: 2px 6px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.35;
  word-break: break-word;
}

.history-chip.actor {
  color: #000080;
  background: #eef2ff;
  border: 1px solid #c7d2fe;
}

.history-chip.status {
  color: #027a48;
  background: #dcfae6;
  border: 1px solid #abefc6;
}

.history-chip.target {
  color: #b42318;
  background: #fee4e2;
  border: 1px solid #fecdca;
}

.history-chip.team {
  color: #344054;
  background: #f2f4f7;
  border: 1px solid #d0d5dd;
}

.unsaved-copy {
  margin: 0;
  color: #475467;
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 1180px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .state-editor,
  .state-form {
    grid-template-columns: 1fr;
  }
}
</style>
