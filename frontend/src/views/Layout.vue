<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="240px" class="sidebar">
      <div class="logo">
        <el-icon class="logo-icon"><Monitor /></el-icon>
        <span class="logo-text">研发作业平台</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="transparent"
        text-color="#94a3b8"
        active-text-color="#00d9ff"
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
              <el-avatar :size="32" class="user-avatar">
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
  background: var(--color-bg-base);
}

.sidebar {
  background: linear-gradient(180deg, var(--color-bg-surface) 0%, var(--color-bg-base) 100%);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
}

.logo {
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid var(--color-border);
}

.logo-icon {
  font-size: 28px;
  color: var(--color-primary);
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  letter-spacing: 0.5px;
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
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: var(--color-bg-elevated) !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(0, 217, 255, 0.1) !important;
  color: var(--color-primary) !important;
}

.sidebar-menu :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 24px;
  background: var(--color-primary);
  border-radius: 0 2px 2px 0;
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--color-border);
}

.version-tag {
  font-size: 12px;
  color: var(--color-text-muted);
  font-family: var(--font-mono);
}

.header {
  background: var(--color-bg-surface);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
}

.page-name {
  font-size: 18px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.user-info:hover {
  background: var(--color-bg-elevated);
}

.user-avatar {
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
}

.username {
  color: var(--color-text-secondary);
  font-size: 14px;
}

.dropdown-icon {
  color: var(--color-text-muted);
  font-size: 12px;
}

.main-content {
  background: var(--color-bg-base);
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
