<template>
  <el-container class="main-layout">
    <el-aside :width="isCollapse ? '68px' : '232px'" class="app-sidebar">
      <div class="brand-lockup" :class="{ collapsed: isCollapse }">
        <BrandLogo subtitle="Operations Console" :compact="isCollapse" />
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="sidebar-menu"
      >
        <div v-show="!isCollapse" class="menu-section">IT Workspace</div>
        <el-menu-item index="/workspace/overview" v-if="canShowMenu('request:read')">
          <el-icon><Odometer /></el-icon>
          <span>Overview</span>
        </el-menu-item>
        <el-menu-item index="/workspace/team-queue" v-if="canShowMenu('request:read')">
          <el-icon><Tickets /></el-icon>
          <span>Team Queue</span>
        </el-menu-item>
        <el-menu-item index="/workspace/my-tasks" v-if="canShowMenu('request:read')">
          <el-icon><Operation /></el-icon>
          <span>My Tasks</span>
        </el-menu-item>
        <el-menu-item index="/workspace/test-queue" v-if="canShowMenu('request:read')">
          <el-icon><Monitor /></el-icon>
          <span>Test Queue</span>
        </el-menu-item>
        <el-menu-item index="/workspace/assignment" v-if="canShowMenu('request:assign')">
          <el-icon><Connection /></el-icon>
          <span>Assignment</span>
        </el-menu-item>

        <div v-if="userStore.isAdmin" v-show="!isCollapse" class="menu-section">Admin Console</div>
        <el-menu-item index="/dashboard" v-if="userStore.isAdmin">
          <el-icon><Odometer /></el-icon>
          <span>Admin Overview</span>
        </el-menu-item>
        <el-menu-item index="/requests" v-if="userStore.isAdmin">
          <el-icon><Tickets /></el-icon>
          <span>Requests</span>
        </el-menu-item>
        <el-menu-item index="/organizations" v-if="userStore.isAdmin">
          <el-icon><OfficeBuilding /></el-icon>
          <span>Organizations</span>
        </el-menu-item>
        <el-menu-item index="/users" v-if="userStore.isAdmin">
          <el-icon><User /></el-icon>
          <span>Users</span>
        </el-menu-item>
        <el-menu-item index="/roles" v-if="userStore.isAdmin">
          <el-icon><Key /></el-icon>
          <span>Roles</span>
        </el-menu-item>
        <el-menu-item index="/permissions" v-if="userStore.isAdmin">
          <el-icon><Lock /></el-icon>
          <span>Permissions</span>
        </el-menu-item>
        <el-menu-item index="/teams" v-if="userStore.isAdmin">
          <el-icon><UserFilled /></el-icon>
          <span>Teams</span>
        </el-menu-item>
        <el-menu-item index="/routing-rules" v-if="userStore.isAdmin">
          <el-icon><Connection /></el-icon>
          <span>Routing Rules</span>
        </el-menu-item>
        <el-menu-item index="/audit-logs" v-if="userStore.isAdmin">
          <el-icon><Document /></el-icon>
          <span>Audit Logs</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="app-main-shell">
      <el-header class="app-header">
        <div class="header-left">
          <el-button class="collapse-button" text @click="toggleCollapse">
            <el-icon><component :is="isCollapse ? Expand : Fold" /></el-icon>
          </el-button>
          <div class="page-context">
            <span>{{ sectionTitle }}</span>
            <strong>{{ pageTitle }}</strong>
          </div>
        </div>

        <div class="header-right">
          <div class="environment-chip">Local Demo</div>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <span class="user-avatar">{{ userInitial }}</span>
              <span>{{ username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="portal">Open Portal</el-dropdown-item>
                <el-dropdown-item command="profile">Profile</el-dropdown-item>
                <el-dropdown-item command="logout" divided>Logout</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="app-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  Connection,
  Document,
  Expand,
  Fold,
  Key,
  Lock,
  Monitor,
  Odometer,
  OfficeBuilding,
  Operation,
  Tickets,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import BrandLogo from '@/components/BrandLogo.vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)
const username = ref(localStorage.getItem('username') || 'Admin')

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => String(route.meta.title || 'Workflow Overview'))
const sectionTitle = computed(() => route.path.startsWith('/workspace') ? 'IT Workspace' : 'Admin Console')
const userInitial = computed(() => username.value.slice(0, 1).toUpperCase())

onMounted(async () => {
  await userStore.fetchCurrentUser()
})

const canShowMenu = (permission?: string) => {
  if (!permission) return true
  return userStore.hasPermission(permission)
}

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command: string) => {
  if (command === 'portal') {
    router.push('/portal')
    return
  }

  if (command === 'logout') {
    ElMessageBox.confirm('Are you sure you want to logout?', 'Confirm', {
      confirmButtonText: 'Logout',
      cancelButtonText: 'Cancel',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    })
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.main-layout {
  height: 100vh;
  min-width: 1024px;
  background: $bg-color;
}

.app-sidebar {
  position: relative;
  color: #fff;
  background: linear-gradient(180deg, $sidebar-bg 0%, $sidebar-bg-2 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 8px 0 24px rgba(15, 23, 42, 0.08);
}

.brand-lockup {
  height: 78px;
  display: flex;
  align-items: center;
  padding: 0 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-lockup.collapsed {
  justify-content: center;
  padding: 0;
}

.sidebar-menu {
  padding: 12px 8px;
  background: transparent;
  border: 0;
}

.menu-section {
  padding: 14px 12px 8px;
  color: rgba(255, 255, 255, 0.42);
  font-size: 11px;
  font-weight: 800;
  text-transform: uppercase;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  height: 42px;
  line-height: 42px;
  margin: 3px 0;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 13px;
  font-weight: 700;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: rgba(59, 130, 246, 0.18);
  box-shadow: inset 3px 0 0 #7aa2ff;
}

.sidebar-menu :deep(.el-icon) {
  font-size: 17px;
}

.app-main-shell {
  min-width: 0;
}

.app-header {
  height: $header-height;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
  background: rgba(255, 255, 255, 0.96);
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.header-left,
.header-right,
.user-info {
  display: flex;
  align-items: center;
}

.header-left,
.header-right {
  gap: 12px;
}

.collapse-button {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  color: #475467;
}

.page-context span,
.page-context strong {
  display: block;
}

.page-context span {
  color: #98a2b3;
  font-size: 11px;
  font-weight: 800;
  text-transform: uppercase;
}

.page-context strong {
  margin-top: 2px;
  color: #111827;
  font-size: 15px;
}

.environment-chip {
  padding: 7px 10px;
  border-radius: 999px;
  background: #eef2ff;
  color: #000080;
  border: 1px solid #c7d2fe;
  font-size: 12px;
  font-weight: 800;
}

.user-info {
  gap: 8px;
  cursor: pointer;
  padding: 6px 8px 6px 6px;
  border-radius: 999px;
  color: #344054;
  font-size: 13px;
  font-weight: 700;
}

.user-info:hover {
  background: #f8fafc;
}

.user-avatar {
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border-radius: 999px;
  background: #1f2937;
  color: #fff;
  font-size: 12px;
}

.app-content {
  overflow: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
