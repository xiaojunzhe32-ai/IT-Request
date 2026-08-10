export type RequestStatus =
  | 'New'
  | 'Assigned'
  | 'In Progress'
  | 'Testing'
  | 'Resolved'
  | 'User Test Failed'
  | 'Closed'

export type RequestPriority = 'Low' | 'Medium' | 'High' | 'Critical'

export interface PageResponse<T> {
  content: T[]
  pageNumber: number
  pageSize: number
  totalElements: number
  totalPages: number
  last: boolean
}

export interface RequestComment {
  id: number
  author: string
  role: string
  time: string
  message: string
  internal?: boolean
  attachments?: RequestAttachment[]
}

export interface RequestHistoryItem {
  id: number
  time: string
  actor: string
  action: string
  detail: string
  internal?: boolean
}

export interface RequestAttachment {
  id: number
  entityType?: string
  entityId?: number
  fileName?: string
  originalName: string
  fileSize: number
  contentType?: string
  description?: string
  uploaderId?: number
  uploaderName?: string
  createdAt?: string
}

export interface WorkflowRequest {
  id: number
  requestNo: string
  title: string
  description: string
  descriptionHtml?: string
  type: string
  affectedService?: string
  priority: RequestPriority
  status: RequestStatus
  origin?: string
  requester: string
  requesterOrg: string
  assignedTeam: string
  assignee: string
  tester: string
  organizationId?: number
  callerId?: number
  agentId?: number
  teamId?: number
  testerId?: number
  createdAt: string
  updatedAt: string
  startDate?: string
  lastUpdateDate?: string
  submittedToTestingAt?: string
  resolvedAt?: string
  closedAt?: string
  occurrenceTime?: string
  requestedResolutionTime?: string
  ttoDeadline?: string
  ttrDeadline?: string
  slaId?: number
  attachments?: RequestAttachment[]
  comments: RequestComment[]
  history: RequestHistoryItem[]
}

export interface CreateRequestPayload {
  title: string
  description: string
  descriptionHtml?: string
  type: string
  affectedService: string
  priority: RequestPriority
  organizationId: number
  occurrenceTime?: string
  requestedResolutionTime?: string
  origin?: string
}

export interface UpdateRequestDescriptionPayload {
  description: string
  descriptionHtml?: string
}

export interface ListRequestsParams {
  page?: number
  size?: number
  status?: RequestStatus | string
  type?: string
  teamId?: number
  priority?: RequestPriority | string
  orgId?: number
  search?: string
  assigneeId?: number
  callerId?: number
}

export interface AssignRequestPayload {
  teamId?: number
  agentId?: number
}

export interface SetTesterPayload {
  testerId?: number
}

export interface TransitionRequestPayload {
  status: RequestStatus
  note?: string
}

export interface AddCommentPayload {
  message: string
  internal?: boolean
}
