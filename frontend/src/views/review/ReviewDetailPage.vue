<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getReviewById, getReviewByOrderId } from '@/api/review'
import { getImageUrl, getAvatarUrl, formatDate } from '@/utils'
import { useUserStore } from '@/stores/user'
import type { Review } from '@/types/entity'
import { NCard, NSpin, NEmpty, NAvatar, NButton, NIcon, NDivider } from 'naive-ui'
import { ArrowLeft24Filled } from '@vicons/fluent'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const review = ref<Review | null>(null)
const allReviews = ref<Review[]>([])
const loading = ref(true)

const myReview = computed(() => allReviews.value.find(r => r.reviewerId === userStore.user?.id) || null)
const otherReview = computed(() => allReviews.value.find(r => r.reviewerId !== userStore.user?.id) || review.value)

async function load() {
  const id = Number(route.params.id)
  if (!id) { loading.value = false; return }
  try {
    const res = await getReviewById(id)
    review.value = res.data
    if (res.data.orderId) {
      const allRes = await getReviewByOrderId(res.data.orderId)
      allReviews.value = allRes.data || []
    } else {
      allReviews.value = [res.data]
    }
  } catch {
    review.value = null
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="max-w-600px mx-auto pb-8">
    <NButton quaternary size="small" class="mb-4" @click="router.back()">
      <template #icon><NIcon><ArrowLeft24Filled /></NIcon></template>
      返回
    </NButton>

    <NSpin :show="loading">
      <template v-if="otherReview">
        <!-- 对方的评价 -->
        <NCard :bordered="true" style="border-radius: 12px">
          <template #header>
            <span class="text-sm text-gray-500">{{ otherReview.reviewerNickname }} 的评价</span>
          </template>
          <div class="space-y-3">
            <div class="flex items-center gap-3">
              <img
                :src="getAvatarUrl(otherReview.reviewerAvatar, 'thumb_64')"
                class="w-9 h-9 rounded-full object-cover"
              />
              <div>
                <div class="font-semibold text-sm">{{ otherReview.reviewerNickname }}</div>
                <div class="text-xs text-gray-400">{{ formatDate(otherReview.createTime) }}</div>
              </div>
            </div>
            <div class="text-yellow-400">
              {{ '★'.repeat(otherReview.rating || 0) }}{{ '☆'.repeat(5 - (otherReview.rating || 0)) }}
            </div>
            <p class="text-sm text-gray-600 leading-relaxed whitespace-pre-wrap">
              {{ otherReview.content || '未填写评价内容' }}
            </p>
          </div>
        </NCard>

        <!-- 关联商品 -->
        <NCard v-if="otherReview.goodsId" :bordered="true" style="border-radius: 12px" class="mt-3">
          <div class="flex items-center gap-3 cursor-pointer hover:bg-gray-50 rounded-lg p-2 -m-2"
            @click="router.push(`/goods/${otherReview.goodsId}`)">
            <img
              v-if="otherReview.goodsCoverImage"
              :src="getImageUrl(otherReview.goodsCoverImage)"
              class="w-14 h-14 object-cover rounded-lg"
            />
            <div class="flex-1 min-w-0">
              <div class="text-xs text-gray-400 mb-1">关联商品</div>
              <div class="text-sm font-semibold text-gray-800 truncate">{{ otherReview.goodsTitle }}</div>
            </div>
          </div>
        </NCard>

        <!-- 我的评价 -->
        <NDivider />

        <template v-if="myReview">
          <NCard :bordered="true" style="border-radius: 12px">
            <template #header>
              <span class="text-sm text-gray-500">我的评价</span>
            </template>
            <div class="space-y-3">
              <div class="text-yellow-400">
                {{ '★'.repeat(myReview.rating || 0) }}{{ '☆'.repeat(5 - (myReview.rating || 0)) }}
              </div>
              <p class="text-sm text-gray-600 leading-relaxed whitespace-pre-wrap">
                {{ myReview.content || '未填写评价内容' }}
              </p>
              <div class="text-xs text-gray-400">{{ formatDate(myReview.createTime) }}</div>
            </div>
          </NCard>
        </template>
        <template v-else>
          <NCard :bordered="true" style="border-radius: 12px">
            <div class="text-center py-4">
              <p class="text-sm text-gray-400 mb-3">你还没有评价对方</p>
              <NButton type="primary" size="small" @click="router.push(`/my-orders`)">
                去评价
              </NButton>
            </div>
          </NCard>
        </template>
      </template>
      <NEmpty v-else-if="!loading" description="评价不存在" />
    </NSpin>
  </div>
</template>

<style scoped>
.max-w-600px { max-width: 600px; }
</style>
