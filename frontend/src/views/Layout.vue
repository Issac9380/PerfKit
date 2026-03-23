<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="260px" class="sidebar">
      <div class="logo">
        <div class="logo-icon-wrap">
          <el-icon class="logo-icon"><Monitor /></el-icon>
        </div>
        <span class="logo-text">研发作业平台</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="transparent"
        text-color="#64748B"
        active-text-color="#2563EB"
        :router="true"
      >
        <el-menu-item index="/clusters">
          <el-icon><Connection /></el-icon>
          <span>K8S 集群</span>
        </el-menu-item>

        <el-menu-item index="/versions">
          <el-icon><Box /></el-icon>
          <span>版本管理</span>
        </el-menu-item>

        <el-menu-item index="/logs">
          <el-icon><Document /></el-icon>
          <span>日志分析</span>
        </el-menu-item>

        <el-menu-item index="/analysis">
          <el-icon><Cpu /></el-icon>
          <span>性能分析</span>
        </el-menu-item>

        <el-menu-item index="/deploy">
          <el-icon><Lightning /></el-icon>
          <span>热部署</span>
        </el-menu-item>

        <el-menu-item index="/templates">
          <el-icon><DocumentAdd /></el-icon>
          <span>模板管理</span>
        </el-menu-item>

        <el-menu-item index="/ai-config">
          <el-icon><Setting /></el-icon>
          <span>AI 配置</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <div class="version-tag">v1.0.0</div>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <h2 class="page-name">{{ pageTitle }}</h2>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" class="user-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">Admin</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 页面内容 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menuItems = {
  '/clusters': 'K8S 集群',
  '/versions': '版本管理',
  '/logs': '日志分析',
  '/analysis': '性能分析',
  '/deploy': '热部署'
}

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => menuItems[route.path] || '控制台')

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: #F8FAFC;
}

.sidebar {
  background: #FFFFFF;
  border-right: 1px solid #E2E8F0;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.02);
}

.logo {
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #E2E8F0;
}

.logo-icon-wrap {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
}

.logo-icon {
  font-size: 22px;
  color: #FFFFFF;
}

.logo-text {
  font-size: 17px;
  font-weight: 700;
  color: #1E293B;
  letter-spacing: -0.3px;
}

.sidebar-menu {
  flex: 1;
  border: none;
  padding: 16px 0;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 4px 12px;
  border-radius: 10px;
  transition: all 0.25s ease;
  font-weight: 500;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: #F1F5F9 !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: #DBEAFE !important;
  color: #2563EB !important;
}

.sidebar-menu :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #2563EB;
  border-radius: 0 3px 3px 0;
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid #E2E8F0;
}

.version-tag {
  font-size: 12px;
  color: #94A3B8;
  font-family: 'JetBrains Mono', monospace;
}

.header {
  background: #FFFFFF;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.header-left {
  display: flex;
  align-items: center;
}

.page-name {
  font-size: 20px;
  font-weight: 600;
  color: #1E293B;
  letter-spacing: -0.3px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 10px;
  transition: background 0.2s ease;
}

.user-info:hover {
  background: #F1F5F9;
}

.user-avatar {
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  border: none;
}

.username {
  color: #64748B;
  font-size: 14px;
  font-weight: 500;
}

.dropdown-icon {
  color: #94A3B8;
  font-size: 12px;
}

.main-content {
  background: #F8FAFC;
  padding: 0;
  overflow-y: auto;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
