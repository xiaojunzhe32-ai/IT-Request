<template>
  <div class="user-portal-layout">
    <header class="portal-header">
      <div class="brand-row">
        <div class="brand-mark">IT</div>
        <div class="brand-copy">
          <strong>IT Request Portal</strong>
          <span>User request workspace</span>
        </div>
      </div>

      <nav class="portal-nav">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path" class="nav-item">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <el-dropdown @command="handleCommand">
        <span class="user-pill">
          <span class="avatar">{{ userInitial }}</span>
          {{ username }}
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">Profile</el-dropdown-item>
            <el-dropdown-item command="logout" divided>Logout</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </header>

    <main class="portal-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ArrowDown, CircleCheck, HomeFilled, Plus, Tickets, Timer } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const username = ref(localStorage.getItem('username') || 'User')
const userInitial = computed(() => username.value.slice(0, 1).toUpperCase())

const navItems = [
  { path: '/portal', label: 'Home', icon: HomeFilled },
  { path: '/portal/new-request', label: 'New Request', icon: Plus },
  { path: '/portal/requests', label: 'All Requests', icon: Tickets },
  { path: '/portal/requests/ongoing', label: 'Ongoing', icon: Timer },
  { path: '/portal/requests/closed', label: 'Closed', icon: CircleCheck }
]

const handleCommand = (command: string) => {
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
.user-portal-layout {
  min-height: 100vh;
  min-width: 0;
  background: #f4f6f8;
}

.portal-header {
  position: sticky;
  top: 0;
  z-index: 20;
  height: 64px;
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr) auto;
  align-items: center;
  gap: 18px;
  padding: 0 28px;
  background: #1c2430;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-row,
.portal-nav,
.user-pill {
  display: flex;
  align-items: center;
}

.brand-row {
  gap: 12px;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 9px;
  background: #d97706;
  font-weight: 800;
}

.brand-copy strong,
.brand-copy span {
  display: block;
}

.brand-copy strong {
  font-size: 14px;
}

.brand-copy span {
  margin-top: 2px;
  color: rgba(255, 255, 255, 0.56);
  font-size: 11px;
}

.portal-nav {
  gap: 6px;
  overflow-x: auto;
  scrollbar-width: none;
}

.portal-nav::-webkit-scrollbar {
  display: none;
}

.nav-item {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 38px;
  padding: 0 12px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.74);
  font-size: 13px;
  font-weight: 700;
}

.nav-item:hover,
.nav-item.router-link-active {
  color: #fff;
  background: rgba(217, 119, 6, 0.2);
}

.user-pill {
  gap: 8px;
  cursor: pointer;
  padding: 6px 10px 6px 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
}

.avatar {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  border-radius: 999px;
  background: #d97706;
}

.portal-main {
  padding: 20px 28px 28px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 1080px) {
  .portal-header {
    grid-template-columns: minmax(0, 1fr) auto;
    height: auto;
    padding: 14px;
  }

  .portal-nav {
    grid-column: 1 / -1;
    order: 3;
    padding-bottom: 2px;
  }

  .nav-item {
    flex: 0 0 auto;
  }
}

@media (max-width: 520px) {
  .portal-header {
    gap: 12px;
  }

  .brand-copy span {
    display: none;
  }

  .portal-main {
    padding: 14px;
  }

  .user-pill {
    max-width: 132px;
    overflow: hidden;
  }
}
</style>
