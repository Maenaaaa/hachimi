<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getBuyerOrders, getSellerOrders, confirmOrder, cancelOrder, completeOrder } from '@/api/order'
import { createReview } from '@/api/review'
import { useUserStore } from '@/stores/user'
import { formatPrice, formatDate, getAvatarUrl, getImageUrl } from '@/utils'
import {
  NCard, NButton, NTag, NEmpty, NSpin, NTabPane, NTabs, NSpace,
  NModal, NInput, NRate, useMessage, useDialog,
} from 'naive-ui'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

const buyerOrders = ref<any[]>([])
const sellerOrders = ref<any[]>([])
const loading = ref(true)
const mainTab = ref('buy')
const subTab = ref('all')

const statusLabels: Record<string, string> = {
  PENDING: '待确认', IN_PROGRESS: '交易中', COMPLETED: '已完成', CANCELLED: '已取消',
}

const statusType: Record<string, 'success' | 'info' | 'error' | 'warning'> = {
  COMPLETED: 'success', IN_PROGRESS: 'info', CANCELLED: 'error', PENDING: 'warning',
}

const currentOrders = computed(() => {
  const base = mainTab.value === 'buy' ? buyerOrders.value : sellerOrders.value
  if (subTab.value === 'all') return base
  if (subTab.value === 'dealing') return base.filter(o => o.status === 'PENDING' || o.status === 'IN_PROGRESS')
  if (subTab.value === 'pending_review') {
    if (isSeller.value) return base.filter(o => o.status === 'COMPLETED' && !o.sellerReviewed)
    return base.filter(o => o.status === 'COMPLETED' && !o.buyerReviewed)
  }
  if (subTab.value === 'reviewed') {
    if (isSeller.value) return base.filter(o => o.status === 'COMPLETED' && o.sellerReviewed)
    return base.filter(o => o.status === 'COMPLETED' && o.buyerReviewed)
  }
  if (subTab.value === 'cancelled') return base.filter(o => o.status === 'CANCELLED')
  return base
})

const dealingCount = computed(() => {
  const base = mainTab.value === 'buy' ? buyerOrders.value : sellerOrders.value
  return base.filter(o => o.status === 'PENDING' || o.status === 'IN_PROGRESS').length
})

const pendingReviewCount = computed(() => {
  const base = mainTab.value === 'buy' ? buyerOrders.value : sellerOrders.value
  if (isSeller.value) return base.filter(o => o.status === 'COMPLETED' && !o.sellerReviewed).length
  return base.filter(o => o.status === 'COMPLETED' && !o.buyerReviewed).length
})

const isSeller = computed(() => mainTab.value === 'sell')

function getCounterLabel(order: any) {
  return isSeller.value ? '买家' : '卖家'
}
function getCounterName(order: any) {
  return isSeller.value ? order.buyerNickname : order.sellerNickname
}
function getCounterAvatar(order: any) {
  return isSeller.value ? order.buyerAvatar : order.sellerAvatar
}
function getCounterId(order: any) {
  return isSeller.value ? order.buyerId : order.sellerId
}

const showReviewModal = ref(false)
const reviewRating = ref(5)
const reviewContent = ref('')
const reviewOrderId = ref<number | null>(null)
const reviewTargetId = ref<number | null>(null)
const submittingReview = ref(false)

async function loadOrders() {
  loading.value = true
  try {
    const [buyerRes, sellerRes] = await Promise.allSettled([getBuyerOrders(), getSellerOrders()])
    if (buyerRes.status === 'fulfilled') buyerOrders.value = (buyerRes.value.data as any[]) || []
    if (sellerRes.status === 'fulfilled') sellerOrders.value = (sellerRes.value.data as any[]) || []
  } catch { message.error('加载订单失败') } finally { loading.value = false }
}

async function handleConfirm(id: number) {
  try { await confirmOrder(id); message.success('已确认'); loadOrders() } catch (err: any) { message.error(err.message || '操作失败') }
}
async function handleCancel(id: number) {
  dialog.warning({
    title: '取消订单', content: '确定取消该订单吗？', positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      try { await cancelOrder(id); message.success('已取消'); loadOrders() } catch { message.error('操作失败') }
    },
  })
}
async function handleComplete(id: number) {
  try { await completeOrder(id); message.success('交易完成'); loadOrders() } catch { message.error('操作失败') }
}

function openReviewModal(order: any) {
  const targetId = isSeller.value ? order.buyerId : order.sellerId
  reviewOrderId.value = order.id
  reviewTargetId.value = targetId
  reviewRating.value = 5
  reviewContent.value = ''
  showReviewModal.value = true
}

async function submitReview() {
  if (!reviewOrderId.value) return
  submittingReview.value = true
  try {
    await createReview({ orderId: reviewOrderId.value, rating: reviewRating.value, content: reviewContent.value })
    message.success('评价成功')
    showReviewModal.value = false
    loadOrders()
  } catch { message.error('评价失败') } finally { submittingReview.value = false }
}

function goToGoods(id: number) { router.push(`/goods/${id}`) }
function goToOrder(id: number) { router.push(`/order/${id}`) }

onMounted(loadOrders)
</script>

<template>
  <div>
    <h2 class="text-xl font-bold text-gray-800 mb-4">我的订单</h2>

    <NCard :bordered="true" style="border-radius: 12px">
      <NTabs v-model:value="mainTab" type="line" @update:value="subTab = 'all'">
        <NTabPane name="buy" tab="我买到的">
          <NTabs v-model:value="subTab" type="segment" size="small" class="mt-4">
            <NTabPane name="all" tab="全部" />
            <NTabPane name="dealing" :tab="dealingCount > 0 ? `交易中 (${dealingCount})` : '交易中'" />
            <NTabPane name="pending_review" :tab="pendingReviewCount > 0 ? `待评价 (${pendingReviewCount})` : '待评价'" />
            <NTabPane name="reviewed" tab="已评价" />
            <NTabPane name="cancelled" tab="已取消" />
          </NTabs>
        </NTabPane>
        <NTabPane name="sell" tab="我卖出的">
          <NTabs v-model:value="subTab" type="segment" size="small" class="mt-4">
            <NTabPane name="all" tab="全部" />
            <NTabPane name="dealing" :tab="dealingCount > 0 ? `交易中 (${dealingCount})` : '交易中'" />
            <NTabPane name="pending_review" :tab="pendingReviewCount > 0 ? `待评价 (${pendingReviewCount})` : '待评价'" />
            <NTabPane name="reviewed" tab="已评价" />
            <NTabPane name="cancelled" tab="已取消" />
          </NTabs>
        </NTabPane>
      </NTabs>

      <NSpin :show="loading">
        <div v-if="currentOrders.length > 0" class="mt-4 space-y-3">
          <div v-for="order in currentOrders" :key="order.id">
            <NCard :bordered="true" style="border-radius: 12px" class="cursor-pointer hover:shadow-md transition-shadow" @click="goToOrder(order.id)">
              <div class="flex items-start gap-4">
                <img :src="getImageUrl(order.goodsCoverImage)" class="w-20 h-20 object-cover rounded-lg"
                  @click.stop="goToGoods(order.goodsId)" />
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between flex-wrap gap-2">
                    <span class="font-semibold text-gray-800 cursor-pointer hover:text-[#3B82F6] truncate" @click.stop="goToGoods(order.goodsId)">{{ order.goodsTitle }}</span>
                    <div class="flex items-center gap-2">
                      <span class="text-xs text-gray-400">订单号：{{ order.orderNo }}</span>
                      <NTag size="small" :type="statusType[order.status] || 'default'">{{ statusLabels[order.status] || order.status }}</NTag>
                    </div>
                  </div>
                  <div class="text-[#3B82F6] font-bold text-lg mt-1">
                    <template v-if="order.goodsTradeType === 'EXCHANGE'">🔄 置换</template>
                    <template v-else>{{ formatPrice(order.amount) }}</template>
                  </div>
                  <div v-if="order.goodsTradeType === 'EXCHANGE' && order.exchangeGoodsId" class="flex items-center gap-2 mt-2 p-2 bg-green-50 rounded-lg">
                    <img :src="getImageUrl(order.exchangeGoodsCoverImage)" class="w-10 h-10 object-cover rounded shrink-0" />
                    <span class="text-xs text-gray-600">⇄ 置换商品：{{ order.exchangeGoodsTitle }}</span>
                  </div>
                  <div v-if="order.meetTime || order.meetPlace" class="text-xs text-gray-500 mt-1">
                    <span v-if="order.meetTime">🕐 {{ order.meetTime }}</span>
                    <span v-if="order.meetPlace" class="ml-3">📍 {{ order.meetPlace }}</span>
                  </div>
                  <div class="flex items-center justify-between mt-2">
                    <div class="flex items-center gap-2 text-xs text-gray-400">
                      <span class="text-gray-400">{{ getCounterLabel(order) }}:</span>
                      <img :src="getAvatarUrl(getCounterAvatar(order), 'thumb_64')" class="w-5 h-5 rounded-full object-cover cursor-pointer shrink-0"
                        @click.stop="router.push('/profile?userId=' + getCounterId(order))" />
                      <span class="text-gray-600 cursor-pointer hover:text-[#3B82F6]"
                        @click.stop="router.push('/profile?userId=' + getCounterId(order))">{{ getCounterName(order) }}</span>
                      <span class="ml-2">{{ formatDate(order.createTime) }}</span>
                    </div>
                    <NSpace @click.stop>
                      <template v-if="isSeller">
                        <NButton v-if="order.status === 'PENDING'" size="tiny" type="primary" @click="handleConfirm(order.id)">确认</NButton>
                        <NButton v-if="order.status === 'PENDING'" size="tiny" @click="handleCancel(order.id)">拒绝</NButton>
                        <NButton v-if="order.status === 'COMPLETED' && !order.sellerReviewed" size="tiny" @click="openReviewModal(order)">评价买家</NButton>
                        <NTag v-if="order.status === 'COMPLETED' && order.sellerReviewed" size="tiny" type="success">已评价</NTag>
                      </template>
                      <template v-else>
                        <NButton v-if="order.status === 'PENDING'" size="tiny" type="error" @click="handleCancel(order.id)">取消</NButton>
                        <NButton v-if="order.status === 'IN_PROGRESS'" size="tiny" type="success" @click="handleComplete(order.id)">确认收货</NButton>
                        <NButton v-if="order.status === 'COMPLETED' && !order.buyerReviewed" size="tiny" @click="openReviewModal(order)">评价卖家</NButton>
                        <NTag v-if="order.status === 'COMPLETED' && order.buyerReviewed" size="tiny" type="success">已评价</NTag>
                      </template>
                    </NSpace>
                  </div>
                </div>
              </div>
            </NCard>
          </div>
        </div>
        <NEmpty v-else description="暂无订单" class="mt-4" />
      </NSpin>
    </NCard>

    <!-- Review Modal -->
    <NModal v-model:show="showReviewModal" title="评价" preset="card" style="width: 400px; border-radius: 12px">
      <div class="space-y-4">
        <div class="text-center">
          <p class="text-sm text-gray-500 mb-2">评分</p>
          <NRate v-model:value="reviewRating" size="large" />
        </div>
        <NInput v-model:value="reviewContent" type="textarea" placeholder="写下你的评价（选填）" :rows="3" />
        <NButton type="primary" block :loading="submittingReview" @click="submitReview">提交评价</NButton>
      </div>
    </NModal>
  </div>
</template>
