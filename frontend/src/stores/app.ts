import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export type Theme = 'light' | 'dark'

export const useAppStore = defineStore('app', () => {
  const theme = ref<Theme>('light')
  const unreadCount = ref(0)
  const collapsed = ref(false)
  const showLoginModal = ref(false)
  const pendingRedirect = ref<string | null>(null)
  const isDark = computed(() => theme.value === 'dark')

  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    document.documentElement.classList.toggle('dark', theme.value === 'dark')
  }

  function setUnreadCount(count: number) {
    unreadCount.value = count
  }

  function toggleCollapsed() {
    collapsed.value = !collapsed.value
  }

  function openLoginModal(redirect?: string) {
    showLoginModal.value = true
    if (redirect) pendingRedirect.value = redirect
  }

  function closeLoginModal() {
    showLoginModal.value = false
    pendingRedirect.value = null
  }

  return {
    theme,
    unreadCount,
    collapsed,
    showLoginModal,
    pendingRedirect,
    isDark,
    toggleTheme,
    setUnreadCount,
    toggleCollapsed,
    openLoginModal,
    closeLoginModal,
  }
})
