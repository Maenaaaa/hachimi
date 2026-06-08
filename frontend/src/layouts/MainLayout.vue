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
  NDropdown,
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

function handleUserMenuSelect(key: string) {
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
    <NLayoutHeader bordered class="layout-header" style="height: 64px">
      <div class="max-w-1200px mx-auto h-full flex items-center px-4 justify-between">
        <div class="flex items-center gap-2 cursor-pointer" @click="router.push('/')">
          <div class="w-8 h-8 bg-[#3B82F6] rounded-lg flex items-center justify-center text-white font-bold text-lg">C</div>
          <span class="text-lg font-bold text-gray-800 dark:text-gray-100 hidden sm:block">校园闲置交换</span>
        </div>

        <div class="flex-1 max-w-400px mx-4 hidden md:block">
          <NInput
            v-model:value="keyword"
            placeholder="搜索闲置物品..."
            clearable
            @keyup.enter="handleSearch"
          >
            <template #suffix>
              <NButton quaternary circle size="tiny" @click="handleSearch">
                <template #icon>
                  <NIcon><Search24Filled /></NIcon>
                </template>
              </NButton>
            </template>
          </NInput>
        </div>

        <NSpace align="center" :size="12">
          <NButton quaternary circle @click="appStore.toggleTheme()">
            <template #icon>
              <NIcon><WeatherSunny48Filled v-if="appStore.isDark" /><WeatherMoon48Filled v-else /></NIcon>
            </template>
          </NButton>

          <template v-if="userStore.isLoggedIn">
            <NButton type="primary" size="small" @click="goToPublish">
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

            <NDropdown trigger="click" :options="userMenuOptions" @select="handleUserMenuSelect">
              <img 
                :src="getAvatarUrl(userStore.user?.avatar, 'original')" 
                class="w-8 h-8 rounded-full object-cover cursor-pointer"
              />
            </NDropdown>
          </template>

          <template v-else>
            <NButton quaternary size="small" @click="appStore.openLoginModal()">登录</NButton>
            <NButton type="primary" size="small" @click="appStore.openLoginModal()">注册</NButton>
          </template>
        </NSpace>
      </div>
    </NLayoutHeader>

    <NLayoutContent class="p-4">
      <div class="max-w-1200px mx-auto">
        <router-view />
      </div>
    </NLayoutContent>
  </NLayout>
  <LoginModal />
</template>

<style scoped>
.max-w-1200px {
  max-width: 1200px;
}
.min-h-screen {
  min-height: 100vh;
}
</style>
