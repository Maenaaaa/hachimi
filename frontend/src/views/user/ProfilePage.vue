<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserProfile, getUserPublicProfile } from '@/api/user'
import { getMyGoods, getUserGoods } from '@/api/goods'
import { getMyFavorites } from '@/api/favorite'
import { getFollowers, getFollowing, addFollow, removeFollow } from '@/api/follow'
import { getUserReviews } from '@/api/review'
import { formatPrice, formatDate, getImageUrl, getAvatarUrl } from '@/utils'
import { GOODS_STATUS } from '@/constants'
import {
  NAvatar, NButton, NCard, NTag, NTab, NTabPane, NTabs, NEmpty, NSpin, NModal, NIcon, useMessage,
} from 'naive-ui'
import { Person24Filled } from '@vicons/fluent'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const message = useMessage()

const profileUser = ref<any>(null)
const tab = ref((route.query.tab as string) || 'goods')
const myGoods = ref<any[]>([])
const favorites = ref<any[]>([])
const followers = ref<any[]>([])
const following = ref<any[]>([])
const reviews = ref<any[]>([])
const loading = ref(true)
const showFollowModal = ref(false)
const followModalTitle = ref('')
const followModalList = ref<any[]>([])
const loadingFollowList = ref(false)
const isFollowing = ref(false)

const isOwnProfile = computed(() => {
  const userId = route.query.userId ? Number(route.query.userId) : null
  return !userId || userId === userStore.user?.id
})

const displayUser = computed(() => profileUser.value || userStore.user)

const userAvatarUrl = computed(() => {
  return getAvatarUrl(displayUser.value?.avatar, 'original')
})

async function loadProfile() {
  loading.value = true
  const userId = route.query.userId ? Number(route.query.userId) : undefined
  const targetUserId = userId || userStore.user?.id

  try {
    const promises: Promise<unknown>[] = []

    if (userId) {
      promises.push(
        getUserPublicProfile(userId).then((res: any) => { profileUser.value = res.data }).catch(() => {}),
      )
    } else if (userStore.user?.id) {
      promises.push(
        getUserPublicProfile(userStore.user.id).then((res: any) => { profileUser.value = res.data }).catch(() => {}),
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
      )
    } else if (userId && userStore.isLoggedIn) {
      promises.push(
        getFollowing(userStore.user!.id, 1, 100).then((res: any) => {
          isFollowing.value = (res.data || []).some((f: any) => f.id === userId)
        }).catch(() => {}),
      )
    }

    await Promise.allSettled(promises)
  } finally { loading.value = false }
}

function goToGoods(id: number) { router.push(`/goods/${id}`) }

function getUserId(): number | null {
  return route.query.userId ? Number(route.query.userId) : userStore.user?.id || null
}

async function showFollowers() {
  const userId = getUserId()
  if (!userId) return
  followModalTitle.value = '粉丝列表'
  showFollowModal.value = true
  loadingFollowList.value = true
  try {
    const res = await getFollowers(userId)
    followModalList.value = res.data || []
  } catch { followModalList.value = [] }
  finally { loadingFollowList.value = false }
}

async function showFollowing() {
  const userId = getUserId()
  if (!userId) return
  followModalTitle.value = '关注列表'
  showFollowModal.value = true
  loadingFollowList.value = true
  try {
    const res = await getFollowing(userId)
    followModalList.value = res.data || []
  } catch { followModalList.value = [] }
  finally { loadingFollowList.value = false }
}

function toggleFollow() {
  const userId = getUserId()
  if (!userId || !userStore.isLoggedIn) return
  if (isFollowing.value) {
    removeFollow(userId)
      .then(() => {
        isFollowing.value = false
        if (displayUser.value) displayUser.value.followerCount = Math.max(0, (displayUser.value.followerCount || 1) - 1)
        message.success('已取消关注')
      })
      .catch(() => message.error('操作失败'))
  } else {
    addFollow(userId)
      .then(() => {
        isFollowing.value = true
        if (displayUser.value) displayUser.value.followerCount = (displayUser.value.followerCount || 0) + 1
        message.success('已关注')
      })
      .catch(() => message.error('操作失败'))
  }
}

onMounted(loadProfile)

watch(() => route.query.userId, () => {
  loadProfile()
})
</script>

<template>
  <div class="pb-8">
    <NSpin :show="loading">
      <NCard :bordered="true" style="border-radius: 12px" class="mb-6">
        <div class="flex flex-col items-center gap-4 sm:flex-row sm:items-start sm:gap-6">
          <img
            :src="getAvatarUrl(displayUser?.avatar, 'original')"
            class="w-20 h-20 rounded-full object-cover"
            style="background-color: #3B82F6"
          />
          <div class="flex-1 text-center sm:text-left">
            <div class="flex items-center gap-2 justify-center sm:justify-start">
              <h2 class="text-xl font-bold dark:text-gray-100 text-gray-800">{{ displayUser?.nickname || '用户' }}</h2>
              <NTag v-if="displayUser?.isVerified" size="small" type="success">已认证</NTag>
            </div>
            <p class="text-sm text-gray-400">@{{ displayUser?.username }} · {{ displayUser?.school || '未设置学校' }}</p>
            <p class="text-sm text-gray-500 mt-1">信用分: {{ displayUser?.creditScore || 5.0 }}</p>
            <div class="flex gap-6 mt-3 justify-center sm:justify-start text-sm">
              <span><b>{{ displayUser?.goodsCount || 0 }}</b> 商品</span>
              <span class="cursor-pointer hover:text-[#3B82F6]" @click="showFollowers"><b>{{ displayUser?.followerCount || 0 }}</b> 粉丝</span>
              <span class="cursor-pointer hover:text-[#3B82F6]" @click="showFollowing"><b>{{ displayUser?.followingCount || 0 }}</b> 关注</span>
            </div>
          </div>
          <div v-if="isOwnProfile" class="flex gap-2 shrink-0">
            <NButton size="small" @click="router.push('/settings')">编辑资料</NButton>
            <NButton size="small" @click="router.push('/my-goods')">我的商品</NButton>
          </div>
          <div v-else-if="userStore.isLoggedIn" class="shrink-0">
            <NButton
              :type="isFollowing ? 'default' : 'primary'"
              size="small"
              @click="toggleFollow"
            >
              {{ isFollowing ? '已关注' : '+ 关注' }}
            </NButton>
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
                <div v-if="item.tradeType !== 'EXCHANGE'" class="text-[#3B82F6] font-bold text-sm">{{ formatPrice(item.price) }}</div>
                <div v-else class="text-green-600 font-bold text-xs">🔄 仅置换</div>
              </div>
            </div>
            <NEmpty v-else description="暂无商品" class="mt-4" />
          </NTabPane>

          <NTabPane v-if="isOwnProfile" name="favorites" tab="我的收藏">
            <div v-if="favorites.length > 0" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3 mt-4">
              <div v-for="fav in favorites" :key="fav.id" class="cursor-pointer" @click="goToGoods(fav.id)">
                <img :src="fav.coverImage || ''" class="w-full h-28 object-cover rounded-lg" />
                <div class="text-sm font-semibold mt-1 truncate">{{ fav.title }}</div>
                <div v-if="fav.tradeType !== 'EXCHANGE'" class="text-[#3B82F6] font-bold text-sm">{{ formatPrice(fav.price) }}</div>
                <div v-else class="text-green-600 font-bold text-xs">🔄 仅置换</div>
              </div>
            </div>
            <NEmpty v-else description="暂无收藏" class="mt-4" />
          </NTabPane>

          <NTabPane v-if="isOwnProfile" name="following" tab="我的关注">
            <div v-if="following.length > 0" class="space-y-2 mt-4">
              <div v-for="f in following" :key="f.id" class="flex items-center gap-3 p-2 cursor-pointer dark:hover:bg-gray-700 hover:bg-gray-50 rounded-lg"
                @click="router.push('/profile?userId=' + f.id)">
                <img :src="getAvatarUrl(f.avatar, 'thumb_64')" class="w-9 h-9 rounded-full object-cover" />
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
                  <img :src="getAvatarUrl(review.reviewerAvatar, 'thumb_64')" class="w-6 h-6 rounded-full object-cover" />
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

    <!-- Followers/Following Modal -->
    <NModal v-model:show="showFollowModal" :title="followModalTitle" preset="card" style="width: 400px; border-radius: 12px; max-height: 70vh;">
      <NSpin :show="loadingFollowList">
        <div v-if="followModalList.length > 0" class="space-y-2 max-h-80 overflow-y-auto">
          <div v-for="u in followModalList" :key="u.id"
            class="flex items-center gap-3 p-2 rounded-lg cursor-pointer hover:bg-gray-50"
            @click="showFollowModal = false; router.push('/profile?userId=' + u.id)">
            <img :src="getAvatarUrl(u.avatar, 'thumb_64')" class="w-9 h-9 rounded-full object-cover" />
            <div class="flex-1 min-w-0">
              <div class="text-sm font-semibold truncate">{{ u.nickname }}</div>
              <div class="text-xs text-gray-400">{{ u.school || '' }}</div>
            </div>
          </div>
        </div>
        <NEmpty v-else description="暂无数据" />
      </NSpin>
    </NModal>
  </div>
</template>
