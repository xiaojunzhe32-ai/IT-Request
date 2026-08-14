<template>
  <div class="login-page">
    <section class="login-shell" aria-label="IT request workflow sign in">
      <aside class="brand-panel">
        <div class="brand-panel__top">
          <div class="brand-row">
            <BrandLogo />
            <div class="brand-copy">
              <strong>IT Request Workflow</strong>
              <span>Internal service desk</span>
            </div>
          </div>
          <div class="system-tag">Secure access</div>
        </div>

        <div class="hero-copy">
          <p class="hero-kicker">Enterprise IT Service</p>
          <h1>Handle requests with clear ownership.</h1>
          <p>
            Track request intake, assignment, team handoff, testing and closure in one controlled workspace.
          </p>
        </div>

        <div class="workflow-board" aria-label="Request workflow overview">
          <div class="workflow-board__header">
            <span>Request flow</span>
            <strong>Team routed</strong>
          </div>
          <div class="flow-stack">
            <div v-for="item in flowStatuses" :key="item" class="flow-step">
              <span>{{ item }}</span>
            </div>
          </div>
        </div>

        <div class="capability-grid">
          <div v-for="item in serviceHighlights" :key="item.title" class="capability-card">
            <span>{{ item.title }}</span>
            <strong>{{ item.text }}</strong>
          </div>
        </div>
      </aside>

      <main class="login-panel">
        <div class="login-card">
          <div class="login-card__header">
            <span class="login-eyebrow">Sign in</span>
            <h2>Welcome back</h2>
            <p>Use your internal account to continue.</p>
          </div>

          <el-form ref="loginFormRef" :model="loginForm" :rules="rules" class="login-form" label-position="top">
            <el-form-item label="Username" prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="Enter username"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item label="Password" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="Enter password"
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

          <div class="access-note" aria-label="Login security notes">
            <div v-for="item in accessNotes" :key="item.title">
              <span>{{ item.title }}</span>
              <p>{{ item.text }}</p>
            </div>
          </div>
        </div>
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import BrandLogo from '@/components/BrandLogo.vue'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const flowStatuses = ['New', 'Assigned', 'In Progress', 'To be test', 'Testing', 'Resolved', 'Closed']

const serviceHighlights = [
  { title: 'Team visibility', text: 'Requests stay aligned with owning teams.' },
  { title: 'IT routing', text: 'IT teams receive and process service work.' },
  { title: 'Audit trail', text: 'Every handoff remains easy to follow.' }
]

const accessNotes = [
  { title: 'Protected workspace', text: 'Access is limited to authorized internal users.' },
  { title: 'Role aware entry', text: 'The system opens the right workspace after sign in.' }
]

const loginForm = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: 'Please enter username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please enter password', trigger: 'blur' }]
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
      const isRequester = userStore.hasRole('REQUESTER') && !userStore.hasAnyPermission('request:assign', 'request:write')
      ElMessage.success('Signed in')
      router.push(isRequester ? '/portal' : '/workspace/overview')
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
  display: flex;
  align-items: stretch;
  justify-content: stretch;
  padding: 0;
  overflow: hidden;
  color: #172033;
  background:
    radial-gradient(circle at 18% 14%, rgba(43, 94, 178, 0.2), transparent 32%),
    radial-gradient(circle at 82% 88%, rgba(19, 88, 139, 0.18), transparent 34%),
    linear-gradient(135deg, #eef3f8 0%, #f8fafc 52%, #e8eef5 100%);
}

.login-page::before {
  content: '';
  position: fixed;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(15, 23, 42, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(15, 23, 42, 0.035) 1px, transparent 1px);
  background-size: 44px 44px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.88), transparent 82%);
}

.login-shell {
  position: relative;
  z-index: 1;
  width: 100%;
  min-height: 100dvh;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(500px, 0.62fr);
  overflow: hidden;
  border: 0;
  border-radius: 0;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: none;
}

.brand-panel {
  position: relative;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 34px;
  padding: clamp(42px, 5.2vw, 78px);
  color: #f8fafc;
  overflow: hidden;
  background:
    linear-gradient(155deg, rgba(49, 113, 216, 0.28) 0%, rgba(49, 113, 216, 0) 42%),
    linear-gradient(135deg, #0f172a 0%, #17213a 54%, #0c1725 100%);
}

.brand-panel::before,
.brand-panel::after {
  content: '';
  position: absolute;
  pointer-events: none;
  border-radius: 999px;
}

.brand-panel::before {
  width: 380px;
  height: 380px;
  right: -130px;
  top: -120px;
  background: rgba(59, 130, 246, 0.24);
  filter: blur(10px);
}

.brand-panel::after {
  width: 520px;
  height: 520px;
  left: -260px;
  bottom: -270px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: inset 0 0 90px rgba(59, 130, 246, 0.16);
}

.brand-panel__top,
.hero-copy,
.workflow-board,
.capability-grid {
  position: relative;
  z-index: 1;
}

.brand-panel__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.brand-copy strong,
.brand-copy span {
  display: block;
}

.brand-copy strong {
  color: #ffffff;
  font-size: 18px;
  line-height: 1.2;
  letter-spacing: -0.01em;
}

.brand-copy span {
  margin-top: 5px;
  color: rgba(226, 232, 240, 0.68);
  font-size: 13px;
}

.system-tag {
  flex: 0 0 auto;
  padding: 9px 12px;
  border: 1px solid rgba(191, 219, 254, 0.18);
  border-radius: 999px;
  color: #dbeafe;
  background: rgba(15, 23, 42, 0.34);
  font-size: 12px;
  font-weight: 700;
}

.hero-copy {
  max-width: 650px;
  padding: 34px 0 6px;
}

.hero-kicker,
.login-eyebrow {
  display: inline-flex;
  margin: 0 0 16px;
  color: #93c5fd;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.hero-copy h1 {
  max-width: 700px;
  margin: 0;
  color: #ffffff;
  font-size: clamp(44px, 5vw, 72px);
  line-height: 0.98;
  letter-spacing: -0.055em;
}

.hero-copy p:not(.hero-kicker) {
  max-width: 540px;
  margin: 24px 0 0;
  color: rgba(226, 232, 240, 0.78);
  font-size: 17px;
  line-height: 1.65;
}

.workflow-board {
  width: min(100%, 680px);
  padding: 22px;
  border: 1px solid rgba(191, 219, 254, 0.16);
  border-radius: 22px;
  background: rgba(8, 16, 32, 0.38);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.06);
}

.workflow-board__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  color: rgba(226, 232, 240, 0.68);
  font-size: 13px;
}

.workflow-board__header strong {
  color: #bfdbfe;
  font-size: 13px;
}

.flow-stack {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.flow-step {
  min-height: 54px;
  display: flex;
  align-items: center;
  padding: 0 14px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 14px;
  color: rgba(248, 250, 252, 0.9);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.075), rgba(255, 255, 255, 0.035));
  font-size: 13px;
  font-weight: 750;
}

.capability-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.capability-card {
  min-height: 126px;
  padding: 18px;
  border: 1px solid rgba(191, 219, 254, 0.14);
  border-radius: 20px;
  background: rgba(15, 23, 42, 0.28);
}

.capability-card span {
  display: block;
  margin-bottom: 14px;
  color: rgba(191, 219, 254, 0.78);
  font-size: 12px;
  font-weight: 800;
}

.capability-card strong {
  display: block;
  color: rgba(248, 250, 252, 0.92);
  font-size: 15px;
  line-height: 1.42;
}

.login-panel {
  display: grid;
  place-items: center;
  padding: clamp(42px, 5vw, 72px);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.98)),
    #f8fafc;
}

.login-card {
  width: min(100%, 520px);
  padding: clamp(42px, 4vw, 58px);
  border: 1px solid rgba(30, 41, 59, 0.1);
  border-radius: 28px;
  background: #ffffff;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.12);
}

.login-card__header h2 {
  margin: 0;
  color: #101828;
  font-size: clamp(40px, 3vw, 48px);
  line-height: 1.04;
  letter-spacing: -0.045em;
}

.login-card__header p {
  margin: 12px 0 0;
  color: #667085;
  font-size: 15px;
  line-height: 1.55;
}

.login-eyebrow {
  margin-bottom: 14px;
  color: #1d4ed8;
}

.login-form {
  margin-top: 34px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-form-item__label) {
  margin-bottom: 8px;
  color: #344054;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.2;
}

.login-form :deep(.el-input__wrapper) {
  min-height: 56px;
  padding: 0 17px;
  border-radius: 16px;
  background: #f9fafb;
  box-shadow: inset 0 0 0 1px #d0d5dd;
  transition: background 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  background: #ffffff;
  box-shadow: inset 0 0 0 1px #98a2b3;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  box-shadow: inset 0 0 0 1px #2563eb, 0 0 0 4px rgba(37, 99, 235, 0.12);
}

.login-form :deep(.el-input__inner) {
  color: #101828;
  font-weight: 650;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: #667085;
  font-weight: 500;
}

.login-button {
  width: 100%;
  min-height: 58px;
  margin-top: 8px;
  border: 0;
  border-radius: 16px;
  color: #ffffff;
  background: #1d4ed8;
  font-size: 16px;
  font-weight: 800;
  box-shadow: 0 14px 30px rgba(29, 78, 216, 0.25);
  transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.login-button:hover,
.login-button:focus {
  background: #1e40af;
  box-shadow: 0 16px 34px rgba(29, 78, 216, 0.32);
}

.login-button:active {
  transform: translateY(1px);
  box-shadow: 0 10px 22px rgba(29, 78, 216, 0.24);
}

.access-note {
  display: grid;
  gap: 12px;
  margin-top: 28px;
  padding-top: 24px;
  border-top: 1px solid #eaecf0;
}

.access-note div {
  padding: 14px 16px;
  border: 1px solid #eaecf0;
  border-radius: 16px;
  background: #f9fafb;
}

.access-note span {
  display: block;
  margin-bottom: 5px;
  color: #344054;
  font-size: 13px;
  font-weight: 800;
}

.access-note p {
  margin: 0;
  color: #667085;
  font-size: 13px;
  line-height: 1.5;
}

@media (prefers-reduced-motion: no-preference) {
  .login-shell {
    animation: login-shell-enter 0.52s cubic-bezier(0.16, 1, 0.3, 1) both;
  }

  .hero-copy,
  .workflow-board,
  .capability-grid,
  .login-card {
    animation: login-content-enter 0.58s cubic-bezier(0.16, 1, 0.3, 1) both;
  }

  .workflow-board {
    animation-delay: 0.08s;
  }

  .capability-grid,
  .login-card {
    animation-delay: 0.14s;
  }
}

@keyframes login-shell-enter {
  from {
    opacity: 0;
    transform: translateY(16px) scale(0.985);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes login-content-enter {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1180px) {
  .login-page {
    align-items: flex-start;
    padding: 0;
    overflow-y: auto;
  }

  .login-shell {
    width: 100%;
    min-height: 100dvh;
    grid-template-columns: 1fr;
    border: 0;
    border-radius: 0;
  }

  .brand-panel {
    min-height: auto;
    padding: 32px;
  }

  .login-panel {
    padding: 32px;
  }

  .hero-copy {
    padding-top: 30px;
  }

  .hero-copy h1 {
    max-width: 760px;
    font-size: clamp(40px, 8vw, 64px);
  }
}

@media (max-width: 760px) {
  .brand-panel,
  .login-panel {
    padding: 24px 18px;
  }

  .brand-panel__top {
    align-items: flex-start;
    flex-direction: column;
  }

  .system-tag {
    align-self: flex-start;
  }

  .hero-copy h1 {
    font-size: 40px;
    line-height: 1.02;
  }

  .hero-copy p:not(.hero-kicker) {
    font-size: 15px;
  }

  .workflow-board {
    padding: 18px;
  }

  .flow-stack,
  .capability-grid {
    grid-template-columns: 1fr;
  }

  .capability-card {
    min-height: auto;
  }

  .login-card {
    padding: 26px;
    border-radius: 22px;
  }

  .login-card__header h2 {
    font-size: 32px;
  }
}

@media (max-height: 760px) and (min-width: 1181px) {
  .login-page {
    align-items: flex-start;
    overflow-y: auto;
  }

  .login-shell {
    min-height: auto;
  }

  .brand-panel,
  .login-panel {
    padding: 30px;
  }

  .hero-copy {
    padding-top: 16px;
  }

  .hero-copy h1 {
    font-size: clamp(40px, 4.4vw, 60px);
  }

  .hero-copy p:not(.hero-kicker) {
    margin-top: 18px;
  }

  .capability-card {
    min-height: 104px;
  }

  .login-card {
    padding: 32px;
  }
}
</style>
