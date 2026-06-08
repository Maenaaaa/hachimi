<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getBuyerOrders, getSellerOrders, confirmOrder, cancelOrder, completeOrder } from '@/api/order'
import { createReview } from '@/api/review'
import { useUserStore } from '@/stores/user'
import { formatPrice, formatDate, getImageUrl, getAvatarUrl } from '@/utils'
import {
  NCard, NButton, NTag, NEmpty, NSpin, NTabPane, NTabs, NSpace,
  NModal, NInput, NRate, NAvatar, useMessage, useDialog,
} from 'naive-ui'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

const buyerOrders = ref<any[]>([])
const sellerOrders = ref<any[]>([])
const loading = ref(true)
const tab = ref('buy')

const displayedOrders = computed(() => {
  if (tab.value === 'buy') return buyerOrders.value
  if (tab.value === 'sell') return sellerOrders.value
  // exchange: orders from both sides where goodsTradeType === 'EXCHANGE'
  return [...buyerOrders.value, ...sellerOrders.value].filter(o => o.goodsTradeType === 'EXCHANGE')
})

const isBuyerTab = computed(() => tab.value === 'buy' || (tab.value === 'exchange'))

const showReviewModal = ref(false)
const reviewRating = ref(5)
const reviewContent = ref('')
const reviewOrderId = ref<number | null>(null)
const reviewTargetId = ref<number | null>(null)
const submittingReview = ref(false)

const statusLabels: Record<string, string> = {
  PENDING: '待确认', IN_PROGRESS: '交易中', COMPLETED: '已完成', CANCELLED: '已取消',
}

const statusType: Record<string, 'success' | 'info' | 'error' | 'warning'> = {
  COMPLETED: 'success', IN_PROGRESS: 'info', CANCELLED: 'error', PENDING: 'warning',
}

const tabs = [
  { key: 'buy', label: '购买', orders: () => buyerOrders.value, isSeller: () => false,
    counterLabel: (_: any) => '卖家', counterName: (o: any) => o.sellerNickname, counterAvatar: (o: any) => o.sellerAvatar, counterId: (o: any) => o.sellerId },
  { key: 'sell', label: '出售', orders: () => sellerOrders.value, isSeller: () => true,
    counterLabel: (_: any) => '买家', counterName: (o: any) => o.buyerNickname, counterAvatar: (o: any) => o.buyerAvatar, counterId: (o: any) => o.buyerId },
  { key: 'exchange', label: '置换', orders: () => displayedOrders.value, isSeller: (o: any) => o.sellerId === userStore.user?.id,
    counterLabel: (o: any) => o.sellerId === userStore.user?.id ? '买家' : '卖家',
    counterName: (o: any) => o.sellerId === userStore.user?.id ? o.buyerNickname : o.sellerNickname,
    counterAvatar: (o: any) => o.sellerId === userStore.user?.id ? o.buyerAvatar : o.sellerAvatar,
    counterId: (o: any) => o.sellerId === userStore.user?.id ? o.buyerId : o.sellerId },
]

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
  const targetId = tab.value === 'buy' ? order.sellerId : order.buyerId
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
      <NTabs v-model:value="tab" type="line">
        <NTabPane v-for="tabInfo in tabs" :key="tabInfo.key" :name="tabInfo.key" :tab="tabInfo.label">
          <NSpin :show="loading">
            <div v-if="tabInfo.orders().length > 0" class="mt-4 space-y-3">
              <div v-for="order in tabInfo.orders()" :key="order.id">
                <NCard :bordered="true" style="border-radius: 12px" class="cursor-pointer hover:shadow-md transition-shadow" @click="goToOrder(order.id)">
                  <div class="flex items-start gap-4">
                    <img :src="order.goodsCoverImage || ''" class="w-20 h-20 object-cover rounded-lg"
                      @click.stop="goToGoods(order.goodsId)" />
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center justify-between flex-wrap gap-2">
                        <span class="font-semibold text-gray-800 cursor-pointer hover:text-[#3B82F6] truncate" @click.stop="goToGoods(order.goodsId)">{{ order.goodsTitle }}</span>
                        <NTag size="small" :type="statusType[order.status] || 'default'">{{ statusLabels[order.status] || order.status }}</NTag>
                      </div>
                      <div class="text-[#3B82F6] font-bold text-lg mt-1">
                        <template v-if="order.goodsTradeType === 'EXCHANGE'">🔄 置换</template>
                        <template v-else>{{ formatPrice(order.amount) }}</template>
                      </div>
                      <div v-if="order.goodsTradeType === 'EXCHANGE' && order.exchangeGoodsId" class="flex items-center gap-2 mt-2 p-2 bg-green-50 rounded-lg">
                        <img :src="order.exchangeGoodsCoverImage || ''" class="w-10 h-10 object-cover rounded shrink-0" />
                        <span class="text-xs text-gray-600">⇄ 置换商品：{{ order.exchangeGoodsTitle }}</span>
                      </div>
                      <div v-if="order.meetTime || order.meetPlace" class="text-xs text-gray-500 mt-1">
                        <span v-if="order.meetTime">🕐 {{ order.meetTime }}</span>
                        <span v-if="order.meetPlace" class="ml-3">📍 {{ order.meetPlace }}</span>
                      </div>
                        <div class="flex items-center justify-between mt-2">
                          <div class="flex items-center gap-2 text-xs text-gray-400">
                            <span class="text-gray-400">{{ tabInfo.counterLabel(order) }}:</span>
                            <img :src="getAvatarUrl(tabInfo.counterAvatar(order), 'thumb_64')" class="w-5 h-5 rounded-full object-cover cursor-pointer shrink-0"
                              @click.stop="router.push('/profile?userId=' + tabInfo.counterId(order))" />
                            <span class="text-gray-600 cursor-pointer hover:text-[#3B82F6]"
                              @click.stop="router.push('/profile?userId=' + tabInfo.counterId(order))">{{ tabInfo.counterName(order) }}</span>
                          <span class="ml-2">{{ formatDate(order.createTime) }}</span>
                        </div>
                        <NSpace @click.stop>
                          <template v-if="tabInfo.isSeller(order)">
                            <NButton v-if="order.status === 'PENDING'" size="tiny" type="primary" @click="handleConfirm(order.id)">确认</NButton>
                            <NButton v-if="order.status === 'PENDING'" size="tiny" @click="handleCancel(order.id)">拒绝</NButton>
                            <NButton v-if="order.status === 'COMPLETED'" size="tiny" @click="openReviewModal(order)">评价买家</NButton>
                          </template>
                          <template v-else>
                            <NButton v-if="order.status === 'PENDING'" size="tiny" type="error" @click="handleCancel(order.id)">取消</NButton>
                            <NButton v-if="order.status === 'IN_PROGRESS'" size="tiny" type="success" @click="handleComplete(order.id)">确认收货</NButton>
                            <NButton v-if="order.status === 'COMPLETED'" size="tiny" @click="openReviewModal(order)">评价卖家</NButton>
                          </template>
                        </NSpace>
                      </div>
                    </div>
                  </div>
                </NCard>
              </div>
            </div>
            <NEmpty v-else :description="'暂无' + tabInfo.label + '订单'" class="mt-4" />
          </NSpin>
        </NTabPane>
      </NTabs>
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
