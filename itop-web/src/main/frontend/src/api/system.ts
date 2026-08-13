import request from '@/utils/request'
import type {
  AuditLog,
  CodeTableCode,
  CodeTableItem,
  DashboardStats,
  RoutingRule,
  SystemPageResponse,
  SystemRole,
  SystemUser,
  Team
} from '@/types/system'

export type CodeTableItemInput = {
  tableCode?: CodeTableCode | string
  code?: string
  name?: string
  status?: string
  description?: string
  sortOrder?: number
}

export const teamApi = {
  list(params?: {
    page?: number
    size?: number
    sort?: string
    type?: string
  }): Promise<SystemPageResponse<Team>> {
    return request.get('/teams', { params })
  },

  getById(id: number): Promise<Team> {
    return request.get(`/teams/${id}`)
  },

  create(data: Partial<Team>): Promise<Team> {
    return request.post('/teams', data)
  },

  update(id: number, data: Partial<Team>): Promise<Team> {
    return request.put(`/teams/${id}`, data)
  },

  delete(id: number): Promise<void> {
    return request.delete(`/teams/${id}`)
  }
}

export const userApi = {
  list(params?: {
    page?: number
    size?: number
    sort?: string
    search?: string
    status?: string
  }): Promise<SystemPageResponse<SystemUser>> {
    return request.get('/users', { params })
  },

  create(data: Record<string, unknown>): Promise<SystemUser> {
    return request.post('/users', data)
  },

  update(id: number, data: Record<string, unknown>): Promise<SystemUser> {
    return request.put(`/users/${id}`, data)
  },

  setStatus(id: number, status: 'active' | 'inactive'): Promise<SystemUser> {
    return request.patch(`/users/${id}/status`, { status })
  },

  delete(id: number): Promise<void> {
    return request.delete(`/users/${id}`)
  }
}

export const roleApi = {
  list(params?: { page?: number; size?: number; sort?: string }): Promise<SystemPageResponse<SystemRole>> {
    return request.get('/roles', { params })
  },

  permissions(): Promise<string[]> {
    return request.get('/roles/permissions')
  },

  create(data: Partial<SystemRole>): Promise<SystemRole> {
    return request.post('/roles', data)
  },

  update(id: number, data: Partial<SystemRole>): Promise<SystemRole> {
    return request.put(`/roles/${id}`, data)
  },

  delete(id: number): Promise<void> {
    return request.delete(`/roles/${id}`)
  }
}

export const routingRuleApi = {
  list(): Promise<RoutingRule[]> {
    return request.get('/routing-rules')
  },

  suggest(params: { organizationId?: number; requestType?: string; priority?: string; affectedService?: string }): Promise<{ teamId: number; teamName: string } | null> {
    return request.get('/routing-rules/suggest', { params })
  },

  create(data: Partial<RoutingRule>): Promise<RoutingRule> {
    return request.post('/routing-rules', data)
  },

  update(id: number, data: Partial<RoutingRule>): Promise<RoutingRule> {
    return request.put(`/routing-rules/${id}`, data)
  },

  setEnabled(id: number, enabled: boolean): Promise<RoutingRule> {
    return request.patch(`/routing-rules/${id}/enabled`, { enabled })
  },

  reorder(orderedIds: number[]): Promise<void> {
    return request.put('/routing-rules/reorder', { orderedIds })
  },

  delete(id: number): Promise<void> {
    return request.delete(`/routing-rules/${id}`)
  }
}

export const codeTableApi = {
  list(tableCode: CodeTableCode, status?: string): Promise<CodeTableItem[]> {
    return request.get(`/code-tables/${tableCode}/items`, {
      params: status ? { status } : undefined
    })
  },

  create(tableCode: CodeTableCode, data: CodeTableItemInput): Promise<CodeTableItem> {
    return request.post(`/code-tables/${tableCode}/items`, { ...data, tableCode })
  },

  update(tableCode: CodeTableCode, id: number, data: CodeTableItemInput): Promise<CodeTableItem> {
    return request.put(`/code-tables/${tableCode}/items/${id}`, { ...data, tableCode })
  },

  delete(tableCode: CodeTableCode, id: number): Promise<void> {
    return request.delete(`/code-tables/${tableCode}/items/${id}`)
  }
}

export const auditLogApi = {
  list(params?: {
    page?: number
    size?: number
    entityType?: string
    entityId?: number
    action?: string
    userId?: number
    startDate?: string
    endDate?: string
  }): Promise<SystemPageResponse<AuditLog>> {
    return request.get('/audit-logs', { params })
  }
}

export const dashboardApi = {
  stats(): Promise<DashboardStats> {
    return request.get('/dashboard/stats')
  }
}
