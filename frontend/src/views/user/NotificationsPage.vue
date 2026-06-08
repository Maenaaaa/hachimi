<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, readNotification, readAllNotifications } from '@/api/notification'
import { formatDate } from '@/utils'
import { useAppStore } from '@/stores/app'
import { NButton, NCard, NEmpty, NSpin, NTag, useMessage } from 'naive-ui'

const router = useRouter()
const message = useMessage()
const appStore = useAppStore()
const notifications = ref<any[]>([])
const loading = ref(true)

const typeLabels: Record<string, string> = { SYSTEM: '系统', ORDER: '订单', REVIEW: '审核' }
const typeColors: Record<string, string> = { SYSTEM: '#3B82F6', ORDER: '#F59E0B', REVIEW: '#10B981' }

async function load() {
  loading.value = true
  try {
    const res = await getNotifications()
    notifications.value = res.data || []
  } catch { message.error('加载失败') } finally { loading.value = false }
}

async function handleClick(n: any) {
  if (!n.isRead) {
    try {
      await readNotification(n.id)
      n.isRead = true
      appStore.setUnreadCount(Math.max(0, appStore.unreadCount - 1))
    } catch { /* ignore */ }
  }
  if (n.relatedId) {
    if (n.type === 'ORDER') router.push(`/order/${n.relatedId}`)
    else if (n.title === '收到新评价') router.push(`/review/${n.relatedId}`)
    else if (n.type === 'REVIEW') router.push(`/goods/${n.relatedId}`)
  }
}

async function handleMarkAll() {
  try {
    await readAllNotifications()
    const unreadNum = notifications.value.filter(n => !n.isRead).length
    notifications.value.forEach(n => n.isRead = true)
    appStore.setUnreadCount(0)
    message.success('已全部标为已读')
  } catch { message.error('操作失败') }
}

onMounted(load)
</script>

<template>
  <div class="max-w-700px mx-auto pb-8">
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-xl font-bold text-gray-800 dark:text-gray-100">消息通知</h2>
      <NButton size="small" quaternary @click="handleMarkAll">全部已读</NButton>
    </div>

    <NSpin :show="loading">
      <div v-if="notifications.length > 0" class="space-y-2">
        <div v-for="n in notifications" :key="n.id"
          class="p-4 rounded-xl cursor-pointer transition-colors border"
          :class="n.isRead ? 'bg-white dark:bg-gray-800 border-gray-100 dark:border-gray-700' : 'bg-[#EFF6FF] dark:bg-blue-900/30 border-[#BFDBFE] dark:border-blue-700/50'"
          @click="handleClick(n)">
          <div class="flex items-center gap-2">
            <span class="w-2 h-2 rounded-full shrink-0" :style="{ backgroundColor: typeColors[n.type] || '#999' }" />
            <span class="font-semibold text-sm flex-1 text-gray-800 dark:text-gray-100">{{ n.title }}</span>
            <span v-if="!n.isRead" class="w-2 h-2 bg-[#3B82F6] rounded-full shrink-0" />
            <NTag size="tiny">{{ typeLabels[n.type] || n.type }}</NTag>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1 ml-4">{{ n.content }}</p>
          <span class="text-xs text-gray-400 dark:text-gray-500 mt-1 ml-4 block">{{ formatDate(n.createTime) }}</span>
        </div>
      </div>
      <NEmpty v-else description="暂无通知" class="mt-10" />
    </NSpin>
  </div>
</template>

<style scoped>
.max-w-700px { max-width: 700px; }
</style>
