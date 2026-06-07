import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types/entity'
import * as authApi from '@/api/auth'
import * as userApi from '@/api/user'
import { setTokens, clearTokens, getToken } from '@/utils/request'
import { USER_KEY } from '@/constants'

function loadUser(): User | null {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? (JSON.parse(raw) as User) : null
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(loadUser())
  const isLoggedIn = ref(!!getToken() && !!user.value)
  const isAdmin = ref(user.value?.role?.toUpperCase() === 'ADMIN')

  function updateAuthState() {
    isLoggedIn.value = !!getToken() && !!user.value
    isAdmin.value = user.value?.role?.toUpperCase() === 'ADMIN'
  }

  async function login(data: { username: string; password: string }) {
    const res = await authApi.login(data)
    setTokens(res.data.accessToken, res.data.refreshToken)
    await fetchProfile()
    updateAuthState()
    return res
  }

  async function register(data: { username: string; password: string; confirmPassword: string; nickname: string }) {
    const res = await authApi.register(data)
    if (res.data?.accessToken) {
      setTokens(res.data.accessToken, res.data.refreshToken)
      await fetchProfile()
      updateAuthState()
    }
    return res
  }

  async function fetchProfile() {
    const res = await userApi.getUserProfile()
    user.value = res.data
    localStorage.setItem(USER_KEY, JSON.stringify(res.data))
    updateAuthState()
    return res
  }

  async function updateProfile(data: { nickname: string; email: string; phone: string }) {
    const res = await userApi.updateUserProfile(data)
    user.value = res.data
    localStorage.setItem(USER_KEY, JSON.stringify(res.data))
    return res
  }

  async function changePassword(data: { oldPassword: string; newPassword: string; confirmPassword: string }) {
    return userApi.changePassword(data)
  }

  async function uploadAvatar(formData: FormData) {
    const res = await userApi.uploadAvatar(formData)
    if (user.value) {
      user.value.avatar = res.data
      localStorage.setItem(USER_KEY, JSON.stringify(user.value))
    }
    return res
  }

  function logout() {
    clearTokens()
    user.value = null
    updateAuthState()
  }

  return {
    user,
    isLoggedIn,
    isAdmin,
    login,
    register,
    fetchProfile,
    updateProfile,
    changePassword,
    uploadAvatar,
    logout,
  }
})
