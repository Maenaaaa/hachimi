<script setup lang="ts">
import { ref, watch } from 'vue'
import { getOrderDetail } from '@/api/order'
import { getAvatarUrl, formatPrice, formatDate } from '@/utils'
import { ORDER_STATUS } from '@/constants'
import { NModal, NSpin, NTag, NButton } from 'naive-ui'

const props = defineProps<{ orderId: number | null }>()
const emit = defineEmits<{ close: [] }>()

const order = ref<any>(null)
const loading = ref(false)

watch(() => props.orderId, async (id) => {
  if (!id) { order.value = null; return }
  loading.value = true
  try {
    const res = await getOrderDetail(id)
    order.value = res.data
  } catch { order.value = null } finally { loading.value = false }
})
</script>

<template>
  <NModal :show="!!orderId" preset="card" style="width: 520px; max-height: 80vh; border-radius: 12px"
    @update:show="(v) => { if (!v) emit('close') }">
    <template #header>
      <div class="font-bold text-gray-800">订单详情</div>
    </template>
    <NSpin :show="loading">
      <div v-if="order" class="space-y-3 text-sm">
        <div class="flex items-center justify-between">
          <span class="text-gray-400">订单号</span>
          <span class="font-mono">{{ order.orderNo }}</span>
        </div>
        <div class="flex items-center justify-between">
          <span class="text-gray-400">状态</span>
          <NTag size="small">{{ ORDER_STATUS[order.status] || order.status }}</NTag>
        </div>
        <div class="flex items-center justify-between">
          <span class="text-gray-400">金额</span>
          <span class="text-[#3B82F6] font-bold">{{ formatPrice(order.amount) }}</span>
        </div>
        <div class="flex items-center justify-between">
          <span class="text-gray-400">商品</span>
          <span class="truncate max-w-60">{{ order.goodsTitle }}</span>
        </div>
        <div class="flex items-center justify-between">
          <span class="text-gray-400">买家</span>
          <div class="flex items-center gap-1.5">
            <img :src="getAvatarUrl(order.buyerAvatar, 'thumb_64')" class="w-5 h-5 rounded-full" />
            <span>{{ order.buyerNickname }}</span>
          </div>
        </div>
        <div class="flex items-center justify-between">
          <span class="text-gray-400">卖家</span>
          <div class="flex items-center gap-1.5">
            <img :src="getAvatarUrl(order.sellerAvatar, 'thumb_64')" class="w-5 h-5 rounded-full" />
            <span>{{ order.sellerNickname }}</span>
          </div>
        </div>
        <div v-if="order.meetPlace" class="flex items-center justify-between">
          <span class="text-gray-400">交易地点</span>
          <span>{{ order.meetPlace }}</span>
        </div>
        <div v-if="order.meetTime" class="flex items-center justify-between">
          <span class="text-gray-400">交易时间</span>
          <span>{{ order.meetTime }}</span>
        </div>
        <div class="flex items-center justify-between">
          <span class="text-gray-400">创建时间</span>
          <span>{{ formatDate(order.createTime) }}</span>
        </div>
      </div>
      <div v-else-if="!loading" class="text-center text-gray-400 py-10">订单不存在</div>
    </NSpin>
  </NModal>
</template>
