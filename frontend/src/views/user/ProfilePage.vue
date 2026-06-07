<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserProfile } from '@/api/user'
import { getMyGoods, getUserGoods } from '@/api/goods'
import { getMyFavorites } from '@/api/favorite'
import { getFollowers, getFollowing } from '@/api/follow'
import { getUserReviews } from '@/api/review'
import { formatPrice, formatDate } from '@/utils'
import { GOODS_STATUS } from '@/constants'
import {
  NAvatar, NButton, NCard, NTag, NTab, NTabPane, NTabs, NEmpty, NSpin,
} from 'naive-ui'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const profileUser = ref<any>(null)
const tab = ref((route.query.tab as string) || 'goods')
const myGoods = ref<any[]>([])
const favorites = ref<any[]>([])
const followers = ref<any[]>([])
const following = ref<any[]>([])
const reviews = ref<any[]>([])
const loading = ref(true)

const isOwnProfile = computed(() => {
  const userId = route.query.userId ? Number(route.query.userId) : null
  return !userId || userId === userStore.user?.id
})

const displayUser = computed(() => isOwnProfile.value ? userStore.user : profileUser.value)

async function loadProfile() {
  loading.value = true
  const userId = route.query.userId ? Number(route.query.userId) : undefined
  const targetUserId = userId || userStore.user?.id

  try {
    const promises: Promise<unknown>[] = []

    if (userId) {
      promises.push(
        getUserProfile().then((res: any) => { profileUser.value = res.data }).catch(() => {}),
      )
    }

    if (targetUserId) {
      promises.push(
        getUserReviews(targetUserId).then((res: any) => { reviews.value = res.data || [] }).catch(() => {}),
        getUserGoods(targetUserId).then((res: any) => { myGoods.value = res.data || [] }).catch(() => {}),
      )
    }

    if (isOwnProfile.value) {
      promises.push(
        getMyFavorites().then((res: any) => { favorites.value = res.data || [] }).catch(() => {}),
        getFollowers(targetUserId!).then((res: any) => { followers.value = res.data || [] }).catch(() => {}),
        getFollowing(targetUserId!).then((res: any) => { following.value = res.data || [] }).catch(() => {}),
      )
    }

    await Promise.allSettled(promises)
  } finally { loading.value = false }
}

function goToGoods(id: number) { router.push(`/goods/${id}`) }

onMounted(loadProfile)
</script>

<template>
  <div class="pb-8">
    <NSpin :show="loading">
      <NCard :bordered="true" style="border-radius: 12px" class="mb-6">
        <div class="flex flex-col items-center gap-4 sm:flex-row sm:items-start sm:gap-6">
          <NAvatar
            :src="displayUser?.avatar || undefined"
            :size="80" round
            style="background-color: #3B82F6; font-size: 32px"
          >{{ displayUser?.nickname?.charAt(0) || 'U' }}</NAvatar>
          <div class="flex-1 text-center sm:text-left">
            <div class="flex items-center gap-2 justify-center sm:justify-start">
              <h2 class="text-xl font-bold dark:text-gray-100 text-gray-800">{{ displayUser?.nickname || '用户' }}</h2>
              <NTag v-if="displayUser?.isVerified" size="small" type="success">已认证</NTag>
            </div>
            <p class="text-sm text-gray-400">@{{ displayUser?.username }} · {{ displayUser?.school || '未设置学校' }}</p>
            <p class="text-sm text-gray-500 mt-1">信用分: {{ displayUser?.creditScore || 5.0 }}</p>
            <div class="flex gap-6 mt-3 justify-center sm:justify-start text-sm">
              <span><b>{{ displayUser?.goodsCount || 0 }}</b> 商品</span>
              <span><b>{{ displayUser?.followerCount || 0 }}</b> 粉丝</span>
              <span><b>{{ displayUser?.followingCount || 0 }}</b> 关注</span>
            </div>
          </div>
          <div v-if="isOwnProfile" class="flex gap-2 shrink-0">
            <NButton size="small" @click="router.push('/settings')">编辑资料</NButton>
            <NButton size="small" @click="router.push('/my-goods')">我的商品</NButton>
          </div>
        </div>
      </NCard>

      <NCard :bordered="true" style="border-radius: 12px">
        <NTabs v-model:value="tab" type="line">
          <NTabPane name="goods" tab="发布的商品">
            <div v-if="myGoods.length > 0" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3 mt-4">
              <div v-for="item in myGoods" :key="item.id" class="cursor-pointer" @click="goToGoods(item.id)">
                <img :src="item.coverImage || ''" class="w-full h-28 object-cover rounded-lg" />
                <NTag v-if="item.status" size="tiny" class="mt-1">{{ GOODS_STATUS[item.status] || item.status }}</NTag>
                <div class="text-sm font-semibold mt-1 truncate">{{ item.title }}</div>
                <div class="text-[#3B82F6] font-bold text-sm">{{ formatPrice(item.price) }}</div>
              </div>
            </div>
            <NEmpty v-else description="暂无商品" class="mt-4" />
          </NTabPane>

          <NTabPane v-if="isOwnProfile" name="favorites" tab="我的收藏">
            <div v-if="favorites.length > 0" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3 mt-4">
              <div v-for="fav in favorites" :key="fav.id" class="cursor-pointer" @click="goToGoods(fav.id)">
                <img :src="fav.coverImage || ''" class="w-full h-28 object-cover rounded-lg" />
                <div class="text-sm font-semibold mt-1 truncate">{{ fav.title }}</div>
                <div class="text-[#3B82F6] font-bold text-sm">{{ formatPrice(fav.price) }}</div>
              </div>
            </div>
            <NEmpty v-else description="暂无收藏" class="mt-4" />
          </NTabPane>

          <NTabPane v-if="isOwnProfile" name="following" tab="我的关注">
            <div v-if="following.length > 0" class="space-y-2 mt-4">
              <div v-for="f in following" :key="f.id" class="flex items-center gap-3 p-2 cursor-pointer dark:hover:bg-gray-700 hover:bg-gray-50 rounded-lg"
                @click="router.push('/profile?userId=' + f.id)">
                <NAvatar :src="f.avatar || undefined" :size="36" round style="background-color: #3B82F6">
                  {{ f.nickname?.charAt(0) || 'U' }}
                </NAvatar>
                <div>
                  <div class="font-semibold text-sm">{{ f.nickname }}</div>
                  <div class="text-xs text-gray-400">{{ f.school || '' }}</div>
                </div>
              </div>
            </div>
            <NEmpty v-else description="暂无关注" class="mt-4" />
          </NTabPane>

          <NTabPane name="reviews" tab="评价">
            <div v-if="reviews.length > 0" class="space-y-3 mt-4">
              <div v-for="review in reviews" :key="review.id" class="pb-3 dark:border-gray-700 border-b border-gray-100 last:border-0">
                <div class="flex items-center gap-2">
                  <NAvatar :src="review.reviewerAvatar || undefined" :size="24" round style="background-color: #3B82F6">
                    {{ review.reviewerNickname?.charAt(0) || 'U' }}
                  </NAvatar>
                  <span class="text-sm font-semibold">{{ review.reviewerNickname }}</span>
                  <span class="text-yellow-400 text-sm">{{ '★'.repeat(review.rating) }}{{ '☆'.repeat(5 - review.rating) }}</span>
                  <span class="text-xs text-gray-400">{{ formatDate(review.createTime) }}</span>
                </div>
                <p class="text-sm dark:text-gray-400 text-gray-600 mt-1">{{ review.content || '未填写评价内容' }}</p>
              </div>
            </div>
            <NEmpty v-else description="暂无评价" class="mt-4" />
          </NTabPane>

        </NTabs>
      </NCard>
    </NSpin>
  </div>
</template>
