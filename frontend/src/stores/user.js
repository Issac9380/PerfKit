import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(username, password) {
    const res = await api.post('/auth/login', { username, password })
    token.value = res.data.data.token
    localStorage.setItem('token', token.value)
    return res.data
  }

  async function register(username, password, email) {
    const res = await api.post('/auth/register', { username, password, email })
    return res.data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    register,
    logout
  }
})
