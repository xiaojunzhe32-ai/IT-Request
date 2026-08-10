import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

interface User {
  id: number
  username: string
  email?: string
  firstName?: string
  lastName?: string
  organizationId?: number
  organizationName?: string
  roles?: string[]
  permissions?: string[]
  globalAccess?: boolean
}

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))

  const setUser = (userData: User) => {
    user.value = userData
    if (userData.id) {
      localStorage.setItem('userId', String(userData.id))
    }
    localStorage.setItem('username', userData.username)
    localStorage.setItem('userRoles', JSON.stringify(userData.roles || []))
    localStorage.setItem('userPermissions', JSON.stringify(userData.permissions || []))
    if (userData.organizationId) {
      localStorage.setItem('organizationId', String(userData.organizationId))
    }
    if (userData.organizationName) {
      localStorage.setItem('organizationName', userData.organizationName)
    }
  }

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('userId')
    localStorage.removeItem('userRoles')
    localStorage.removeItem('userPermissions')
    localStorage.removeItem('organizationId')
    localStorage.removeItem('organizationName')
  }

  const loadUserFromStorage = () => {
    const userId = localStorage.getItem('userId')
    const username = localStorage.getItem('username')
    if (userId && username) {
      user.value = {
        id: Number(userId),
        username: username,
        organizationId: Number(localStorage.getItem('organizationId')) || undefined,
        organizationName: localStorage.getItem('organizationName') || undefined,
        roles: JSON.parse(localStorage.getItem('userRoles') || '[]'),
        permissions: JSON.parse(localStorage.getItem('userPermissions') || '[]')
      }
    }
  }

  const fetchCurrentUser = async () => {
    try {
      const res = await request.get('/auth/me')
      if (res) {
        setUser({
          id: res.id,
          username: res.username,
          email: res.email,
          firstName: res.firstName,
          lastName: res.lastName,
          organizationId: res.organizationId,
          organizationName: res.organizationName,
          roles: res.roles || [],
          permissions: res.permissions || [],
          globalAccess: res.globalAccess
        })
      }
    } catch (error) {
      console.error('Failed to fetch current user:', error)
      loadUserFromStorage()
    }
  }

  const hasPermission = (permission: string): boolean => {
    if (!user.value?.permissions) return false
    if (user.value.permissions.includes('*')) return true
    if (user.value.permissions.includes(permission)) return true
    const wildcardMatch = user.value.permissions.find(p => p.endsWith(':*') && permission.startsWith(p.slice(0, -1)))
    return !!wildcardMatch
  }

  const hasAnyPermission = (...permissions: string[]): boolean => {
    return permissions.some(p => hasPermission(p))
  }

  const hasRole = (roleCode: string): boolean => {
    if (!user.value?.roles) return false
    return user.value.roles.includes(roleCode) || user.value.roles.includes('ADMIN')
  }

  const isAdmin = computed(() => {
    return user.value?.roles?.includes('ADMIN') || user.value?.permissions?.includes('*') || false
  })

  return {
    user,
    token,
    setUser,
    setToken,
    logout,
    loadUserFromStorage,
    fetchCurrentUser,
    hasPermission,
    hasAnyPermission,
    hasRole,
    isAdmin
  }
})
