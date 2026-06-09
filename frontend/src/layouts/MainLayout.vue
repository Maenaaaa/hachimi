<script setup lang="ts">
import { ref, watch, onMounted, h, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { getUnreadCount } from '@/api/notification'
import { getUnreadCount as getChatUnreadCount } from '@/api/chat'
import { useWebSocket } from '@/composables/useWebSocket'
import { getToken } from '@/utils/request'
import { getAvatarUrl } from '@/utils'
import LoginModal from '@/components/LoginModal.vue'
import {
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NButton,
  NSpace,
  NAvatar,
  NBadge,
  NPopover,
  NIcon,
  NInput,
  NMessageProvider,
} from 'naive-ui'
import {
  Search24Filled,
  Chat24Filled,
  Alert24Filled,
  Person24Filled,
  Add24Filled,
  SignOut24Filled,
  Settings24Filled,
  WeatherMoon48Filled,
  WeatherSunny48Filled,
  ShoppingBag24Filled,
} from '@vicons/fluent'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()
const { subscribe, connect, connected } = useWebSocket()

const userAvatarUrl = computed(() => {
  return getAvatarUrl(userStore.user?.avatar, 'original')
})

const keyword = ref('')
const unreadCount = ref(0)
const chatUnreadCount = ref(0)
const showMobileMenu = ref(false)
const showUserMenu = ref(false)

function handleSearch() {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: keyword.value.trim() } })
  }
}

function goToChat() {
  router.push('/chat')
}

function goToPublish() {
  router.push('/publish')
}

function goToProfile() {
  router.push('/profile')
}

function goToSettings() {
  router.push('/settings')
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

const userMenuOptions = [
  { label: '我的主页', key: 'profile', icon: () => h(Person24Filled) },
  { label: '我的订单', key: 'my-orders', icon: () => h(ShoppingBag24Filled) },
  { label: '个人设置', key: 'settings', icon: () => h(Settings24Filled) },
  { type: 'divider' as const },
  { label: '退出登录', key: 'logout', icon: () => h(SignOut24Filled) },
]

function handleMenuClick(key: string) {
  showUserMenu.value = false
  switch (key) {
    case 'profile':
      goToProfile()
      break
    case 'my-orders':
      router.push('/my-orders')
      break
    case 'settings':
      goToSettings()
      break
    case 'logout':
      handleLogout()
      break
  }
}

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data
    appStore.setUnreadCount(res.data)
  } catch {
    // ignore
  }
}

async function fetchChatUnreadCount() {
  try {
    const res = await getChatUnreadCount()
    chatUnreadCount.value = res.data
  } catch {
    // ignore
  }
}

onMounted(async () => {
  if (getToken() && !userStore.user) {
    try {
      await userStore.fetchProfile()
    } catch {
      // ignore
    }
  }
  if (userStore.isLoggedIn) {
    fetchUnreadCount()
    fetchChatUnreadCount()
    connect()
    // 定时刷新未读数
    setInterval(() => {
      if (userStore.isLoggedIn) {
        fetchChatUnreadCount()
        fetchUnreadCount()
      }
    }, 5000)
    // WebSocket 订阅
    const trySubscribe = (attempts: number) => {
      if (attempts <= 0) return
      if (connected.value) {
        subscribe('/user/queue/notifications', () => { fetchUnreadCount() })
        subscribe('/user/queue/messages', () => { fetchChatUnreadCount() })
      } else {
        setTimeout(() => trySubscribe(attempts - 1), 1000)
      }
    }
    trySubscribe(10)
  }
})

watch(
  () => userStore.isLoggedIn,
  (val) => {
    if (val) {
      fetchUnreadCount()
      fetchChatUnreadCount()
      connect()
    }
  },
)

watch(
  () => route.path,
  () => {
    if (userStore.isLoggedIn) {
      fetchUnreadCount()
      fetchChatUnreadCount()
    }
  },
)

watch(
  () => route.query.login,
  (val) => {
    if (val === 'true' && !userStore.isLoggedIn) {
      appStore.openLoginModal(route.query.redirect as string | undefined)
    }
  },
  { immediate: true },
)
</script>

<template>
  <NLayout class="min-h-screen page-bg">
    <NLayoutHeader class="layout-header header-shadow" style="height: 64px; border-bottom: 1px solid var(--n-border-color)">
      <div class="max-w-[1400px] mx-auto h-full flex items-center px-6 justify-between">
        <div class="flex items-center gap-3 cursor-pointer" @click="router.push('/')">
          <div class="logo-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M17 1l4 4-4 4" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M3 11V9a4 4 0 0 1 4-4h14" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M7 23l-4-4 4-4" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M21 13v2a4 4 0 0 1-4 4H3" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <span class="text-lg font-bold text-gray-800 dark:text-gray-100 hidden sm:block">校园淘</span>
        </div>

        <div class="flex-1 max-w-[500px] mx-6 hidden md:block">
          <NInput
            v-model:value="keyword"
            placeholder="搜索闲置物品..."
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <NIcon><Search24Filled /></NIcon>
            </template>
            <template #suffix>
              <NButton quaternary circle size="tiny" @click="handleSearch">
                <template #icon>
                  <NIcon><Search24Filled /></NIcon>
                </template>
              </NButton>
            </template>
          </NInput>
        </div>

        <NSpace align="center" :size="16">
          <NButton quaternary circle @click="appStore.toggleTheme()">
            <template #icon>
              <NIcon><WeatherSunny48Filled v-if="appStore.isDark" /><WeatherMoon48Filled v-else /></NIcon>
            </template>
          </NButton>

          <template v-if="userStore.isLoggedIn">
            <NButton type="primary" @click="goToPublish">
              <template #icon>
                <NIcon><Add24Filled /></NIcon>
              </template>
              发布
            </NButton>

            <NBadge :value="chatUnreadCount" :max="99">
              <NButton quaternary circle @click="goToChat">
                <template #icon>
                  <NIcon size="22"><Chat24Filled /></NIcon>
                </template>
              </NButton>
            </NBadge>

            <NBadge :value="unreadCount" :max="99">
              <NButton quaternary circle @click="router.push('/notifications')">
                <template #icon>
                  <NIcon size="22"><Alert24Filled /></NIcon>
                </template>
              </NButton>
            </NBadge>

            <NPopover trigger="click" placement="bottom-end" :show="showUserMenu" @update:show="showUserMenu = $event">
              <template #trigger>
                <div class="avatar-wrapper" @click="showUserMenu = !showUserMenu">
                  <img 
                    :src="getAvatarUrl(userStore.user?.avatar, 'original')" 
                    class="avatar-img"
                  />
                </div>
              </template>
              <div class="user-menu">
                <div class="user-info">
                  <img 
                    :src="getAvatarUrl(userStore.user?.avatar, 'original')" 
                    class="user-avatar"
                  />
                  <div class="user-details">
                    <div class="user-nickname">{{ userStore.user?.nickname || '用户' }}</div>
                    <div class="user-id">@{{ userStore.user?.username || 'user' }}</div>
                  </div>
                </div>
                <div class="menu-divider"></div>
                <div class="menu-item" @click="handleMenuClick('profile')">
                  <NIcon :size="18"><Person24Filled /></NIcon>
                  <span>我的主页</span>
                </div>
                <div class="menu-item" @click="handleMenuClick('my-orders')">
                  <NIcon :size="18"><ShoppingBag24Filled /></NIcon>
                  <span>我的订单</span>
                </div>
                <div class="menu-item" @click="handleMenuClick('settings')">
                  <NIcon :size="18"><Settings24Filled /></NIcon>
                  <span>个人设置</span>
                </div>
                <div class="menu-divider"></div>
                <div class="menu-item logout" @click="handleMenuClick('logout')">
                  <NIcon :size="18"><SignOut24Filled /></NIcon>
                  <span>退出登录</span>
                </div>
              </div>
            </NPopover>
          </template>

          <template v-else>
            <NButton quaternary @click="appStore.openLoginModal()">登录</NButton>
            <NButton type="primary" @click="appStore.openLoginModal()">注册</NButton>
          </template>
        </NSpace>
      </div>
    </NLayoutHeader>

    <NLayoutContent :native-scrollbar="false" style="height: calc(100vh - 64px)">
      <div class="max-w-[1400px] mx-auto px-6 py-6">
        <router-view />
      </div>
    </NLayoutContent>
  </NLayout>
  <LoginModal />
</template>

<style scoped>
.min-h-screen {
  min-height: 100vh;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.4);
  transition: all 0.3s ease;
}

.logo-icon:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.5);
}

/* User Menu Styles */
.avatar-wrapper {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.avatar-wrapper:hover {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}

.avatar-img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e5e7eb;
  transition: all 0.2s ease;
}

.avatar-wrapper:hover .avatar-img {
  border-color: #3B82F6;
}

.user-menu {
  width: 220px;
  background: #fff;
  border-radius: 12px;
  padding: 8px;
  margin: 0;
  border: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(37, 99, 235, 0.04) 100%);
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-nickname {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-id {
  font-size: 12px;
  color: #9ca3af;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.menu-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 6px 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #4b5563;
  font-size: 14px;
}

.menu-item:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.menu-item:active {
  transform: scale(0.98);
}

.menu-item.logout {
  color: #ef4444;
}

.menu-item.logout:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

/* Override NPopover default styles */
:deep(.n-popover) {
  border: none !important;
  box-shadow: none !important;
}
</style>

<style>
/* Dark mode styles - unscoped to work with NPopover */
html.dark .avatar-img {
  border-color: #374151;
}

html.dark .user-menu {
  background: #1f2937;
}

html.dark .user-info {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(37, 99, 235, 0.08) 100%);
}

html.dark .user-avatar {
  border-color: #374151;
}

html.dark .user-nickname {
  color: #f3f4f6;
}

html.dark .user-id {
  color: #9ca3af;
}

html.dark .menu-divider {
  background: #374151;
}

html.dark .menu-item {
  color: #d1d5db;
}

html.dark .menu-item:hover {
  background: #374151;
  color: #f3f4f6;
}

html.dark .menu-item.logout {
  color: #f87171;
}

html.dark .menu-item.logout:hover {
  background: rgba(248, 113, 113, 0.15);
  color: #ef4444;
}
</style>
