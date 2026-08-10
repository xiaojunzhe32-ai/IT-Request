import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

interface RequestInstance extends Omit<AxiosInstance, 'request' | 'get' | 'delete' | 'head' | 'options' | 'post' | 'put' | 'patch'> {
  request<T = any>(config: AxiosRequestConfig): Promise<T>
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
  head<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
  options<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>
  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>
}

const axiosService: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

axiosService.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    return config
  },
  (error) => Promise.reject(error)
)

axiosService.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res && typeof res === 'object' && 'success' in res) {
      if (!res.success) {
        ElMessage.error(res.message || 'Request failed')
        return Promise.reject(new Error(res.message || 'Request failed'))
      }
      return res.data
    }
    return res
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        ElMessage.error('Session expired. Please sign in again.')
        localStorage.removeItem('token')
        window.location.href = '/login'
      } else if (status === 403) {
        ElMessage.error('Access denied')
      } else if (status === 404) {
        ElMessage.error('Resource not found')
      } else if (status >= 500) {
        ElMessage.error('Server error')
      } else {
        ElMessage.error(error.message || 'Request failed')
      }
    } else {
      ElMessage.error('Network error. Please check the connection.')
    }

    return Promise.reject(error)
  }
)

const service = axiosService as RequestInstance

export default service
