<script setup lang="ts">
import { ref, computed, h, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { getToken } from '@/utils/request'
import { getAvatarUrl } from '@/utils'
import { getAdminAiJudgments } from '@/api/ai-judgment'
import {
  NLayout,
  NLayoutHeader,
  NLayoutSider,
  NMenu,
  NButton,
  NAvatar,
  NIcon,
  NSpace,
  NPopover,
} from 'naive-ui'
import {
  Board24Filled,
  Person24Filled,
  Gift24Filled,
  Warning24Filled,
  Megaphone24Filled,
  SignOut24Filled,
  WeatherMoon48Filled,
  WeatherSunny48Filled,
} from '@vicons/fluent'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()
const collapsed = ref(false)
const showUserMenu = ref(false)
const escalatedCount = ref(0)

let pollTimer: ReturnType<typeof setInterval> | null = null

const userAvatarUrl = computed(() => {
  return getAvatarUrl(userStore.user?.avatar, 'original')
})

async function fetchPendingCount() {
  try {
    const [escalatedRes, appealedRes] = await Promise.all([
      getAdminAiJudgments({ status: 'ESCALATED', page: 1, size: 100 }),
      getAdminAiJudgments({ status: 'APPEALED', page: 1, size: 100 }),
    ])
    const escalated = ((escalatedRes.data as any)?.records || []) as any[]
    const appealed = ((appealedRes.data as any)?.records || []) as any[]
    escalatedCount.value = escalated.length + appealed.length
  } catch { /* ignore */ }
}

function handleLogout() {
  showUserMenu.value = false
  userStore.logout()
  router.push('/login')
}

function handleMenuClick(key: string) {
  showUserMenu.value = false
  if (key === 'logout') {
    handleLogout()
  }
}

function renderMenuLabel(text: string, count: number) {
  return () => h('span', { style: { display: 'inline-flex', alignItems: 'center', gap: '6px' } }, [
    text,
    count > 0
      ? h('span', {
          style: {
            height: '18px', borderRadius: '9px', padding: '0 6px',
            background: '#ef4444', color: '#fff', fontSize: '11px', fontWeight: 'bold',
            display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
            lineHeight: '18px', minWidth: '18px', flexShrink: '0',
          }
        }, String(count > 99 ? '99+' : count))
      : null,
  ])
}

const menuOptions = computed(() => [
  { label: '数据概览', key: '/admin', icon: () => h(NIcon, null, { default: () => h(Board24Filled) }) },
  { label: '用户管理', key: '/admin/users', icon: () => h(NIcon, null, { default: () => h(Person24Filled) }) },
  { label: '商品管理', key: '/admin/goods', icon: () => h(NIcon, null, { default: () => h(Gift24Filled) }) },
  { label: '举报管理', key: '/admin/reports', icon: () => h(NIcon, null, { default: () => h(Warning24Filled) }) },
  { label: '认证审核', key: '/admin/verifications', icon: () => h(NIcon, null, { default: () => h(Person24Filled) }) },
  { label: '仲裁管理', key: '/admin/disputes', icon: () => h(NIcon, null, { default: () => h(Warning24Filled) }) },
  { label: '公告管理', key: '/admin/announcements', icon: () => h(NIcon, null, { default: () => h(Megaphone24Filled) }) },
  { label: renderMenuLabel('AI判决管理', escalatedCount.value), key: '/admin/ai-judgments', icon: () => h(NIcon, null, { default: () => h(Board24Filled) }) },
  { label: 'AI配置管理', key: '/admin/ai-config', icon: () => h(NIcon, null, { default: () => h(Board24Filled) }) },
])

const currentKey = computed(() => route.path)

watch(currentKey, () => {
  fetchPendingCount()
})

function handleMenuUpdate(key: string) {
  router.push(key)
}

onMounted(async () => {
  if (getToken() && !userStore.user) {
    try { await userStore.fetchProfile() } catch { /* ignore */ }
  }
  fetchPendingCount()
  pollTimer = setInterval(fetchPendingCount, 60000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<template>
  <NLayout class="admin-layout" has-sider>
    <NLayoutSider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="220"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
      class="admin-sider dark:bg-gray-800 bg-white"
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

    <NLayout class="admin-main">
      <NLayoutHeader bordered class="layout-header h-16 flex items-center px-4 justify-end">
        <div class="header-right">
          <NButton quaternary circle @click="appStore.toggleTheme()">
            <template #icon>
              <NIcon><WeatherSunny48Filled v-if="appStore.isDark" /><WeatherMoon48Filled v-else /></NIcon>
            </template>
          </NButton>
          <NPopover trigger="click" placement="bottom-end" :show="showUserMenu" @update:show="showUserMenu = $event">
            <template #trigger>
              <div class="avatar-wrapper" @click="showUserMenu = !showUserMenu">
                <img
                  :src="getAvatarUrl(userStore.user?.avatar, 'original')"
                  class="avatar-img"
                />
                <span class="user-name">{{ userStore.user?.nickname || '管理员' }}</span>
              </div>
            </template>
            <div class="user-menu">
              <div class="user-info">
                <img 
                  :src="getAvatarUrl(userStore.user?.avatar, 'original')" 
                  class="user-avatar"
                />
                <div class="user-details">
                  <div class="user-nickname">{{ userStore.user?.nickname || '管理员' }}</div>
                  <div class="user-role">管理员</div>
                </div>
              </div>
              <div class="menu-divider"></div>
              <div class="menu-item logout" @click="handleMenuClick('logout')">
                <NIcon :size="18"><SignOut24Filled /></NIcon>
                <span>退出登录</span>
              </div>
            </div>
          </NPopover>
        </div>
      </NLayoutHeader>

      <div class="admin-content p-6 page-bg">
        <router-view />
      </div>
    </NLayout>
  </NLayout>
</template>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

.admin-sider {
  height: 100vh;
  overflow-y: auto;
}

.admin-main {
  height: 100vh;
  overflow: hidden;
}

.admin-content {
  height: calc(100vh - 64px);
  overflow-y: auto;
}

/* Header Right */
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* Avatar Wrapper */
.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px 4px 4px;
  border-radius: 20px;
  transition: all 0.2s ease;
}

.avatar-wrapper:hover {
  background: rgba(59, 130, 246, 0.08);
}

.avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e5e7eb;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.user-name {
  font-size: 14px;
  color: #4b5563;
  font-weight: 500;
  white-space: nowrap;
}

/* User Menu */
.user-menu {
  width: 200px;
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
  width: 40px;
  height: 40px;
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
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: #3B82F6;
  font-weight: 500;
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
html.dark .user-menu {
  background: #1f2937;
}

html.dark .avatar-img {
  border-color: #374151;
}

html.dark .avatar-wrapper:hover .avatar-img {
  border-color: #3B82F6;
}

html.dark .user-name {
  color: #d1d5db;
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
