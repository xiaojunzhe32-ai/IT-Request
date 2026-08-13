import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: 'Login', requiresAuth: false }
  },
  {
    path: '/portal',
    component: () => import('@/layouts/UserPortalLayout.vue'),
    meta: { title: 'Portal', requiresAuth: true },
    children: [
      {
        path: '',
        name: 'PortalHome',
        component: () => import('@/views/portal/PortalHome.vue'),
        meta: { title: 'Home' }
      },
      {
        path: 'new-request',
        name: 'PortalNewRequest',
        component: () => import('@/views/portal/NewRequest.vue'),
        meta: { title: 'New Request' }
      },
      {
        path: 'requests',
        name: 'PortalRequests',
        component: () => import('@/views/portal/PortalRequests.vue'),
        meta: { title: 'All Requests' }
      },
      {
        path: 'requests/ongoing',
        name: 'PortalOngoingRequests',
        component: () => import('@/views/portal/PortalRequests.vue'),
        meta: { title: 'Ongoing Requests' }
      },
      {
        path: 'requests/closed',
        name: 'PortalClosedRequests',
        component: () => import('@/views/portal/PortalRequests.vue'),
        meta: { title: 'Closed Requests' }
      },
      {
        path: 'requests/:id',
        name: 'PortalRequestDetail',
        component: () => import('@/views/requests/RequestDetail.vue'),
        meta: { title: 'Request Detail' }
      }
    ]
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/workspace/overview',
    children: [
      {
        path: 'workspace/overview',
        name: 'WorkspaceOverview',
        component: () => import('@/views/workspace/Overview.vue'),
        meta: { title: 'Workflow Overview', icon: 'Odometer', permission: 'request:read' }
      },
      {
        path: 'workspace/team-queue',
        name: 'TeamQueue',
        component: () => import('@/views/workspace/TeamQueue.vue'),
        meta: { title: 'Team Queue', icon: 'Tickets', permission: 'request:read' }
      },
      {
        path: 'workspace/my-tasks',
        name: 'MyTasks',
        component: () => import('@/views/workspace/MyTasks.vue'),
        meta: { title: 'My Tasks', icon: 'Operation', permission: 'request:read' }
      },
      {
        path: 'workspace/assignment',
        name: 'AssignmentDesk',
        component: () => import('@/views/workspace/Assignment.vue'),
        meta: { title: 'Assignment Desk', icon: 'Connection', permission: 'request:assign' }
      },
      {
        path: 'workspace/requests/:id',
        name: 'WorkspaceRequestDetail',
        component: () => import('@/views/requests/RequestDetail.vue'),
        meta: { title: 'Request Detail', icon: 'Tickets', permission: 'request:read' }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: 'Dashboard', icon: 'Odometer', adminOnly: true }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/system/UserList.vue'),
        meta: { title: 'Users', icon: 'User', adminOnly: true }
      },
      {
        path: 'teams',
        name: 'Teams',
        component: () => import('@/views/system/TeamList.vue'),
        meta: { title: 'Teams', icon: 'UserFilled', adminOnly: true }
      },
      {
        path: 'code-tables',
        name: 'CodeTables',
        component: () => import('@/views/system/CodeTableList.vue'),
        meta: { title: 'Code Tables', icon: 'Collection', adminOnly: true }
      },
      {
        path: 'code-tables/roles',
        name: 'Roles',
        component: () => import('@/views/system/RoleList.vue'),
        meta: { title: 'Roles', icon: 'Key', adminOnly: true }
      },
      {
        path: 'code-tables/permissions',
        name: 'Permissions',
        component: () => import('@/views/system/PermissionList.vue'),
        meta: { title: 'Permissions', icon: 'Lock', adminOnly: true }
      },
      {
        path: 'code-tables/routing-rules',
        name: 'RoutingRules',
        component: () => import('@/views/system/RoutingRules.vue'),
        meta: { title: 'Routing Rules', icon: 'Connection', adminOnly: true }
      },
      {
        path: 'requests',
        name: 'AllRequests',
        component: () => import('@/views/system/RequestList.vue'),
        meta: { title: 'All Requests', icon: 'Tickets', adminOnly: true }
      },
      {
        path: 'audit-logs',
        name: 'AuditLogs',
        component: () => import('@/views/system/AuditLogList.vue'),
        meta: { title: 'Audit Logs', icon: 'Document', adminOnly: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  const userStore = useUserStore()

  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next(localStorage.getItem('userRoles')?.includes('REQUESTER') ? '/portal' : '/workspace/overview')
  } else if (to.meta.adminOnly) {
    if (!userStore.user) {
      await userStore.fetchCurrentUser()
    }
    if (userStore.isAdmin) {
      next()
    } else {
      next('/workspace/overview')
    }
  } else if (to.meta.permission) {
    if (!userStore.user) {
      await userStore.fetchCurrentUser()
    }
    if (userStore.hasPermission(to.meta.permission as string)) {
      next()
    } else {
      next(localStorage.getItem('userRoles')?.includes('REQUESTER') ? '/portal' : '/workspace/overview')
    }
  } else {
    next()
  }
})

export default router

