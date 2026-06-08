<script setup lang="ts">
import { ref, computed, h, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { getToken } from '@/utils/request'
import { getAvatarUrl } from '@/utils'
import {
  NLayout,
  NLayoutHeader,
  NLayoutSider,
  NLayoutContent,
  NMenu,
  NButton,
  NAvatar,
  NIcon,
  NSpace,
  NDropdown,
} from 'naive-ui'
import {
  Board24Filled,
  Person24Filled,
  Gift24Filled,
  Warning24Filled,
  Megaphone24Filled,
  ArrowLeft24Filled,
  SignOut24Filled,
  WeatherMoon48Filled,
  WeatherSunny48Filled,
} from '@vicons/fluent'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()
const collapsed = ref(false)

const userAvatarUrl = computed(() => {
  return getAvatarUrl(userStore.user?.avatar, 'original')
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

const userMenuOptions = [
  { label: '退出登录', key: 'logout', icon: () => h(NIcon, null, { default: () => h(SignOut24Filled) }) },
]

const menuOptions = [
  { label: '数据概览', key: '/admin', icon: () => h(NIcon, null, { default: () => h(Board24Filled) }) },
  { label: '用户管理', key: '/admin/users', icon: () => h(NIcon, null, { default: () => h(Person24Filled) }) },
  { label: '商品管理', key: '/admin/goods', icon: () => h(NIcon, null, { default: () => h(Gift24Filled) }) },
  { label: '举报管理', key: '/admin/reports', icon: () => h(NIcon, null, { default: () => h(Warning24Filled) }) },
  { label: '认证审核', key: '/admin/verifications', icon: () => h(NIcon, null, { default: () => h(Person24Filled) }) },
  { label: '仲裁管理', key: '/admin/disputes', icon: () => h(NIcon, null, { default: () => h(Warning24Filled) }) },
  { label: '公告管理', key: '/admin/announcements', icon: () => h(NIcon, null, { default: () => h(Megaphone24Filled) }) },
]

const currentKey = computed(() => route.path)

function handleMenuUpdate(key: string) {
  router.push(key)
}

onMounted(async () => {
  if (getToken() && !userStore.user) {
    try { await userStore.fetchProfile() } catch { /* ignore */ }
  }
})
</script>

<template>
  <NLayout class="min-h-screen page-bg" has-sider>
    <NLayoutSider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="220"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
      class="dark:bg-gray-800 bg-white"
    >
      <div class="flex items-center justify-center h-16 border-b dark:border-gray-700 border-gray-100">
        <div v-if="!collapsed" class="text-lg font-bold text-[#3B82F6]">管理后台</div>
        <div v-else class="text-lg font-bold text-[#3B82F6]">M</div>
      </div>
      <NMenu
        :value="currentKey"
        :collapsed="collapsed"
        :collapsed-width="64"
        :options="menuOptions"
        @update:value="handleMenuUpdate"
      />
    </NLayoutSider>

    <NLayout>
      <NLayoutHeader bordered class="layout-header h-16 flex items-center px-4 justify-between">
        <NSpace>
          <NButton quaternary circle @click="router.push('/')">
            <template #icon>
              <NIcon><ArrowLeft24Filled /></NIcon>
            </template>
          </NButton>
          <span class="text-gray-500 dark:text-gray-400 text-sm">返回前台</span>
        </NSpace>

        <NSpace align="center" :size="12">
          <NButton quaternary circle @click="appStore.toggleTheme()">
            <template #icon>
              <NIcon><WeatherSunny48Filled v-if="appStore.isDark" /><WeatherMoon48Filled v-else /></NIcon>
            </template>
          </NButton>
          <NDropdown trigger="click" :options="userMenuOptions" @select="(key: string) => { if (key === 'logout') handleLogout() }">
            <NSpace align="center" class="cursor-pointer">
              <img
                :src="getAvatarUrl(userStore.user?.avatar, 'original')"
                class="w-8 h-8 rounded-full object-cover"
              />
              <span class="text-sm text-gray-600 dark:text-gray-300">{{ userStore.user?.nickname || '管理员' }}</span>
            </NSpace>
          </NDropdown>
        </NSpace>
      </NLayoutHeader>

      <NLayoutContent class="p-6 page-bg" style="min-height: calc(100vh - 64px)">
        <router-view />
      </NLayoutContent>
    </NLayout>
  </NLayout>
</template>

<style scoped>
.min-h-screen {
  min-height: 100vh;
}
</style>
