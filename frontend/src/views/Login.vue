<template>
  <div class="login-container">
    <!-- 背景效果 -->
    <div class="bg-effects">
      <div class="grid-lines"></div>
      <div class="glow-orb orb-1"></div>
      <div class="glow-orb orb-2"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <div class="login-header">
        <div class="logo">
          <el-icon class="logo-icon"><Monitor /></el-icon>
          <span class="logo-text">研发作业平台</span>
        </div>
        <p class="subtitle">面向研发人员的生产环境问题定位工具</p>
      </div>

      <div class="login-tabs">
        <div
          :class="['tab', { active: activeTab === 'login' }]"
          @click="activeTab = 'login'"
        >
          登录
        </div>
        <div
          :class="['tab', { active: activeTab === 'register' }]"
          @click="activeTab = 'register'"
        >
          注册
        </div>
      </div>

      <!-- 登录表单 -->
      <el-form
        v-if="activeTab === 'login'"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          class="submit-btn"
          :loading="loading"
          @click="handleLogin"
        >
          登录
        </el-button>
      </el-form>

      <!-- 注册表单 -->
      <el-form
        v-else
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="login-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="邮箱"
            size="large"
            :prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          class="submit-btn"
          :loading="loading"
          @click="handleRegister"
        >
          注册
        </el-button>
      </el-form>
    </div>

    <!-- 底部信息 -->
    <div class="login-footer">
      <p>安全 · 高效 · 专业</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Monitor } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)
const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (activeTab.value === 'register') {
    if (value !== registerForm.password) {
      callback(new Error('两次输入密码不一致'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在6-100之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm.username, loginForm.password)
        ElMessage.success('登录成功')
        router.push('/clusters')
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

async function handleRegister() {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.register(
          registerForm.username,
          registerForm.password,
          registerForm.email
        )
        ElMessage.success('注册成功，请登录')
        activeTab.value = 'login'
        loginForm.username = registerForm.username
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: var(--color-bg-base);
}

/* 背景效果 */
.bg-effects {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.grid-lines {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0, 217, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 217, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
}

.glow-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.3;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: var(--color-primary);
  top: -100px;
  right: -100px;
  animation: float 20s ease-in-out infinite;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: #6366f1;
  bottom: -50px;
  left: -50px;
  animation: float 15s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, 30px); }
}

/* 登录卡片 */
.login-card {
  width: 400px;
  background: rgba(17, 24, 39, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  padding: 40px;
  position: relative;
  z-index: 10;
  box-shadow: var(--shadow-lg);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 12px;
}

.logo-icon {
  font-size: 36px;
  color: var(--color-primary);
}

.logo-text {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.subtitle {
  color: var(--color-text-muted);
  font-size: 14px;
}

/* 标签页 */
.login-tabs {
  display: flex;
  margin-bottom: 32px;
  border-bottom: 1px solid var(--color-border);
}

.tab {
  flex: 1;
  text-align: center;
  padding: 12px;
  cursor: pointer;
  color: var(--color-text-muted);
  font-size: 15px;
  font-weight: 500;
  transition: all var(--transition-normal);
  position: relative;
}

.tab:hover {
  color: var(--color-text-secondary);
}

.tab.active {
  color: var(--color-primary);
}

.tab.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--color-primary);
}

/* 表单 */
.login-form {
  margin-top: 24px;
}

.login-form :deep(.el-input__wrapper) {
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 4px 12px;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: var(--color-primary);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(0, 217, 255, 0.1) !important;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  margin-top: 8px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  border: none;
  border-radius: var(--radius-md);
}

.submit-btn:hover {
  opacity: 0.9;
}

/* 底部 */
.login-footer {
  position: absolute;
  bottom: 24px;
  color: var(--color-text-muted);
  font-size: 13px;
}
</style>
