<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getGoodsDetail, toggleGoodsStatus, deleteGoods, getMyGoods } from '@/api/goods'
import { addFavorite, removeFavorite } from '@/api/favorite'
import { addFollow, removeFollow } from '@/api/follow'
import { createOrder } from '@/api/order'
import { getComments, createComment } from '@/api/comment'
import { createReport } from '@/api/report'
import { getImageUrl, getAvatarUrl, formatPrice, formatDate } from '@/utils'
import { useUserStore } from '@/stores/user'
import { GOODS_CONDITIONS, GOODS_STATUS } from '@/constants'
import type { Goods, Review } from '@/types/entity'
import {
  NButton,
  NCard,
  NTag,
  NIcon,
  NSpace,
  NAvatar,
  NDivider,
  NSpin,
  NEmpty,
  NResult,
  NModal,
  NInput,
  NRadio,
  NRadioGroup,
  NStatistic,
  NGrid,
  NGi,
  NDatePicker,
  useMessage,
  useDialog,
} from 'naive-ui'
import { zhCN, dateZhCN } from 'naive-ui'
import {
  Heart24Filled,
  Heart24Regular,
  Chat24Filled,
  Share24Filled,
  Flag24Filled,
  Eye24Filled,
} from '@vicons/fluent'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const message = useMessage()
const dialog = useDialog()

const goods = ref<any>(null)
const comments = ref<any[]>([])
const loading = ref(true)
const commentText = ref('')
const replyTo = ref<{ id: number; name: string } | null>(null)
const submittingComment = ref(false)
const error = ref('')
const isFavorited = ref(false)
const isFollowing = ref(false)
const showBuyModal = ref(false)
const buyMessage = ref('')
const meetTime = ref('')
const meetTimeTs = ref<number | null>(null)
const meetPlace = ref('')
const exchangeOfferId = ref<number | null>(null)
const myExchangeGoods = ref<any[]>([])
const submitting = ref(false)
const currentImageIndex = ref(0)

// 举报相关状态
const showReportModal = ref(false)
const reportReason = ref('')
const reportDescription = ref('')
const submittingReport = ref(false)

const goodsId = computed(() => Number(route.params.id))

const isOwner = computed(() => goods.value?.userId === userStore.user?.id)

async function fetchData() {
  loading.value = true
  error.value = ''
  try {
    const goodsRes = await getGoodsDetail(goodsId.value)
    goods.value = goodsRes.data
    isFavorited.value = goodsRes.data.isFavorited
    isFollowing.value = goodsRes.data.isFollowed
    if (goodsRes.data.id) {
      try {
        const commentRes = await getComments(goodsRes.data.id)
        comments.value = commentRes.data || []
      } catch { /* ignore */ }
    }
  } catch {
    error.value = '商品不存在或已下架'
  } finally {
    loading.value = false
  }
}

function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    router.push('/login')
    return
  }
  if (!goods.value) return

  if (isFavorited.value) {
    removeFavorite(goods.value.id)
      .then(() => {
        isFavorited.value = false
        if (goods.value) goods.value.favoriteCount--
        message.success('已取消收藏')
      })
      .catch(() => message.error('操作失败'))
  } else {
    addFavorite(goods.value.id)
      .then(() => {
        isFavorited.value = true
        if (goods.value) goods.value.favoriteCount++
        message.success('已收藏')
      })
      .catch(() => message.error('操作失败'))
  }
}

function toggleFollow() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    router.push('/login')
    return
  }
  if (!goods.value?.userId) return

  if (isFollowing.value) {
    removeFollow(goods.value.userId)
      .then(() => {
        isFollowing.value = false
        message.success('已取消关注')
      })
      .catch(() => message.error('操作失败'))
  } else {
    addFollow(goods.value.userId)
      .then(() => {
        isFollowing.value = true
        message.success('已关注')
      })
      .catch(() => message.error('操作失败'))
  }
}

function startChat() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    router.push('/login')
    return
  }
  router.push(`/chat?goodsId=${goodsId.value}`)
}

async function handleBuy() {
  if (!userStore.isLoggedIn) { message.warning('请先登录'); router.push('/login'); return }
  if (goods.value?.tradeType === 'EXCHANGE') {
    try {
      const res = await getMyGoods()
      const allGoods = (Array.isArray(res.data) ? res.data : (res as any)?.data?.data || []) as any[]
      myExchangeGoods.value = allGoods.filter((g: any) =>
        g.status === 'ACTIVE' && g.id !== goods.value?.id)
      console.log('myGoods:', allGoods.length, 'filtered:', myExchangeGoods.value.length)
    } catch (e: any) {
      console.error('加载我的商品失败:', e?.message || e)
      myExchangeGoods.value = []
    }
  }
  showBuyModal.value = true
}

async function submitOrder() {
  if (!goods.value) return
  submitting.value = true
  try {
    // 格式化时间
    let formattedTime = ''
    if (meetTimeTs.value) {
      const d = new Date(meetTimeTs.value)
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const h = String(d.getHours()).padStart(2, '0')
      const min = String(d.getMinutes()).padStart(2, '0')
      formattedTime = `${y}-${m}-${day} ${h}:${min}`
    }
    await createOrder({
      goodsId: goods.value.id,
      exchangeGoodsId: goods.value.tradeType === 'EXCHANGE' ? exchangeOfferId.value : undefined,
      remark: buyMessage.value,
      meetTime: formattedTime,
      meetPlace: meetPlace.value,
    })
    message.success(goods.value.tradeType === 'EXCHANGE' ? '置换请求已发送，等待对方确认' : '下单成功，等待卖家确认')
    showBuyModal.value = false
    buyMessage.value = ''
    meetTimeTs.value = null
    meetPlace.value = ''
    exchangeOfferId.value = null
  } catch (err: unknown) {
    const e = err as { message?: string }
    message.error(e.message || '下单失败')
  } finally {
    submitting.value = false
  }
}

async function handleSubmitComment() {
  const text = commentText.value.trim()
  if (!text) return
  submittingComment.value = true
  try {
    await createComment({ goodsId: goodsId.value, content: text, parentId: replyTo.value?.id })
    message.success(replyTo.value ? '回复成功' : '留言成功')
    commentText.value = ''
    replyTo.value = null
    fetchData()
  } catch { message.error('留言失败') } finally { submittingComment.value = false }
}

function handleReply(comment: any) {
  replyTo.value = { id: comment.id, name: comment.userNickname }
  commentText.value = ''
}

// 举报相关函数
function openReportModal() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    router.push('/login')
    return
  }
  reportReason.value = ''
  reportDescription.value = ''
  showReportModal.value = true
}

function handleRelist() {
  dialog.info({
    title: '重新上架',
    content: '将重新提交审核，审核通过后商品将恢复上架',
    positiveText: '确认上架',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await toggleGoodsStatus(goodsId.value, 'PENDING_REVIEW')
        message.success('已重新提交审核')
        fetchData()
      } catch {
        message.error('操作失败')
      }
    },
  })
}

function handleDeleteGoods() {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除该商品吗？此操作不可恢复。',
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteGoods(goodsId.value)
        message.success('删除成功')
        router.push('/my-goods')
      } catch {
        message.error('删除失败')
      }
    },
  })
}

async function submitReport() {
  if (!reportReason.value) {
    message.warning('请选择举报理由')
    return
  }
  if (!goods.value) return
  
  submittingReport.value = true
  try {
    await createReport({
      targetId: goods.value.id,
      type: 'GOODS',
      reason: reportReason.value,
      description: reportDescription.value || undefined,
    })
    message.success('举报已提交，感谢您的反馈')
    showReportModal.value = false
    reportReason.value = ''
    reportDescription.value = ''
  } catch (err: unknown) {
    const e = err as { message?: string }
    message.error(e.message || '举报失败')
  } finally {
    submittingReport.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="pb-8">
    <NSpin :show="loading">
      <template v-if="goods">
        <div class="grid grid-cols-1 md:grid-cols-5 gap-6">
          <!-- Image Gallery -->
          <div class="md:col-span-3">
            <NCard :bordered="true" style="border-radius: 12px; overflow: hidden">
              <div v-if="goods.images && goods.images.length > 0">
                <div class="image-container">
                  <img
                    :src="getImageUrl(goods.images[currentImageIndex])"
                    :alt="goods.title"
                    class="goods-main-image"
                  />
                </div>
                <div v-if="goods.images.length > 1" class="flex gap-2 mt-3 overflow-x-auto">
                  <img
                    v-for="(img, idx) in goods.images"
                    :key="idx"
                    :src="getImageUrl(img)"
                    class="w-16 h-16 object-cover rounded-lg cursor-pointer border-2 transition-all"
                    :class="idx === currentImageIndex ? 'border-[#3B82F6]' : 'border-transparent'"
                    @click="currentImageIndex = idx"
                    @error="(e: Event) => { (e.target as HTMLImageElement).style.display = 'none' }"
                  />
                </div>
              </div>
              <NEmpty v-else description="暂无图片" />
            </NCard>
          </div>

          <!-- Goods Info -->
          <div class="md:col-span-2 space-y-4">
            <NCard :bordered="true" style="border-radius: 12px">
              <div class="space-y-4">
                <div class="flex items-center gap-2">
                  <NTag
                    :color="{
                      textColor: '#fff',
                      borderColor: goods.status === 'active' ? '#10B981' : '#EF4444',
                      color: goods.status === 'active' ? '#10B981' : '#EF4444',
                    }"
                  >
                    {{ goods.status === 'ACTIVE' ? '在售' : goods.status === 'PENDING_REVIEW' ? '审核中' : goods.status === 'INACTIVE' ? '已下架' : goods.status === 'REJECTED' ? '未通过' : goods.status === 'TAKEN_DOWN' ? '强制下架' : goods.status }}
                  </NTag>
                  <NTag v-if="goods.categoryName" size="small">{{ goods.categoryName }}</NTag>
                  <NTag size="small">{{ GOODS_CONDITIONS[goods.condition] || goods.condition }}</NTag>
                  <NTag v-if="goods.tradeType === 'EXCHANGE'" size="small" type="success">置换</NTag>
                  <NTag v-else size="small" type="info">出售</NTag>
                </div>

                <h1 class="text-xl font-bold text-gray-800">{{ goods.title }}</h1>

                <div v-if="goods.tradeType !== 'EXCHANGE'" class="flex items-baseline gap-2">
                  <span class="text-2xl font-bold text-[#3B82F6]">{{ formatPrice(goods.price) }}</span>
                  <span v-if="goods.originalPrice" class="text-sm text-gray-400 line-through">{{ formatPrice(goods.originalPrice) }}</span>
                </div>
                <div v-else class="flex items-center gap-2">
                  <span class="text-lg text-green-600 font-bold">🔄 仅支持置换</span>
                  <span class="text-xs text-gray-400">（以物易物，不涉及金钱交易）</span>
                </div>

                <div class="flex items-center gap-4 text-sm text-gray-400">
                  <span class="flex items-center gap-1">
                    <NIcon size="14"><Eye24Filled /></NIcon>
                    {{ goods.viewCount || 0 }} 次浏览
                  </span>
                  <span class="flex items-center gap-1">
                    <NIcon size="14"><Heart24Regular /></NIcon>
                    {{ goods.favoriteCount || 0 }} 收藏
                  </span>
                </div>

                <NDivider />

                <div class="text-sm text-gray-600 leading-relaxed whitespace-pre-wrap">
                  {{ goods.description }}
                </div>

                <div class="text-xs text-gray-400">发布于 {{ formatDate(goods.createTime) }}</div>
              </div>
            </NCard>

            <!-- Seller Info -->
            <NCard :bordered="true" style="border-radius: 12px">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3 cursor-pointer" @click="router.push(`/profile?userId=${goods.userId}`)">
                  <img 
                    :src="getAvatarUrl(goods.userAvatar, 'thumb_128')" 
                    class="w-12 h-12 rounded-full object-cover"
                  />
                  <div>
                    <div class="font-semibold text-gray-800">{{ goods.userNickname }}</div>
                    <div class="text-xs text-gray-400">信用分 {{ goods.userCreditScore || 0 }}</div>
                  </div>
                </div>
                <NButton
                  v-if="!isOwner && userStore.isLoggedIn"
                  :type="isFollowing ? 'default' : 'primary'"
                  size="small"
                  @click="toggleFollow"
                >
                  {{ isFollowing ? '已关注' : '+ 关注' }}
                </NButton>
              </div>
            </NCard>

            <!-- Owner Actions -->
            <NCard :bordered="true" style="border-radius: 12px" v-if="isOwner">
              <div class="space-y-3">
                <div class="flex items-center gap-2 text-sm text-gray-500">
                  <span>商品状态：</span>
                  <NTag
                    :type="goods.status === 'ACTIVE' ? 'success' : goods.status === 'TAKEN_DOWN' || goods.status === 'REJECTED' ? 'error' : 'warning'"
                    size="small"
                  >{{ GOODS_STATUS[goods.status] || goods.status }}</NTag>
                </div>
                <div class="flex gap-2">
                  <NButton type="primary" size="small" @click="router.push(`/goods/${goodsId}/edit`)">
                    编辑商品
                  </NButton>
                  <NButton
                    v-if="goods.status === 'TAKEN_DOWN' || goods.status === 'INACTIVE'"
                    type="success"
                    size="small"
                    @click="handleRelist"
                  >
                    重新上架
                  </NButton>
                  <NButton
                    v-if="goods.status === 'TAKEN_DOWN' || goods.status === 'INACTIVE'"
                    type="error"
                    size="small"
                    quaternary
                    @click="handleDeleteGoods"
                  >
                    删除商品
                  </NButton>
                </div>
              </div>
            </NCard>

            <!-- Actions -->
            <NCard :bordered="true" style="border-radius: 12px" v-if="!isOwner">
              <div class="flex gap-3">
                <NButton type="primary" size="large" block :disabled="goods.status !== 'ACTIVE'" @click="handleBuy">
                  {{ goods.tradeType === 'EXCHANGE' ? '发起置换' : '立即购买' }}
                </NButton>
                <NButton size="large" @click="startChat">
                  <template #icon><NIcon><Chat24Filled /></NIcon></template>
                  私聊
                </NButton>
              </div>
              <div class="flex gap-2 mt-3">
                <NButton quaternary size="small" @click="toggleFavorite">
                  <template #icon>
                    <NIcon :color="isFavorited ? '#EF4444' : undefined">
                      <Heart24Filled v-if="isFavorited" />
                      <Heart24Regular v-else />
                    </NIcon>
                  </template>
                  {{ isFavorited ? '已收藏' : '收藏' }}
                </NButton>
                <NButton quaternary size="small" @click="router.push(`/search?keyword=${goods.title}`)">
                  <template #icon><NIcon><Share24Filled /></NIcon></template>
                  相似
                </NButton>
                <NButton quaternary size="small" @click="openReportModal">
                  <template #icon><NIcon><Flag24Filled /></NIcon></template>
                  举报
                </NButton>
              </div>
            </NCard>
          </div>
        </div>

        <!-- Comments -->
        <NCard :bordered="true" style="border-radius: 12px" class="mt-6">
          <h3 class="text-lg font-bold text-gray-800 mb-4">留言 ({{ comments.length }})</h3>

          <!-- Comment Input -->
          <div class="mb-4" v-if="userStore.isLoggedIn">
            <div v-if="replyTo" class="text-xs text-[#3B82F6] mb-1">
              回复 @{{ replyTo.name }}
              <span class="text-gray-400 cursor-pointer ml-2" @click="replyTo = null">取消</span>
            </div>
            <div class="flex gap-2">
              <NInput v-model:value="commentText" placeholder="写下你的留言..." class="flex-1" size="small" @keyup.enter="handleSubmitComment" />
              <NButton type="primary" size="small" :loading="submittingComment" @click="handleSubmitComment">
                {{ replyTo ? '回复' : '留言' }}
              </NButton>
            </div>
          </div>
          <div v-else class="text-center text-sm text-gray-400 mb-4">
            <span class="text-[#3B82F6] cursor-pointer" @click="router.push('/login')">登录</span>后即可留言
          </div>

          <div v-if="comments.length > 0" class="space-y-3">
            <template v-for="c in comments" :key="c.id">
              <div class="pb-3 border-b border-gray-100 last:border-0">
                <div class="flex gap-2">
                  <img :src="getAvatarUrl(c.userAvatar, 'thumb_64')" class="w-8 h-8 rounded-full object-cover" />
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-2">
                      <span class="font-semibold text-sm">{{ c.userNickname }}</span>
                      <span class="text-xs text-gray-400">{{ formatDate(c.createTime) }}</span>
                    </div>
                    <p class="text-sm text-gray-600 mt-1">{{ c.content }}</p>
                    <NButton v-if="userStore.isLoggedIn" size="tiny" quaternary class="mt-1" @click="handleReply(c)">回复</NButton>

                    <!-- Replies -->
                    <div v-if="c.replies && c.replies.length > 0" class="mt-2 ml-4 pl-3 border-l-2 border-gray-100 space-y-2">
                      <div v-for="r in c.replies" :key="r.id" class="flex gap-2">
                        <img :src="getAvatarUrl(r.userAvatar, 'thumb_64')" class="w-6 h-6 rounded-full object-cover" />
                        <div class="flex-1">
                          <div class="flex items-center gap-2">
                            <span class="font-semibold text-xs">{{ r.userNickname }}</span>
                            <span class="text-xs text-gray-400">{{ formatDate(r.createTime) }}</span>
                          </div>
                          <p class="text-xs text-gray-600 mt-0.5">{{ r.content }}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>
          <NEmpty v-else description="暂无留言，来第一个留言吧" />
        </NCard>
      </template>

      <NResult v-else-if="error" status="404" title="商品不存在" :description="error">
        <template #footer>
          <NButton @click="router.push('/')">返回首页</NButton>
        </template>
      </NResult>

    </NSpin>

    <!-- Buy/Exchange Modal -->
    <NModal v-model:show="showBuyModal" :title="goods?.tradeType === 'EXCHANGE' ? '发起置换' : '确认购买'" preset="card" style="width: 420px; border-radius: 12px">
      <div class="space-y-4">
        <div class="flex items-center gap-3">
          <img v-if="goods?.images?.[0]" :src="getImageUrl(goods.images[0])" class="w-16 h-16 object-cover rounded-lg" />
          <div>
            <div class="font-semibold text-gray-800">{{ goods?.title }}</div>
            <div v-if="goods?.tradeType !== 'EXCHANGE'" class="text-[#3B82F6] font-bold">{{ formatPrice(goods?.price || 0) }}</div>
            <div v-else class="text-green-600 font-bold text-sm">🔄 置换</div>
          </div>
        </div>

        <!-- Exchange: select own goods to offer -->
        <div v-if="goods?.tradeType === 'EXCHANGE'">
          <p class="text-sm text-gray-500 mb-2">选择你要置换的商品：</p>
          <div v-if="myExchangeGoods.length > 0" class="max-h-40 overflow-y-auto space-y-2 border rounded-lg p-2">
            <div v-for="g in myExchangeGoods" :key="g.id"
              class="flex items-center gap-3 p-2 rounded-lg cursor-pointer border"
              :class="exchangeOfferId === g.id ? 'border-[#3B82F6] bg-blue-50' : 'border-gray-100 hover:bg-gray-50'"
              @click="exchangeOfferId = exchangeOfferId === g.id ? null : g.id">
              <img :src="g.coverImage || ''" class="w-12 h-12 object-cover rounded-lg shrink-0" />
              <div class="flex-1 min-w-0">
                <div class="text-sm font-semibold truncate">{{ g.title }}</div>
                <div v-if="g.tradeType !== 'EXCHANGE'" class="text-xs text-[#3B82F6] font-bold">{{ formatPrice(g.price || 0) }}</div>
                <div v-else class="text-xs text-green-600">仅置换</div>
              </div>
              <div v-if="exchangeOfferId === g.id" class="text-[#3B82F6] font-bold text-sm">✓</div>
            </div>
          </div>
          <p v-else class="text-xs text-gray-400 mb-2">你还没有可置换的商品</p>
          <NButton size="small" text type="primary" class="mt-1" @click="showBuyModal = false; router.push('/publish')">
            + 发布新商品用于置换
          </NButton>
        </div>

        <NDatePicker v-model:value="meetTimeTs" type="datetime" clearable placeholder="约定交易时间" 
          style="width: 100%" input-readonly format="yyyy-MM-dd HH:mm" />
        <NInput v-model:value="meetPlace" placeholder="约定交易地点，如：图书馆门口" />
        <NInput v-model:value="buyMessage" type="textarea" placeholder="给对方留言（选填）" :rows="2" />

        <NButton type="primary" block :loading="submitting"
          :disabled="goods?.tradeType === 'EXCHANGE' && !exchangeOfferId"
          @click="submitOrder">
          {{ goods?.tradeType === 'EXCHANGE' ? '发起置换' : '确认购买' }}
        </NButton>
      </div>
    </NModal>

    <!-- Report Modal -->
    <NModal v-model:show="showReportModal" title="举报商品" preset="card" style="width: 420px; border-radius: 12px">
      <div class="space-y-4">
        <p class="text-sm text-gray-500">请选择举报理由：</p>
        <NRadioGroup v-model:value="reportReason" class="flex flex-col gap-2">
          <NRadio value="FALSE_INFO">虚假信息</NRadio>
          <NRadio value="FRAUD">欺诈行为</NRadio>
          <NRadio value="AD">广告内容</NRadio>
          <NRadio value="VIOLATION">违规内容</NRadio>
        </NRadioGroup>
        <NInput
          v-model:value="reportDescription"
          type="textarea"
          placeholder="补充说明（选填）"
          :rows="3"
        />
        <div class="flex gap-2">
          <NButton @click="showReportModal = false" class="flex-1">取消</NButton>
          <NButton type="error" :loading="submittingReport" @click="submitReport" class="flex-1">提交举报</NButton>
        </div>
      </div>
    </NModal>
  </div>
</template>

<style scoped>
.grid-cols-1 {
  display: grid;
}

.image-container {
  width: 100%;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  border-radius: 8px;
  overflow: hidden;
}

.goods-main-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
</style>
