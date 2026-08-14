import request from '@/utils/request'
import type {
  AddCommentPayload,
  AssignRequestPayload,
  CreateRequestPayload,
  ListRequestsParams,
  PageResponse,
  RequestComment,
  RequestHistoryItem,
  RequestStatus,
  SetTesterPayload,
  TransitionRequestPayload,
  UpdateRequestDescriptionPayload,
  WorkflowRequest
} from '@/types/requests'

export const requestApi = {
  list(params?: ListRequestsParams): Promise<PageResponse<WorkflowRequest>> {
    return request.get('/requests', {
      params,
      paramsSerializer: {
        serialize: (values) => {
          const searchParams = new URLSearchParams()
          Object.entries(values).forEach(([key, value]) => {
            if (value == null || value === '') return
            if (Array.isArray(value)) {
              value.forEach((item) => {
                if (item != null && item !== '') searchParams.append(key, String(item))
              })
              return
            }
            searchParams.append(key, String(value))
          })
          return searchParams.toString()
        }
      }
    })
  },

  getById(id: number): Promise<WorkflowRequest> {
    return request.get(`/requests/${id}`)
  },

  create(data: CreateRequestPayload): Promise<WorkflowRequest> {
    return request.post('/requests', data)
  },

  updateDescription(id: number, data: UpdateRequestDescriptionPayload): Promise<WorkflowRequest> {
    return request.put(`/requests/${id}/description`, data)
  },

  assign(id: number, data: AssignRequestPayload): Promise<WorkflowRequest> {
    return request.put(`/requests/${id}/assign`, data)
  },

  setTester(id: number, data: SetTesterPayload): Promise<WorkflowRequest> {
    return request.put(`/requests/${id}/tester`, data)
  },

  transition(id: number, data: TransitionRequestPayload): Promise<WorkflowRequest> {
    return request.put(`/requests/${id}/status`, data)
  },

  addComment(id: number, data: AddCommentPayload): Promise<RequestComment> {
    return request.post(`/requests/${id}/comments`, data)
  },

  getHistory(id: number): Promise<RequestHistoryItem[]> {
    return request.get(`/requests/${id}/history`)
  },

  getComments(id: number): Promise<RequestComment[]> {
    return request.get(`/requests/${id}/comments`)
  },

  getStatuses(): Promise<RequestStatus[]> {
    return request.get('/requests/statuses')
  }
}
