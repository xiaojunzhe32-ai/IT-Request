export interface BaseEntity {
  id: number
  name: string
  status: string
  description?: string
  createdAt: string
  updatedAt: string
}

export interface Organization extends BaseEntity {
  code?: string
  parentId?: number
  type?: string
  address?: string
  phone?: string
  email?: string
  website?: string
}

export interface Server extends BaseEntity {
  organizationId: number
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
  move2Production?: string
  obsolescenceDate?: string
  businessCriticity?: string
}

export interface PageResponse<T> {
  content: T[]
  pageNumber: number
  pageSize: number
  totalElements: number
  totalPages: number
  last: boolean
}