import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    children: [
      {
        path: '',
        redirect: '/clusters'
      },
      {
        path: 'clusters',
        name: 'Clusters',
        component: () => import('@/views/Clusters.vue')
      },
      {
        path: 'versions',
        name: 'Versions',
        component: () => import('@/views/Versions.vue')
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('@/views/Logs.vue')
      },
      {
        path: 'analysis',
        name: 'Analysis',
        component: () => import('@/views/Analysis.vue')
      },
      {
        path: 'deploy',
        name: 'Deploy',
        component: () => import('@/views/Deploy.vue')
      },
      {
        path: 'templates',
        name: 'Templates',
        component: () => import('@/views/Templates.vue')
      },
      {
        path: 'ai-config',
        name: 'AiConfig',
        component: () => import('@/views/AiConfig.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (!to.meta.public && !userStore.token) {
    next('/login')
  } else if (to.path === '/login' && userStore.token) {
    next('/clusters')
  } else {
    next()
  }
})

export default router
