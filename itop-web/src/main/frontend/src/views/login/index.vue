<template>
  <div class="login-page">
    <section class="login-shell">
      <div class="login-product">
        <div class="brand-row">
          <BrandLogo />
          <div class="brand-intro">
            <strong>IT Request Workflow</strong>
            <span>Request, assignment, testing and closure</span>
          </div>
        </div>

        <div class="workflow-card">
          <p class="section-kicker">Core Flow</p>
          <div class="flow-line">
            <span v-for="status in flowStatuses" :key="status">{{ status }}</span>
          </div>
        </div>

        <div class="role-grid">
          <button
            v-for="account in demoAccounts"
            :key="account.username"
            class="role-card"
            :class="{ active: selectedAccount === account.username }"
            type="button"
            @click="selectAccount(account.username)"
          >
            <el-icon><component :is="account.icon" /></el-icon>
            <span>{{ account.label }}</span>
            <small>{{ account.home }}</small>
          </button>
        </div>
      </div>

      <div class="login-panel">
        <div class="login-card">
          <div class="login-card__header">
            <p>Secure Sign In</p>
            <h1>Welcome back</h1>
            <span>Select a demo role or enter a configured account.</span>
          </div>

          <el-form ref="loginFormRef" :model="loginForm" :rules="rules" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="Username"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="Password"
                :prefix-icon="Lock"
                size="large"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-button
              type="primary"
              size="large"
              class="login-button"
              :loading="loading"
              @click="handleLogin"
            >
              Sign In
            </el-button>
          </el-form>

          <div class="demo-strip">
            <span>Demo password</span>
            <strong>{{ activeAccount.password }}</strong>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Lock, Monitor, Operation, Setting, Tickets, User } from '@element-plus/icons-vue'
import BrandLogo from '@/components/BrandLogo.vue'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const selectedAccount = ref('admin')

const flowStatuses = ['New', 'Assigned', 'In Progress', 'Testing', 'Resolved', 'Closed']

const demoAccounts = [
  {
    username: 'admin',
    password: 'admin123',
    label: 'Admin',
    home: 'Workspace + Admin',
    icon: Setting,
    route: '/workspace/overview'
  },
  {
    username: 'requester01',
    password: 'admin123',
    label: 'Requester',
    home: 'User Portal',
    icon: Tickets,
    route: '/portal'
  },
  {
    username: 'technician01',
    password: 'admin123',
    label: 'Technician',
    home: 'My Tasks',
    icon: Operation,
    route: '/workspace/my-tasks'
  },
  {
    username: 'tester01',
    password: 'admin123',
    label: 'Tester',
    home: 'Test Queue',
    icon: Monitor,
    route: '/workspace/test-queue'
  },
  {
    username: 'lead01',
    password: 'admin123',
    label: 'Team Lead',
    home: 'Team Queue',
    icon: Setting,
    route: '/workspace/team-queue'
  }
]

const loginForm = reactive({
  username: 'admin',
  password: 'admin123'
})

const activeAccount = computed(() =>
  demoAccounts.find((account) => account.username === selectedAccount.value) || demoAccounts[0]
)

const rules: FormRules = {
  username: [{ required: true, message: 'Please enter username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please enter password', trigger: 'blur' }]
}

const selectAccount = (username: string) => {
  selectedAccount.value = username
  const account = demoAccounts.find((item) => item.username === username) || demoAccounts[0]
  loginForm.username = account.username
  loginForm.password = account.password
}

const isBrowserZoomShortcut = (event: KeyboardEvent) => {
  const key = event.key.toLowerCase()
  return (
    (event.ctrlKey || event.metaKey) &&
    (key === '+' || key === '-' || key === '=' || key === '_' || key === '0')
  )
}

const preventWheelZoom = (event: WheelEvent) => {
  if (event.ctrlKey || event.metaKey) {
    event.preventDefault()
  }
}

const preventKeyboardZoom = (event: KeyboardEvent) => {
  if (isBrowserZoomShortcut(event)) {
    event.preventDefault()
  }
}

onMounted(() => {
  window.addEventListener('wheel', preventWheelZoom, { passive: false })
  window.addEventListener('keydown', preventKeyboardZoom)
})

onBeforeUnmount(() => {
  window.removeEventListener('wheel', preventWheelZoom)
  window.removeEventListener('keydown', preventKeyboardZoom)
})

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true

    try {
      const response = await authApi.login({
        username: loginForm.username,
        password: loginForm.password
      })
      userStore.setToken(response.token)
      localStorage.setItem('username', response.username)
      await userStore.fetchCurrentUser()
      const account = demoAccounts.find((item) => item.username === loginForm.username)
      const isRequester = userStore.hasRole('REQUESTER') && !userStore.hasAnyPermission('request:assign', 'request:write')
      ElMessage.success('Signed in')
      router.push(account?.route || (isRequester ? '/portal' : '/workspace/overview'))
    } catch (error) {
      ElMessage.error('Invalid username or password')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  min-height: 100dvh;
  display: grid;
  place-items: center;
  padding: 28px;
  overflow-y: auto;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.7) 0%, rgba(255, 255, 255, 0) 180px),
    #f4f6f8;
}

.login-shell {
  width: min(1160px, 100%);
  min-height: 640px;
  display: grid;
  grid-template-columns: minmax(520px, 1.1fr) minmax(380px, 0.9fr);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 22px 70px rgba(15, 23, 42, 0.12);
}

.login-product {
  display: flex;
  flex-direction: column;
  gap: 26px;
  padding: 42px;
  color: #fff;
  background:
    linear-gradient(180deg, rgba(80, 118, 255, 0.16), rgba(80, 118, 255, 0) 46%),
    linear-gradient(135deg, #101626 0%, #151c38 100%);
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.brand-intro strong,
.brand-intro span {
  display: block;
}

.brand-intro strong {
  font-size: 18px;
}

.brand-intro span,
.role-card small {
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.62);
  font-size: 12px;
}

.workflow-card {
  margin-top: auto;
  padding: 18px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.section-kicker {
  margin: 0 0 14px;
  color: #bec8ff;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}

.flow-line {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.flow-line span {
  padding: 8px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.84);
  font-size: 12px;
  font-weight: 700;
}

.role-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.role-card {
  min-height: 92px;
  display: grid;
  justify-items: start;
  gap: 7px;
  padding: 16px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  cursor: pointer;
  text-align: left;
  transition:
    border-color 0.18s ease,
    background 0.18s ease,
    transform 0.18s ease;
}

.role-card:hover,
.role-card.active {
  border-color: rgba(122, 162, 255, 0.8);
  background: rgba(59, 130, 246, 0.16);
}

.role-card:hover {
  transform: translateY(-1px);
}

.role-card .el-icon {
  color: #9db5ff;
  font-size: 20px;
}

.role-card span {
  font-weight: 800;
}

.login-panel {
  display: grid;
  place-items: center;
  padding: 42px;
  background: #f8fafc;
}

.login-card {
  width: min(100%, 420px);
  padding: 30px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.08);
}

.login-card__header p {
  margin: 0;
  color: #000080;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}

.login-card__header h1 {
  margin: 10px 0 8px;
  color: #111827;
  font-size: 28px;
  line-height: 1.15;
}

.login-card__header span {
  color: #667085;
  font-size: 13px;
}

.login-form {
  margin-top: 26px;
}

.login-button {
  width: 100%;
  border-radius: 10px;
  font-weight: 700;
}

.demo-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #eef2ff;
  color: #000080;
  font-size: 12px;
}

.demo-strip strong {
  color: #111827;
}

@media (max-width: 1080px) {
  .login-page {
    display: block;
    padding: 0;
  }

  .login-shell {
    min-height: 100vh;
    width: 100%;
    grid-template-columns: 1fr;
    border-radius: 0;
  }
}

@media (max-height: 760px) and (min-width: 1081px) {
  .login-page {
    place-items: start center;
  }

  .login-shell {
    min-height: auto;
  }

  .login-product,
  .login-panel {
    padding: 30px;
  }

  .login-product {
    gap: 18px;
  }

  .workflow-card {
    margin-top: 0;
  }

  .role-card {
    min-height: 82px;
    padding: 14px;
  }
}
</style>
