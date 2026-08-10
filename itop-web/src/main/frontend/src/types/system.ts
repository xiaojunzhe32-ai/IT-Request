import type { PageResponse } from '@/types/requests'

export interface Team {
  id: number
  name: string
  organizationId?: number
  organizationName?: string
  teamCode?: string
  teamType?: string
  leaderId?: number
  leaderName?: string
  memberIds?: number[]
  memberNames?: string[]
  email?: string
  phone?: string
  status?: string
  createdAt?: string
  updatedAt?: string
}

export interface SystemUser {
  id: number
  username: string
  email: string
  firstName?: string
  lastName?: string
  organizationId?: number
  organizationName?: string
  firstName?: string
  lastName?: string
  phone?: string
  authMethod?: string
  status?: string
  locked?: boolean
  failedLogins?: number
  roleIds?: number[]
  roleCodes?: string[]
  teamNames?: string[]
  createdAt?: string
  updatedAt?: string
  lastLogin?: string
}

export interface SystemRole {
  id: number
  name: string
  roleCode: string
  description?: string
  status?: string
  isSystem?: boolean
  permissions?: string[]
  createdAt?: string
  updatedAt?: string
}

export interface RoutingRule {
  id: number
  name: string
  description?: string
  organizationId?: number
  organizationName?: string
  requestType?: string
  priority?: string
  teamId?: number
  teamName?: string
  enabled?: boolean
  sortOrder?: number
  isFallback?: boolean
}

export interface AuditLog {
  id: number
  entityType?: string
  entityId?: number
  action?: string
  fieldName?: string
  oldValue?: string
  newValue?: string
  userId?: number
  username?: string
  ipAddress?: string
  description?: string
  createdAt?: string
}

export interface DashboardStats {
  totalOrgs?: number
  activeUsers?: number
  activeTeams?: number
  enabledRoutingRules?: number
  totalTickets?: number
  newTickets?: number
  assignedTickets?: number
  inProgressTickets?: number
  testingTickets?: number
  resolvedTickets?: number
  userTestFailedTickets?: number
  closedTickets?: number
  recentAuditLogs?: Array<{
    id: number
    entityType?: string
    entityId?: number
    action?: string
    username?: string
    description?: string
    createdAt?: string
  }>
}

export type SystemPageResponse<T> = PageResponse<T>
