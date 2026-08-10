import request from '@/utils/request'
import type { Organization, PageResponse } from '@/types/cmdb'

export const organizationApi = {
  getList(params?: {
    page?: number
    size?: number
    sort?: string
  }): Promise<PageResponse<Organization>> {
    return request.get('/organizations', { params })
  },

  getById(id: number): Promise<Organization> {
    return request.get(`/organizations/${id}`)
  },

  create(data: Partial<Organization>): Promise<Organization> {
    return request.post('/organizations', data)
  },

  update(id: number, data: Partial<Organization>): Promise<Organization> {
    return request.put(`/organizations/${id}`, data)
  },

  delete(id: number): Promise<void> {
    return request.delete(`/organizations/${id}`)
  }
}
