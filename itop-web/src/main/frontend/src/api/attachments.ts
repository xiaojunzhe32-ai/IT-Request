import request from '@/utils/request'
import type { RequestAttachment } from '@/types/requests'

export const attachmentApi = {
  upload(file: File, entityType: 'REQUEST' | 'REQUEST_COMMENT', entityId: number, description?: string): Promise<RequestAttachment> {
    const data = new FormData()
    data.append('file', file)
    data.append('entityType', entityType)
    data.append('entityId', String(entityId))
    if (description) data.append('description', description)
    return request.post('/attachments/upload', data)
  },

  download(id: number): Promise<Blob> {
    return request.get(`/attachments/download/${id}`, { responseType: 'blob' })
  }
}
