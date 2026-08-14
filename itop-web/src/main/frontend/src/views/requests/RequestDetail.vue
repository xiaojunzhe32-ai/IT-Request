<template>
  <div v-loading="loading" class="detail-page">
    <PageHeader
      eyebrow="Request Detail"
      :title="request.title || 'Request Detail'"
      :description="request.requestNo"
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
          <div class="info-grid">
            <div class="info-cell">
              <span class="info-label">Type</span>
              <span class="info-value">{{ requestTypeLabel(request.type) }}</span>
            </div>
            <div class="info-cell">
              <span class="info-label">Priority</span>
              <span class="info-value"><PriorityTag :priority="request.priority" /></span>
            </div>
            <div class="info-cell">
              <span class="info-label">Requester</span>
              <span class="info-value">{{ request.requester }}</span>
            </div>
            <div class="info-cell">
              <span class="info-label">Team</span>
              <span class="info-value">{{ request.assignedTeam }}</span>
            </div>
            <div class="info-cell">
              <span class="info-label">Assignee</span>
              <span class="info-value">{{ request.assignee || 'Unassigned' }}</span>
            </div>
            <div class="info-cell">
              <span class="info-label">Tester</span>
              <span class="info-value">{{ request.tester || '-' }}</span>
            </div>
            <div class="info-cell">
              <span class="info-label">Affected Service</span>
              <span class="info-value">{{ request.affectedService ? affectedServiceLabel(request.affectedService) : '-' }}</span>
            </div>
            <div class="info-cell">
              <span class="info-label">Occurrence Time</span>
              <span class="info-value">{{ request.occurrenceTime ? formatDateTime(request.occurrenceTime) : '-' }}</span>
            </div>
            <div class="info-cell">
              <span class="info-label">Resolution Time</span>
              <span class="info-value">{{ request.requestedResolutionTime ? formatDateTime(request.requestedResolutionTime) : '-' }}</span>
            </div>
          </div>
        </el-card>

        <el-card v-if="request.description || safeDescriptionHtml || imageAttachments.length || nonImageAttachments.length" class="surface-card" shadow="never">
          <template #header>
            <div class="card-title-row">
              <div>
                <strong>Description</strong>
                <span>Requester-submitted problem details</span>
              </div>
            </div>
          </template>

          <div v-if="request.description || safeDescriptionHtml || imageAttachments.length" class="section-block" style="border-top: none; margin-top: 0; padding-top: 0;">
            <div class="description-window">
              <div v-if="safeDescriptionHtml" ref="descriptionRef" class="rich-description" v-html="safeDescriptionHtml" @click="handleDescriptionClick" />
              <span v-else-if="request.description" class="description-text">{{ request.description }}</span>
            </div>
          </div>

          <div v-if="nonImageAttachments.length" class="section-block">
            <div class="section-label">Attachments</div>
            <div class="detail-attachment-list">
              <div
                v-for="attachment in nonImageAttachments"
                :key="attachment.id"
                class="attachment-row"
              >
                <el-icon class="attachment-icon"><Document /></el-icon>
                <span class="attachment-name" :title="attachment.originalName">{{ attachment.originalName }}</span>
                <el-button text size="small" @click="downloadAttachment(attachment)">Download</el-button>
              </div>
            </div>
          </div>
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
                  <span>{{ comment.role }} - {{ formatDateTime(comment.time) }}</span>
                  <el-tag v-if="comment.internal" size="small" type="warning" effect="light">Internal</el-tag>
                </div>
                <p>{{ comment.message }}</p>
                <div v-if="commentImageAttachments(comment).length" class="comment-image-list">
                  <el-image
                    v-for="attachment in commentImageAttachments(comment)"
                    :key="attachment.id"
                    :src="imageUrls[attachment.id]"
                    :preview-src-list="commentImagePreviewList(comment)"
                    :initial-index="commentImagePreviewIndex(comment, attachment.id)"
                    :preview-teleported="true"
                    :hide-on-click-modal="true"
                    class="comment-image-thumb"
                  />
                </div>
                <div v-if="commentNonImageAttachments(comment).length" class="comment-attachments">
                  <div
                    v-for="attachment in commentNonImageAttachments(comment)"
                    :key="attachment.id"
                    class="attachment-row"
                  >
                    <el-icon class="attachment-icon"><Document /></el-icon>
                    <span class="attachment-name" :title="attachment.originalName">{{ attachment.originalName }}</span>
                    <el-button text size="small" @click="downloadAttachment(attachment)">Download</el-button>
                  </div>
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
              :accept="ACCEPT_ATTR"
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
            <el-form-item label="Team">
              <el-select
                v-model="assignment.teamId"
                :disabled="portalMode"
                filterable
                placeholder="Select team"
              >
                <el-option
                  v-for="team in teamOptions"
                  :key="team.id"
                  :label="team.name"
                  :value="team.id"
                />
              </el-select>
            </el-form-item>
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
                  :label="`${person.name} - ${person.team}`"
                  :value="person.id"
                >
                  <div class="assignee-option">
                    <span>{{ person.name }}</span>
                    <small>{{ person.team }}</small>
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

    <el-dialog
      v-model="descImagePreviewVisible"
      :show-close="true"
      :close-on-click-modal="true"
      align-center
      width="auto"
      append-to-body
    >
      <img :src="descPreviewSrc" class="desc-preview-img" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Picture } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import PriorityTag from '@/components/PriorityTag.vue'
import RequestStatusTag from '@/components/RequestStatusTag.vue'
import { requestApi } from '@/api/requests'
import { attachmentApi } from '@/api/attachments'
import { teamApi, userApi } from '@/api/system'
import { requestStatuses, sanitizeRequestHtml } from '@/data/requestOptions'
import { useCodeTableStore } from '@/stores/codeTables'
import type { RequestAttachment, RequestHistoryItem, RequestStatus, WorkflowRequest } from '@/types/requests'
import type { Team, SystemUser } from '@/types/system'
import { formatDateTime } from '@/utils/format'
import { validateFile, validateBatch, ACCEPT_ATTR } from '@/utils/uploadValidation'

interface AssigneeOption {
  id: number
  name: string
  team: string
}

interface HistoryPart {
  text: string
  kind: 'text' | 'status' | 'target' | 'team'
}

const route = useRoute()
const router = useRouter()
const codeTableStore = useCodeTableStore()
const request = ref<WorkflowRequest>({
  id: Number(route.params.id) || 0,
  requestNo: 'Loading...',
  title: '',
  description: '',
  type: '',
  priority: 'Medium',
  status: 'New',
  requester: '',
  assignedTeam: '',
  assignee: '',
  tester: '',
  createdAt: '',
  updatedAt: '',
  comments: [],
  history: []
})
const users = ref<SystemUser[]>([])
const teams = ref<Team[]>([])
const loading = ref(false)
const commentText = ref('')
const internalComment = ref(false)
const commentFileInputRef = ref<HTMLInputElement>()
const commentFiles = ref<File[]>([])
const submittingComment = ref(false)
const statusDraft = ref<RequestStatus>('New')
const unsavedDialogVisible = ref(false)
const descriptionRef = ref<HTMLElement>()
const descImagePreviewVisible = ref(false)
const descPreviewSrc = ref('')
const pendingLeaveAction = ref<(() => void) | null>(null)
let allowRouteLeave = false
let inlineBlobUrls: string[] = []
const imageUrls = ref<Record<number, string>>({})

const isImage = (attachment: RequestAttachment) => {
  const type = attachment.contentType || ''
  const name = attachment.originalName || ''
  return type.startsWith('image/') || /\.(png|jpe?g|gif|bmp|svg|webp)$/i.test(name)
}

const imageAttachments = computed(() =>
  (request.value.attachments || []).filter(isImage)
)

const nonImageAttachments = computed(() =>
  (request.value.attachments || []).filter((a) => !isImage(a))
)

const imagePreviewList = computed(() =>
  imageAttachments.value
    .map((a) => imageUrls.value[a.id])
    .filter(Boolean) as string[]
)

const imagePreviewIndex = (id: number) => {
  const idx = imageAttachments.value.findIndex((a) => a.id === id)
  return idx >= 0 ? idx : 0
}

const commentImageAttachments = (comment: { attachments?: RequestAttachment[] }) =>
  (comment.attachments || []).filter(isImage)

const commentNonImageAttachments = (comment: { attachments?: RequestAttachment[] }) =>
  (comment.attachments || []).filter((a) => !isImage(a))

const commentImagePreviewList = (comment: { attachments?: RequestAttachment[] }) =>
  commentImageAttachments(comment)
    .map((a) => imageUrls.value[a.id])
    .filter(Boolean) as string[]

const commentImagePreviewIndex = (comment: { attachments?: RequestAttachment[] }, id: number) => {
  const imgs = commentImageAttachments(comment)
  const idx = imgs.findIndex((a) => a.id === id)
  return idx >= 0 ? idx : 0
}

const loadImageUrls = async () => {
  const all: RequestAttachment[] = [
    ...(request.value.attachments || []),
    ...(request.value.comments || []).flatMap((c) => c.attachments || [])
  ]
  await Promise.all(all.filter(isImage).map(async (att) => {
    if (imageUrls.value[att.id]) return
    try {
      const blob = await attachmentApi.download(att.id)
      imageUrls.value[att.id] = URL.createObjectURL(blob)
    } catch { /* skip */ }
  }))
}

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
  teamId: undefined as number | undefined,
  assigneeId: undefined as number | undefined
})

const displayUserName = (user: SystemUser) => {
  const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim()
  return fullName || user.username
}

const teamOptions = computed<Team[]>(() => {
  const items = new Map<number, Team>()
  teams.value.forEach((team) => items.set(team.id, team))
  if (request.value.teamId && !items.has(request.value.teamId)) {
    items.set(request.value.teamId, {
      id: request.value.teamId,
      name: request.value.assignedTeam || `Team ${request.value.teamId}`
    })
  }
  return Array.from(items.values())
})

const assigneeOptions = computed<AssigneeOption[]>(() => {
  const assignableRoles = new Set(['ADMIN', 'TEAM_LEAD', 'TECHNICIAN'])
  const people = new Map<number, AssigneeOption>()

  users.value.forEach((user) => {
    const roles = user.roleCodes || []
    const canAssign = roles.length === 0 || roles.some((role) => assignableRoles.has(role))
    if (!canAssign || user.status?.toLowerCase() === 'inactive') return
    people.set(user.id, {
      id: user.id,
      name: displayUserName(user),
      team: user.teamNames?.length ? user.teamNames.join(', ') : 'No team'
    })
  })

  if (request.value.agentId && !people.has(request.value.agentId)) {
    people.set(request.value.agentId, {
      id: request.value.agentId,
      name: request.value.assignee || `User ${request.value.agentId}`,
      team: request.value.assignedTeam || 'Current assignment'
    })
  }

  return Array.from(people.values())
})

const selectedTeam = computed(() =>
  teamOptions.value.find((team) => team.id === assignment.teamId)
)
const selectedAssignee = computed(() =>
  assigneeOptions.value.find((person) => person.id === assignment.assigneeId)
)

const hasStatusChange = computed(() => statusDraft.value !== request.value.status)
const hasTeamChange = computed(() => assignment.teamId !== request.value.teamId)
const hasAssigneeChange = computed(() => assignment.assigneeId !== request.value.agentId)
const hasAssignmentChange = computed(() => hasTeamChange.value || hasAssigneeChange.value)
const hasUnsavedChanges = computed(() => hasStatusChange.value || hasAssignmentChange.value)
const safeDescriptionHtml = ref('')

const formatHistoryAction = (action: string) => action
  .replace(/_/g, ' ')
  .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase())

const requestTypeLabel = (value?: string) => codeTableStore.labelFor('REQUEST_TYPE', value, value || '-')
const affectedServiceLabel = (value?: string) => codeTableStore.labelFor('AFFECTED_SERVICE', value, value || '-')

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

const handleDescriptionClick = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (target.tagName === 'IMG') {
    descPreviewSrc.value = (target as HTMLImageElement).src
    descImagePreviewVisible.value = true
  }
}

const syncDraftsFromRequest = () => {
  statusDraft.value = request.value.status
  assignment.teamId = request.value.teamId
  assignment.assigneeId = request.value.agentId
}

const loadRequest = async () => {
  loading.value = true
  try {
    request.value = await requestApi.getById(Number(route.params.id))
    syncDraftsFromRequest()
    await hydrateDescriptionImages()
    await loadImageUrls()
  } catch {
    ElMessage.error('Unable to load request detail')
  } finally {
    loading.value = false
  }
}

const loadTeams = async () => {
  if (portalMode.value) return
  try {
    const response = await teamApi.list({ page: 0, size: 100, sort: 'name', type: 'ITMD' })
    teams.value = response.content
  } catch {
    ElMessage.error('Unable to load team options')
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
  if (hasTeamChange.value && !selectedTeam.value) {
    throw new Error('Please select a valid team')
  }
  if (hasAssigneeChange.value && assignment.assigneeId && !selectedAssignee.value) {
    throw new Error('Please select a valid assignee')
  }
  request.value = await requestApi.assign(request.value.id, {
    teamId: assignment.teamId,
    agentId: assignment.assigneeId
  })
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
  const newFiles = Array.from(target.files || [])
  const validFiles: File[] = []
  for (const file of newFiles) {
    const result = validateFile(file)
    if (result.valid) {
      validFiles.push(file)
    } else {
      ElMessage.warning(result.error)
    }
  }
  if (validFiles.length) {
    const batchResult = validateBatch([...commentFiles.value, ...validFiles])
    if (!batchResult.valid) {
      ElMessage.warning(batchResult.error)
    } else {
      commentFiles.value.push(...validFiles)
    }
  }
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
  codeTableStore.ensureTables('REQUEST_TYPE', 'AFFECTED_SERVICE').catch(() => undefined)
  loadRequest()
  loadTeams()
  loadUsers()
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
  clearInlineBlobUrls()
  Object.values(imageUrls.value).forEach((url) => URL.revokeObjectURL(url))
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
  min-width: 0;
}

.state-editor {
  display: grid;
  grid-template-columns: minmax(180px, 0.45fr) minmax(0, 1fr);
  gap: 16px;
  align-items: end;
}

.state-current {
  min-height: 56px;
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
  overflow-wrap: break-word;
  word-break: break-word;
  min-width: 0;
}

.rich-description :deep(a) {
  word-break: break-all;
}

.rich-description :deep(table) {
  display: block;
  max-width: 100%;
  overflow-x: auto;
  border-collapse: collapse;
}

.rich-description :deep(th),
.rich-description :deep(td) {
  padding: 6px 10px;
  border: 1px solid #e5e7eb;
  text-align: left;
}

.rich-description :deep(img) {
  display: block;
  width: min(100%, 360px);
  max-height: 220px;
  margin: 8px 0;
  object-fit: contain;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  cursor: zoom-in;
}

.rich-description :deep(pre) {
  overflow: auto;
  padding: 10px;
  border-radius: 10px;
  background: #111827;
  color: #f8fafc;
}

.description-window {
  max-height: none;
  overflow: visible;
  padding: 4px;
  border-radius: 8px;
}

.section-block {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #edf0f3;
}

.section-label {
  font-size: 13px;
  font-weight: 700;
  color: #344054;
  margin-bottom: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 1px;
  background: #e5e7eb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.info-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 8px 12px;
  background: #fff;
}

.info-label {
  font-size: 11px;
  font-weight: 600;
  color: #98a2b3;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.info-value {
  font-size: 13px;
  color: #344054;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.description-text {
  color: #344054;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.description-images {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}

.inline-image {
  width: 100%;
  max-height: 500px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #e5e7eb;
}

.inline-image:hover {
  border-color: #3b82f6;
}

.comment-image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.comment-image-thumb {
  width: 120px;
  height: 90px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #e5e7eb;
}

.comment-image-thumb:hover {
  border-color: #3b82f6;
}

.detail-attachment-list {
  display: grid;
  gap: 8px;
  max-height: 644px;
  overflow-y: auto;
  padding: 4px;
  border-radius: 8px;
}

.detail-attachment-list .el-button,
.comment-attachments .el-button {
  padding: 5px 9px;
  color: #475467;
  font-size: 12px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
}

.attachment-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
}

.attachment-icon {
  font-size: 28px;
  color: #667085;
  flex-shrink: 0;
}

.attachment-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  color: #344054;
}

.comment-attachments,
.pending-attachments {
  display: grid;
  gap: 8px;
  margin-top: 8px;
  max-height: 280px;
  overflow-y: auto;
  padding: 4px;
  border-radius: 8px;
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
  max-height: 280px;
  overflow-y: auto;
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
  .info-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 720px) {
  .state-editor,
  .state-form {
    grid-template-columns: 1fr;
  }
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.desc-preview-img {
  max-width: 90vw;
  max-height: 85vh;
  object-fit: contain;
  border-radius: 8px;
}
</style>
