<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDashboardStats } from '@/api/admin'
import type { DashboardStats } from '@/types/entity'
import {
  NCard,
  NGrid,
  NGi,
  NStatistic,
  NIcon,
  NSpin,
  NButton,
  useMessage,
} from 'naive-ui'
import {
  Person24Filled,
  Gift24Filled,
  ShoppingBag24Filled,
  Warning24Filled,
  ArrowTrending24Filled,
} from '@vicons/fluent'

const message = useMessage()
const stats = ref<DashboardStats | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getDashboardStats()
    stats.value = res.data
  } catch {
    message.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="space-y-6">
    <h2 class="text-xl font-bold text-gray-800">数据概览</h2>

    <NSpin :show="loading">
      <NGrid :cols="4" :x-gap="16" :y-gap="16">
        <NGi>
          <NCard :bordered="true" style="border-radius: 12px">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center">
                <NIcon size="24" color="#3B82F6"><Person24Filled /></NIcon>
              </div>
              <NStatistic :value="stats?.totalUsers || 0" label="总用户数" />
            </div>
            <div class="text-xs text-gray-400 mt-2">今日新增: {{ stats?.todayNewUsers || 0 }}</div>
          </NCard>
        </NGi>

        <NGi>
          <NCard :bordered="true" style="border-radius: 12px">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center">
                <NIcon size="24" color="#10B981"><Gift24Filled /></NIcon>
              </div>
              <NStatistic :value="stats?.totalGoods || 0" label="总商品数" />
            </div>
            <div class="text-xs text-gray-400 mt-2">今日新增: {{ stats?.todayNewOrders || 0 }} | 待审核: {{ stats?.pendingReviewGoods || 0 }}</div>
          </NCard>
        </NGi>

        <NGi>
          <NCard :bordered="true" style="border-radius: 12px">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center">
                <NIcon size="24" color="#8B5CF6"><ShoppingBag24Filled /></NIcon>
              </div>
              <NStatistic :value="stats?.totalOrders || 0" label="总订单数" />
            </div>
          </NCard>
        </NGi>

        <NGi>
          <NCard :bordered="true" style="border-radius: 12px">
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 bg-red-100 rounded-xl flex items-center justify-center">
                <NIcon size="24" color="#EF4444"><Warning24Filled /></NIcon>
              </div>
              <NStatistic :value="stats?.pendingReports || 0" label="举报数" />
            </div>
            <div class="text-xs text-gray-400 mt-2">待处理: {{ stats?.pendingReports || 0 }}</div>
          </NCard>
        </NGi>
      </NGrid>

      <!-- Quick Links -->
      <NCard :bordered="true" style="border-radius: 12px">
        <h3 class="font-bold text-gray-800 mb-4">快捷操作</h3>
        <div class="flex gap-3 flex-wrap">
          <NButton @click="$router.push('/admin/users')">用户管理</NButton>
          <NButton @click="$router.push('/admin/goods')">商品管理</NButton>
          <NButton @click="$router.push('/admin/reports')">举报管理</NButton>
          <NButton @click="$router.push('/admin/announcements')">公告管理</NButton>
        </div>
      </NCard>
    </NSpin>
  </div>
</template>
