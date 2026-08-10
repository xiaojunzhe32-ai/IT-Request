import request from '@/utils/request'
import type { LoginRequest, JwtResponse } from '@/types/auth'

export const authApi = {
  login(data: LoginRequest): Promise<JwtResponse> {
    return request.post('/auth/login', data)
  },

  logout(): Promise<void> {
    return request.post('/auth/logout')
  }
}