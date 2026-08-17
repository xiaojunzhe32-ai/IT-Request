<template>
  <div v-loading="loading" class="detail-page">
    <section class="work-item-header">
      <div class="work-title-row">
        <div class="work-title-main">
          <div class="work-type-line">
            <span class="work-type-dot"></span>
            <span>REQUEST</span>
            <strong>{{ request.requestNo }}</strong>
          </div>
          <h1>{{ request.title || 'Request Detail' }}</h1>
        </div>
        <div class="work-header-actions">
        <el-button @click="goBack">Back</el-button>
        <el-button type="primary" :disabled="!hasUnsavedChanges" @click="saveAllChanges">
          Save
        </el-button>
        </div>
      </div>

      <div class="work-owner-row">
        <div class="owner-avatar">{{ assigneeInitial }}</div>
        <div class="owner-editor">
          <span>Assigned To</span>
          <el-select
            v-model="assignment.assigneeId"
            :disabled="portalMode"
            filterable
            placeholder="Unassigned"
            class="owner-select"
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
        </div>
        <div v-if="selectedAssignee?.team || request.assignedTeam" class="owner-team">
          {{ selectedAssignee?.team || request.assignedTeam }}
        </div>
      </div>

      <div class="work-meta-strip">
        <div class="work-meta-field work-meta-field--state">
          <span class="meta-label">State</span>
          <el-select v-model="statusDraft" placeholder="Select status" class="state-select">
            <el-option
              v-for="status in visibleStatusOptions"
              :key="status"
              :label="status"
              :value="status"
            />
          </el-select>
        </div>
        <div class="work-meta-field work-meta-field--assignment">
          <span class="meta-label">Assignment</span>
          <el-select
            v-model="assignment.teamId"
            :disabled="portalMode"
            filterable
            placeholder="Select team"
            class="team-select"
          >
            <el-option
              v-for="team in teamOptions"
              :key="team.id"
              :label="team.name"
              :value="team.id"
            />
          </el-select>
        </div>
        <div class="work-meta-field">
          <span class="meta-label">Priority</span>
          <span class="meta-value"><PriorityTag :priority="request.priority" /></span>
        </div>
        <div class="work-meta-field">
          <span class="meta-label">Type</span>
          <span class="meta-value">{{ requestTypeLabel(request.type) }}</span>
        </div>
      </div>
    </section>

    <div class="detail-layout">
      <main class="detail-main">
        <section v-if="request.description || safeDescriptionHtml || visibleImageAttachments.length || nonImageAttachments.length" class="work-section description-section">
          <div class="work-section__header">
            <h2>Description</h2>
            <span>Requester-submitted problem details</span>
          </div>

          <div v-if="request.description || safeDescriptionHtml || visibleImageAttachments.length" class="section-block" style="border-top: none; margin-top: 0; padding-top: 0;">
            <div class="description-window">
              <div v-if="safeDescriptionHtml" ref="descriptionRef" class="rich-description" v-html="safeDescriptionHtml" @click="handleDescriptionClick" />
              <span v-else-if="request.description" class="description-text">{{ request.description }}</span>
              <div v-if="visibleImageAttachments.length" class="description-images">
                <el-image
                  v-for="attachment in visibleImageAttachments"
                  :key="attachment.id"
                  :src="imageUrls[attachment.id]"
                  :preview-src-list="imagePreviewList"
                  :initial-index="imagePreviewIndex(attachment.id)"
                  :preview-teleported="true"
                  :hide-on-click-modal="true"
                  class="inline-image"
                />
              </div>
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
        </section>

        <section class="work-section comments-section">
          <div class="work-section__header">
            <h2>Comments</h2>
            <span>Messages are recorded as request history</span>
          </div>
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
        </section>
      </main>

      <aside class="detail-side">
        <section class="inspector-panel">
          <div class="inspector-section">
            <div class="inspector-title">Details</div>
            <div class="field-list">
              <div class="field-row">
                <span>Requester</span>
                <strong>{{ request.requester || '-' }}</strong>
              </div>
              <div class="field-row">
                <span>Affected Service</span>
                <strong>{{ request.affectedService ? affectedServiceLabel(request.affectedService) : '-' }}</strong>
              </div>
              <div class="field-row">
                <span>Occurrence Time</span>
                <strong>{{ request.occurrenceTime ? formatDateTime(request.occurrenceTime) : '-' }}</strong>
              </div>
              <div class="field-row">
                <span>Resolution Time</span>
                <strong>{{ request.requestedResolutionTime ? formatDateTime(request.requestedResolutionTime) : '-' }}</strong>
              </div>
            </div>
          </div>

          <div class="inspector-section history-section">
            <div class="inspector-title">
              <span>History</span>
              <small>Audit trail</small>
            </div>
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
          </div>
        </section>
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
        You changed the workflow status, team or assignee but have not saved it yet.
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
import { Document } from '@element-plus/icons-vue'
import PriorityTag from '@/components/PriorityTag.vue'
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
const inlineDescriptionImageIds = ref<Set<number>>(new Set())

const isImage = (attachment: RequestAttachment) => {
  const type = attachment.contentType || ''
  const name = attachment.originalName || ''
  return type.startsWith('image/') || /\.(png|jpe?g|gif|bmp|svg|webp)$/i.test(name)
}

const imageAttachments = computed(() =>
  (request.value.attachments || []).filter(isImage)
)

const visibleImageAttachments = computed(() =>
  imageAttachments.value.filter((attachment) => !inlineDescriptionImageIds.value.has(attachment.id))
)

const nonImageAttachments = computed(() =>
  (request.value.attachments || []).filter((a) => !isImage(a))
)

const imagePreviewList = computed(() =>
  visibleImageAttachments.value
    .map((a) => imageUrls.value[a.id])
    .filter(Boolean) as string[]
)

const imagePreviewIndex = (id: number) => {
  const idx = visibleImageAttachments.value.findIndex((a) => a.id === id)
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
const assigneeInitial = computed(() => {
  const name = selectedAssignee.value?.name || request.value.assignee || 'U'
  return name.trim().slice(0, 1).toUpperCase()
})

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
    inlineDescriptionImageIds.value = new Set()
    return
  }
  const template = document.createElement('template')
  template.innerHTML = html
  const images = Array.from(template.content.querySelectorAll<HTMLImageElement>('img[data-attachment-id]'))
  const inlineIds = new Set<number>()
  await Promise.all(images.map(async (image) => {
    const attachmentId = Number(image.dataset.attachmentId)
    if (!attachmentId) return
    inlineIds.add(attachmentId)
    try {
      const blob = await attachmentApi.download(attachmentId)
      const url = URL.createObjectURL(blob)
      inlineBlobUrls.push(url)
      image.src = url
    } catch {
      image.replaceWith(document.createTextNode(`[Image attachment ${attachmentId} unavailable]`))
    }
  }))
  inlineDescriptionImageIds.value = inlineIds
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
  grid-template-columns: minmax(0, 1fr) 380px;
  gap: 16px;
  align-items: start;
}

.detail-main,
.detail-side {
  display: grid;
  gap: 16px;
  min-width: 0;
}

.detail-side {
  position: sticky;
  top: 16px;
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
  min-height: 360px;
  max-height: none;
  overflow: visible;
  padding: 14px;
  border-radius: 8px;
  background: #fff;
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

.request-details-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 16px;
  align-items: start;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
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
  min-height: 56px;
  padding: 10px 12px;
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

.assignment-inline {
  display: grid;
  gap: 12px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8fafc;
}

.assignment-inline__title {
  color: #111827;
  font-size: 13px;
  font-weight: 800;
}

.assignment-inline :deep(.el-form-item) {
  margin-bottom: 0;
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
}

.history-card {
  min-height: 620px;
}

.history-card :deep(.el-card__body) {
  min-height: 540px;
  max-height: calc(100vh - 220px);
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
  .detail-side {
    position: static;
  }
  .request-details-layout {
    grid-template-columns: 1fr;
  }
  .info-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .history-card {
    min-height: auto;
  }
  .history-card :deep(.el-card__body) {
    min-height: auto;
    max-height: none;
    overflow-y: visible;
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

.work-item-header {
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.035);
}

.work-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 20px 12px;
}

.work-title-main {
  min-width: 0;
}

.work-type-line {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #3269a8;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
}

.work-type-line strong {
  color: #475467;
  font-weight: 800;
}

.work-type-dot {
  width: 9px;
  height: 9px;
  border-radius: 999px;
  background: #3269a8;
  box-shadow: 0 0 0 3px rgba(50, 105, 168, 0.12);
}

.work-title-main h1 {
  margin: 8px 0 0;
  color: #1f2937;
  font-size: 24px;
  font-weight: 650;
  line-height: 1.35;
  letter-spacing: 0;
}

.work-header-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-shrink: 0;
}

.work-owner-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px 16px;
}

.owner-avatar {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 999px;
  background: #14532d;
  color: #fff;
  font-size: 15px;
  font-weight: 800;
  box-shadow: 0 0 0 3px rgba(20, 83, 45, 0.1);
  flex-shrink: 0;
}

.owner-editor {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.owner-editor > span {
  color: #667085;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.owner-select {
  width: min(320px, 42vw);
}

.owner-select :deep(.el-select__wrapper),
.state-select :deep(.el-select__wrapper),
.team-select :deep(.el-select__wrapper) {
  min-height: 34px;
  border-radius: 6px;
  background: #fff;
}

.owner-team {
  max-width: 360px;
  overflow: hidden;
  padding: 5px 9px;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  background: #f8fafc;
  color: #667085;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.work-meta-strip {
  display: grid;
  grid-template-columns: minmax(250px, 0.95fr) minmax(280px, 1fr) minmax(150px, 0.7fr) minmax(140px, 0.7fr);
  gap: 18px;
  align-items: center;
  padding: 14px 20px;
  border-top: 1px solid #e5e7eb;
  background: #f5f6f8;
}

.work-meta-field {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.work-meta-field--state {
  grid-template-columns: 44px minmax(0, 230px);
}

.work-meta-field--assignment {
  grid-template-columns: 82px minmax(0, 250px);
}

.meta-label {
  color: #6b7280;
  font-size: 13px;
  font-weight: 600;
}

.meta-value {
  min-width: 0;
  overflow: hidden;
  color: #344054;
  font-size: 13px;
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.state-select,
.team-select {
  width: 100%;
}

.detail-layout {
  grid-template-columns: minmax(0, 1fr) 370px;
  gap: 16px;
}

.detail-main {
  gap: 16px;
}

.detail-side {
  position: sticky;
  top: 16px;
}

.work-section,
.inspector-panel {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.025);
}

.work-section {
  padding: 0 18px 18px;
}

.work-section__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 0 10px;
  margin-bottom: 14px;
  border-bottom: 1px solid #edf0f3;
}

.work-section__header h2 {
  margin: 0;
  color: #111827;
  font-size: 18px;
  font-weight: 750;
  line-height: 1.2;
}

.work-section__header span {
  color: #98a2b3;
  font-size: 12px;
  white-space: nowrap;
}

.description-section {
  min-height: 360px;
}

.description-window {
  min-height: 250px;
  padding: 0;
  border-radius: 0;
}

.rich-description {
  font-size: 14px;
  line-height: 1.7;
}

.rich-description :deep(img) {
  width: min(100%, 560px);
  max-height: 380px;
}

.description-images {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 280px));
  gap: 12px;
  margin-top: 14px;
}

.inline-image {
  width: 100%;
  height: 180px;
  object-fit: contain;
  background: #f8fafc;
}

.inspector-panel {
  overflow: hidden;
}

.inspector-section {
  padding: 16px 18px;
  border-bottom: 1px solid #edf0f3;
}

.inspector-section:last-child {
  border-bottom: none;
}

.inspector-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 14px;
  color: #111827;
  font-size: 18px;
  font-weight: 750;
  line-height: 1.2;
}

.inspector-title small {
  color: #98a2b3;
  font-size: 12px;
  font-weight: 600;
}

.field-list {
  display: grid;
  gap: 14px;
}

.field-row {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.field-row span {
  color: #98a2b3;
  font-size: 12px;
  font-weight: 650;
}

.field-row strong {
  min-width: 0;
  overflow-wrap: anywhere;
  color: #344054;
  font-size: 13px;
  font-weight: 650;
  line-height: 1.45;
}

.history-section {
  min-height: 380px;
}

.history-section .history-timeline {
  max-height: min(520px, calc(100vh - 520px));
  min-height: 300px;
  overflow-y: auto;
  padding: 0 6px 0 4px;
}

.history-section :deep(.el-timeline-item__timestamp) {
  color: #98a2b3;
  font-size: 11px;
}

.comment-item {
  border-radius: 8px;
  background: #fff;
  border-color: #edf0f3;
}

.comment-item:hover {
  background: #f8fafc;
}

@media (max-width: 1180px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .detail-side {
    position: static;
  }

  .work-meta-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .history-section .history-timeline {
    max-height: none;
    overflow-y: visible;
  }
}

@media (max-width: 720px) {
  .work-title-row,
  .work-owner-row,
  .work-section__header,
  .comment-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .work-owner-row {
    align-items: flex-start;
  }

  .owner-editor {
    width: 100%;
    align-items: flex-start;
    flex-direction: column;
  }

  .owner-select {
    width: 100%;
  }

  .work-meta-strip {
    grid-template-columns: 1fr;
  }

  .work-meta-field,
  .work-meta-field--state,
  .work-meta-field--assignment {
    grid-template-columns: 86px minmax(0, 1fr);
  }

  .work-section__header span {
    white-space: normal;
  }
}

.desc-preview-img {
  max-width: 90vw;
  max-height: 85vh;
  object-fit: contain;
  border-radius: 8px;
}
</style>
