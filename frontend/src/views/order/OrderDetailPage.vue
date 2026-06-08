<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, confirmOrder, cancelOrder, completeOrder, requestCancelOrder, approveCancelOrder, rejectCancelOrder, createDispute } from '@/api/order'
import { getReviewByOrderId, createReview } from '@/api/review'
import { getImageUrl, formatPrice, formatDate } from '@/utils'
import { useUserStore } from '@/stores/user'
import { ORDER_STATUS } from '@/constants'
import {
  NCard, NSpin, NButton, NTag, NAvatar, NIcon, NDivider, NTimeline, NTimelineItem,
  NInput, NModal, NRate, NEmpty,
  useMessage, useDialog,
} from 'naive-ui'
import {
  ArrowLeft24Filled, CheckmarkCircle24Filled, DismissCircle24Filled,
  Clock24Filled, Cart24Filled,
} from '@vicons/fluent'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const message = useMessage()
const dialog = useDialog()

const order = ref<any>(null)
const loading = ref(true)
const actionLoading = ref(false)

const orderId = computed(() => Number(route.params.id))
const isBuyer = computed(() => order.value?.buyerId === userStore.user?.id)
const isSeller = computed(() => order.value?.sellerId === userStore.user?.id)
const roleLabel = computed(() => isBuyer.value ? '买家' : '卖家')
const hasBeenRejected = computed(() => order.value?.logs?.some((l: any) => l.action === 'CANCEL_REJECT'))

const statusConfig = computed(() => {
  const s = order.value?.status
  const map: Record<string, { color: string; label: string; icon: any }> = {
    PENDING: { color: '#F59E0B', label: '待确认', icon: Clock24Filled },
    IN_PROGRESS: { color: '#3B82F6', label: '交易中', icon: Cart24Filled },
    CANCEL_REQUESTED: { color: '#F97316', label: '取消申请中', icon: Clock24Filled },
    DISPUTED: { color: '#8B5CF6', label: '仲裁中', icon: Clock24Filled },
    COMPLETED: { color: '#10B981', label: '已完成', icon: CheckmarkCircle24Filled },
    CANCELLED: { color: '#EF4444', label: '已取消', icon: DismissCircle24Filled },
  }
  return map[s] || { color: '#999', label: s, icon: Clock24Filled }
})

async function loadOrder() {
  loading.value = true
  try {
    const res = await getOrderDetail(orderId.value)
    order.value = res.data
    if (res.data.status === 'COMPLETED') {
      loadReviews()
    }
  } catch {
    message.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

async function loadReviews() {
  try {
    const res = await getReviewByOrderId(orderId.value)
    const reviews = res.data || []
    myReview.value = reviews.find((r: any) => r.reviewerId === userStore.user?.id) || null
    otherReview.value = reviews.find((r: any) => r.reviewerId !== userStore.user?.id) || null
  } catch {
    // no reviews yet
  }
}

async function handleSubmitReview() {
  submittingReview.value = true
  try {
    await createReview({ orderId: orderId.value, rating: reviewRating.value, content: reviewContent.value })
    message.success('评价成功')
    loadReviews()
  } catch (err: any) {
    message.error(err.message || '评价失败')
  } finally {
    submittingReview.value = false
  }
}

function handleConfirm() {
  dialog.success({
    title: '确认订单',
    content: '确认后商品将被锁定，交易正式开始',
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      actionLoading.value = true
      try {
        await confirmOrder(orderId.value)
        message.success('订单已确认')
        loadOrder()
      } catch { message.error('操作失败') }
      finally { actionLoading.value = false }
    },
  })
}

function handleCancel() {
  dialog.warning({
    title: '取消交易',
    content: '确定要取消这笔交易吗？',
    positiveText: '确定取消',
    negativeText: '返回',
    onPositiveClick: async () => {
      actionLoading.value = true
      try {
        await cancelOrder(orderId.value)
        message.success('交易已取消')
        loadOrder()
      } catch { message.error('操作失败') }
      finally { actionLoading.value = false }
    },
  })
}

function handleComplete() {
  dialog.success({
    title: '确认完成',
    content: '确认交易已完成？完成后可以对对方进行评价',
    positiveText: '确认完成',
    negativeText: '返回',
    onPositiveClick: async () => {
      actionLoading.value = true
      try {
        await completeOrder(orderId.value)
        message.success('交易已完成')
        loadOrder()
      } catch { message.error('操作失败') }
      finally { actionLoading.value = false }
    },
  })
}

const timelineSteps = [
  { key: 'PENDING', label: '买家下单', desc: '等待卖家确认' },
  { key: 'IN_PROGRESS', label: '卖家确认', desc: '交易进行中' },
  { key: 'COMPLETED', label: '交易完成', desc: '双方确认完成' },
]

const showModal = ref(false)
const modalType = ref<'cancel' | 'dispute'>('cancel')
const modalReason = ref('')

const myReview = ref<any>(null)
const otherReview = ref<any>(null)
const reviewRating = ref(5)
const reviewContent = ref('')
const submittingReview = ref(false)

function openCancelModal() {
  modalType.value = 'cancel'
  modalReason.value = ''
  showModal.value = true
}

function openDisputeModal() {
  modalType.value = 'dispute'
  modalReason.value = ''
  showModal.value = true
}

async function handleRequestCancel() {
  if (!modalReason.value.trim()) { message.warning('请填写取消原因'); return }
  actionLoading.value = true
  try {
    await requestCancelOrder(orderId.value, modalReason.value)
    message.success('已提交取消申请')
    showModal.value = false
    loadOrder()
  } catch { message.error('操作失败') }
  finally { actionLoading.value = false }
}

function handleApproveCancel() {
  dialog.success({
    title: '同意取消',
    content: '确定同意卖家的取消申请吗？',
    positiveText: '同意',
    negativeText: '返回',
    onPositiveClick: async () => {
      actionLoading.value = true
      try {
        await approveCancelOrder(orderId.value)
        message.success('已同意取消')
        loadOrder()
      } catch { message.error('操作失败') }
      finally { actionLoading.value = false }
    },
  })
}

function handleRejectCancel() {
  dialog.warning({
    title: '拒绝取消',
    content: '确定拒绝卖家的取消申请吗？交易将继续进行',
    positiveText: '拒绝',
    negativeText: '返回',
    onPositiveClick: async () => {
      actionLoading.value = true
      try {
        await rejectCancelOrder(orderId.value)
        message.success('已拒绝取消')
        loadOrder()
      } catch { message.error('操作失败') }
      finally { actionLoading.value = false }
    },
  })
}

async function handleCreateDispute() {
  if (!modalReason.value.trim()) { message.warning('请填写申请原因'); return }
  actionLoading.value = true
  try {
    await createDispute(orderId.value, modalReason.value)
    message.success('已申请仲裁')
    showModal.value = false
    loadOrder()
  } catch { message.error('操作失败') }
  finally { actionLoading.value = false }
}

onMounted(loadOrder)
</script>

<template>
  <div class="max-w-700px mx-auto pb-8">
    <NButton quaternary size="small" class="mb-4" @click="router.back()">
      <template #icon><NIcon><ArrowLeft24Filled /></NIcon></template>
      返回
    </NButton>

    <NSpin :show="loading">
      <template v-if="order">
        <!-- Status Header -->
        <NCard :bordered="true" style="border-radius: 12px" class="mb-4">
          <div class="flex items-center gap-3">
            <div class="w-12 h-12 rounded-full flex items-center justify-center" :style="{ backgroundColor: statusConfig.color + '20' }">
              <NIcon :size="28" :color="statusConfig.color"><component :is="statusConfig.icon" /></NIcon>
            </div>
            <div>
              <div class="text-lg font-bold" :style="{ color: statusConfig.color }">{{ statusConfig.label }}</div>
              <div class="text-xs text-gray-400">订单号 {{ order.id }}</div>
            </div>
          </div>

          <!-- Progress -->
          <div class="flex items-center mt-6 relative">
            <div class="absolute top-3 left-6 right-6 h-0.5 bg-gray-100" />
            <div
              class="absolute top-3 left-6 h-0.5 transition-all duration-500"
              :style="{
                backgroundColor: statusConfig.color,
                width: order.status === 'PENDING' ? '0%' : order.status === 'IN_PROGRESS' ? '50%' : '100%',
              }"
            />
              <template v-for="(step, idx) in timelineSteps" :key="step.key">
              <div class="flex-1 flex flex-col items-center relative z-10">
                <div
                  class="w-6 h-6 rounded-full flex items-center justify-center text-xs text-white font-bold"
                  :style="{
                    backgroundColor: ['PENDING', 'IN_PROGRESS', 'COMPLETED'].indexOf(order.status) >= idx || order.status === 'CANCEL_REQUESTED' || order.status === 'DISPUTED'
                      ? statusConfig.color : '#E5E7EB'
                  }"
                >{{ idx + 1 }}</div>
                <span class="text-xs mt-2 text-center" :class="timelineSteps.indexOf(timelineSteps.find(s => s.key === order.status) || timelineSteps[0]) >= idx ? 'text-gray-800 font-semibold' : 'text-gray-400'">{{ step.label }}</span>
              </div>
            </template>
          </div>
        </NCard>

        <!-- Goods Info -->
        <NCard :bordered="true" style="border-radius: 12px" class="mb-4">
          <div class="flex items-center gap-3 cursor-pointer" @click="router.push(`/goods/${order.goodsId}`)">
            <img v-if="order.goodsCoverImage" :src="getImageUrl(order.goodsCoverImage)" class="w-16 h-16 object-cover rounded-lg" />
            <div class="flex-1 min-w-0">
              <div class="text-sm font-semibold text-gray-800 truncate">{{ order.goodsTitle }}</div>
              <div class="flex items-center gap-2 mt-1">
                <span class="text-[#3B82F6] font-bold">{{ formatPrice(order.amount) }}</span>
                <NTag v-if="order.goodsTradeType === 'EXCHANGE'" size="tiny" type="success">置换</NTag>
              </div>
            </div>
          </div>
          <div v-if="order.exchangeGoodsTitle" class="mt-3 pt-3 border-t border-gray-100 flex items-center gap-3">
            <img v-if="order.exchangeGoodsCoverImage" :src="getImageUrl(order.exchangeGoodsCoverImage)" class="w-12 h-12 object-cover rounded-lg" />
            <div class="text-sm text-gray-500">置换商品：<span class="font-semibold text-gray-700">{{ order.exchangeGoodsTitle }}</span></div>
          </div>
        </NCard>

        <!-- Participants -->
        <NCard :bordered="true" style="border-radius: 12px" class="mb-4">
          <div class="space-y-3">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <NAvatar :src="getImageUrl(order.buyerAvatar)" :size="32" round style="background-color: #3B82F6">
                  {{ order.buyerNickname?.charAt(0) || 'B' }}
                </NAvatar>
                <div>
                  <span class="text-sm font-semibold">{{ order.buyerNickname }}</span>
                  <span class="text-xs text-gray-400 ml-1" v-if="isBuyer">(我)</span>
                </div>
              </div>
              <NTag size="tiny" type="info">买家</NTag>
            </div>
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <NAvatar :src="getImageUrl(order.sellerAvatar)" :size="32" round style="background-color: #10B981">
                  {{ order.sellerNickname?.charAt(0) || 'S' }}
                </NAvatar>
                <div>
                  <span class="text-sm font-semibold">{{ order.sellerNickname }}</span>
                  <span class="text-xs text-gray-400 ml-1" v-if="isSeller">(我)</span>
                </div>
              </div>
              <NTag size="tiny" type="warning">卖家</NTag>
            </div>
          </div>
        </NCard>

        <!-- Cancel Request Info -->
        <NCard v-if="order.status === 'CANCEL_REQUESTED' || order.status === 'DISPUTED'" :bordered="true" style="border-radius: 12px" class="mb-4">
          <template #header>
            <span class="text-sm font-bold" :class="order.status === 'CANCEL_REQUESTED' ? 'text-orange-500' : 'text-purple-500'">
              {{ order.status === 'CANCEL_REQUESTED' ? '取消申请' : '仲裁中' }}
            </span>
          </template>
          <div class="space-y-2 text-sm">
            <div v-if="order.cancelRequesterName" class="flex gap-2">
              <span class="text-gray-400 w-16 shrink-0">申请人</span>
              <span class="text-gray-700">{{ order.cancelRequesterName }}</span>
            </div>
            <div v-if="order.cancelReason" class="flex gap-2">
              <span class="text-gray-400 w-16 shrink-0">原因</span>
              <span class="text-gray-700">{{ order.cancelReason }}</span>
            </div>
          </div>
        </NCard>

        <!-- Meeting Info -->
        <NCard v-if="order.meetTime || order.meetPlace || order.remark" :bordered="true" style="border-radius: 12px" class="mb-4">
          <template #header><span class="text-sm font-bold">交易信息</span></template>
          <div class="space-y-2 text-sm">
            <div v-if="order.meetTime" class="flex gap-2">
              <span class="text-gray-400 w-16 shrink-0">约定时间</span>
              <span class="text-gray-700">{{ order.meetTime }}</span>
            </div>
            <div v-if="order.meetPlace" class="flex gap-2">
              <span class="text-gray-400 w-16 shrink-0">约定地点</span>
              <span class="text-gray-700">{{ order.meetPlace }}</span>
            </div>
            <div v-if="order.remark" class="flex gap-2">
              <span class="text-gray-400 w-16 shrink-0">备注</span>
              <span class="text-gray-700">{{ order.remark }}</span>
            </div>
          </div>
        </NCard>

        <!-- Logs -->
        <NCard v-if="order.logs && order.logs.length > 0" :bordered="true" style="border-radius: 12px" class="mb-4">
          <template #header><span class="text-sm font-bold">操作记录</span></template>
          <div class="space-y-3">
            <div v-for="log in order.logs" :key="log.id" class="flex items-start gap-3">
              <div class="w-2 h-2 rounded-full bg-[#3B82F6] mt-1.5 shrink-0" />
              <div>
                <div class="text-sm text-gray-700">{{ log.remark }}</div>
                <div class="text-xs text-gray-400 mt-0.5">{{ log.operatorName }} · {{ formatDate(log.createTime) }}</div>
              </div>
            </div>
          </div>
        </NCard>

        <!-- Actions -->
        <NCard :bordered="true" style="border-radius: 12px" v-if="order.status !== 'COMPLETED' && order.status !== 'CANCELLED'">
          <div class="space-y-2">
            <!-- Pending: seller confirms, buyer cancels -->
            <template v-if="order.status === 'PENDING'">
              <NButton v-if="isSeller" type="primary" block :loading="actionLoading" @click="handleConfirm">确认订单</NButton>
              <NButton v-if="isSeller" block quaternary :loading="actionLoading" @click="handleCancel">拒绝</NButton>
              <NButton v-if="isBuyer" type="error" block quaternary :loading="actionLoading" @click="handleCancel">取消交易</NButton>
            </template>
            <!-- In Progress: buyer completes, both can request cancel -->
            <template v-if="order.status === 'IN_PROGRESS'">
              <NButton v-if="isBuyer" type="success" block :loading="actionLoading" @click="handleComplete">确认完成</NButton>
              <NButton v-if="isSeller" block disabled>等待买家确认完成</NButton>
              <NButton type="warning" block quaternary @click="openCancelModal">申请取消交易</NButton>
              <NButton v-if="hasBeenRejected" type="warning" block quaternary @click="openDisputeModal">申请仲裁</NButton>
            </template>
            <!-- Cancel Requested: non-requester approves/rejects, dispute only after prior rejection -->
            <template v-if="order.status === 'CANCEL_REQUESTED'">
              <template v-if="Number(order.cancelRequesterId) !== Number(userStore.user?.id)">
                <NButton type="success" block :loading="actionLoading" @click="handleApproveCancel">同意取消</NButton>
                <NButton block quaternary :loading="actionLoading" @click="handleRejectCancel">拒绝取消</NButton>
                <NButton v-if="hasBeenRejected" type="warning" block quaternary @click="openDisputeModal">申请仲裁</NButton>
              </template>
              <template v-else>
                <NButton block disabled>等待对方处理取消申请</NButton>
                <NButton v-if="hasBeenRejected" type="warning" block quaternary @click="openDisputeModal">申请仲裁</NButton>
              </template>
            </template>
            <!-- Disputed: waiting for admin -->
            <template v-if="order.status === 'DISPUTED'">
              <NButton block disabled>等待管理员仲裁</NButton>
            </template>
          </div>
        </NCard>

        <!-- Completed: reviews -->
        <template v-if="order.status === 'COMPLETED'">
          <!-- Other's review - only shown after my review is submitted -->
          <NCard v-if="otherReview && myReview" :bordered="true" style="border-radius: 12px" class="mb-4">
            <template #header><span class="text-sm font-bold">对方的评价</span></template>
            <div class="flex items-center gap-2 mb-2">
              <NAvatar :src="getImageUrl(otherReview.reviewerAvatar)" :size="28" round style="background-color: #3B82F6">
                {{ otherReview.reviewerNickname?.charAt(0) || 'U' }}
              </NAvatar>
              <span class="text-sm font-semibold">{{ otherReview.reviewerNickname }}</span>
            </div>
            <div class="text-yellow-400 text-lg mb-1">
              {{ '★'.repeat(otherReview.rating || 0) }}{{ '☆'.repeat(5 - (otherReview.rating || 0)) }}
            </div>
            <p class="text-sm text-gray-600">{{ otherReview.content || '未填写评价内容' }}</p>
          </NCard>
          <NCard v-else-if="otherReview && !myReview" :bordered="true" style="border-radius: 12px" class="mb-4">
            <div class="text-center text-sm text-gray-400 py-2">
              对方已评价，完成评价后即可查看
            </div>
          </NCard>

          <!-- My review -->
          <NCard v-if="myReview" :bordered="true" style="border-radius: 12px" class="mb-4">
            <template #header><span class="text-sm font-bold">我的评价</span></template>
            <div class="text-yellow-400 text-lg mb-1">
              {{ '★'.repeat(myReview.rating || 0) }}{{ '☆'.repeat(5 - (myReview.rating || 0)) }}
            </div>
            <p class="text-sm text-gray-600">{{ myReview.content || '未填写评价内容' }}</p>
          </NCard>

          <!-- Review form -->
          <NCard v-if="!myReview" :bordered="true" style="border-radius: 12px">
            <template #header><span class="text-sm font-bold">评价对方</span></template>
            <div class="space-y-4">
              <div class="text-center">
                <NRate v-model:value="reviewRating" size="large" />
              </div>
              <NInput v-model:value="reviewContent" type="textarea" placeholder="写下你的评价（选填）" :rows="3" />
              <NButton type="primary" block :loading="submittingReview" @click="handleSubmitReview">提交评价</NButton>
            </div>
          </NCard>

          <!-- Completed footer -->
          <NCard :bordered="true" style="border-radius: 12px">
            <div class="text-center text-sm text-gray-500">
              交易已完成
              <NButton type="primary" text size="small" class="ml-2" @click="router.push('/my-orders')">查看所有订单</NButton>
            </div>
          </NCard>
        </template>
      </template>
    </NSpin>

    <!-- Cancel/Dispute Modal -->
    <NModal v-model:show="showModal" :title="modalType === 'cancel' ? '申请取消交易' : '申请仲裁'" preset="card" style="width: 420px; border-radius: 12px">
      <div class="space-y-4">
        <p class="text-sm text-gray-500">{{ modalType === 'cancel' ? '请填写取消原因，提交后等待买家处理' : '请填写仲裁原因，提交后由管理员审核' }}</p>
        <NInput v-model:value="modalReason" type="textarea" :placeholder="modalType === 'cancel' ? '请输入取消原因...' : '请输入仲裁原因...'" :rows="4" :maxlength="500" show-count />
        <NButton type="primary" block :loading="actionLoading" @click="modalType === 'cancel' ? handleRequestCancel() : handleCreateDispute()">
          提交{{ modalType === 'cancel' ? '取消申请' : '仲裁申请' }}
        </NButton>
      </div>
    </NModal>
  </div>
</template>

<style scoped>
.max-w-700px { max-width: 700px; }
</style>
