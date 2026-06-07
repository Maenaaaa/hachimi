<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getGoodsRecommend, getGoodsLatest } from '@/api/goods'
import { getCategories } from '@/api/category'
import { getAnnouncements } from '@/api/announcement'
import { formatPrice, formatDate, getImageUrl } from '@/utils'
import type { Goods, Category, Announcement } from '@/types/entity'
import {
  NCarousel,
  NCard,
  NGrid,
  NGi,
  NImage,
  NTag,
  NButton,
  NIcon,
  NSpace,
  NSpin,
  NEmpty,
  NAvatar,
  NSkeleton,
  NScrollbar,
} from 'naive-ui'

const router = useRouter()

const banners = [
  { id: 1, image: 'https://picsum.photos/seed/banner1/1200/400', title: '闲置变宝藏', subtitle: '让你的闲置物品找到新主人' },
  { id: 2, image: 'https://picsum.photos/seed/banner2/1200/400', title: '绿色校园', subtitle: '低碳生活，从闲置交换开始' },
  { id: 3, image: 'https://picsum.photos/seed/banner3/1200/400', title: '校园集市', subtitle: '发现身边的好物' },
]

const categories = ref<Category[]>([])
const recommendGoods = ref<Goods[]>([])
const latestGoods = ref<Goods[]>([])
const announcements = ref<Announcement[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [catRes, recRes, latestRes, annRes] = await Promise.allSettled([
      getCategories(),
      getGoodsRecommend(),
      getGoodsLatest(),
      getAnnouncements(),
    ])

    if (catRes.status === 'fulfilled') categories.value = catRes.value.data
    if (recRes.status === 'fulfilled') recommendGoods.value = recRes.value.data
    if (latestRes.status === 'fulfilled') latestGoods.value = latestRes.value.data
    if (annRes.status === 'fulfilled') announcements.value = annRes.value.data
  } finally {
    loading.value = false
  }
})

function goToCategory(catId: number) {
  router.push({ path: '/search', query: { categoryId: String(catId) } })
}

function goToGoods(id: number) {
  router.push(`/goods/${id}`)
}
</script>

<template>
  <div class="space-y-6 pb-8">
    <!-- Announcement Banner -->
    <div v-if="announcements.length > 0" class="bg-[#FEF3C7] text-[#92400E] px-4 py-2 rounded-lg text-sm flex items-center gap-2">
      <span>📢</span>
      <span v-for="ann in announcements.slice(0, 2)" :key="ann.id" class="mr-4">{{ ann.title }}</span>
    </div>

    <!-- Banner Carousel -->
    <NCarousel autoplay :interval="4000" effect="slide" show-arrow class="rounded-xl overflow-hidden" style="height: 280px">
      <div v-for="banner in banners" :key="banner.id" class="relative h-full">
        <img :src="banner.image" :alt="banner.title" class="w-full h-full object-cover" />
        <div class="absolute inset-0 bg-gradient-to-r from-black/50 to-transparent flex items-center p-10">
          <div>
            <h2 class="text-white text-3xl font-bold">{{ banner.title }}</h2>
            <p class="text-white/80 text-lg mt-2">{{ banner.subtitle }}</p>
          </div>
        </div>
      </div>
    </NCarousel>

    <!-- Category Navigation -->
    <NCard :bordered="true" style="border-radius: 12px">
      <h3 class="text-lg font-bold text-gray-800 mb-4">分类浏览</h3>
      <NSpin :show="loading && categories.length === 0">
        <div class="grid grid-cols-4 sm:grid-cols-6 md:grid-cols-8 gap-4">
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="flex flex-col items-center cursor-pointer p-3 rounded-xl hover:bg-gray-50 transition-colors"
            @click="goToCategory(cat.id)"
          >
            <div class="w-12 h-12 bg-[#EFF6FF] rounded-2xl flex items-center justify-center text-2xl mb-2">
              {{ cat.icon || '📦' }}
            </div>
            <span class="text-xs text-gray-600">{{ cat.name }}</span>
          </div>
        </div>
      </NSpin>
    </NCard>

    <!-- Recommended Goods -->
    <div>
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-bold text-gray-800">推荐好物</h3>
        <NButton text type="primary" @click="router.push('/search')">查看更多</NButton>
      </div>

      <NSpin :show="loading && recommendGoods.length === 0">
        <div v-if="recommendGoods.length > 0">
          <NGrid :cols="2" :x-gap="12" :y-gap="12" responsive="screen" :screen-s="2" :screen-m="3" :screen-l="4">
            <NGi v-for="goods in recommendGoods" :key="goods.id">
              <NCard
                hoverable
                class="goods-card cursor-pointer"
                :style="{ borderRadius: '12px', boxShadow: '0 4px 20px rgba(0,0,0,.06)' }"
                @click="goToGoods(goods.id)"
              >
                <div class="relative">
                  <img
                    :src="getImageUrl(goods.coverImage)"
                    :alt="goods.title"
                    class="w-full h-36 object-cover rounded-lg"
                    loading="lazy"
                    @error="(e: Event) => { (e.target as HTMLImageElement).src = 'https://picsum.photos/seed/goods/300/200' }"
                  />
                  <NTag
                    v-if="goods.condition"
                    size="tiny"
                    class="absolute top-2 left-2"
                    :color="{ textColor: '#fff', borderColor: '#3B82F6', color: '#3B82F6' }"
                  >
                    {{ goods.condition === 'new' ? '全新' : goods.condition === 'like_new' ? '几乎全新' : goods.condition === 'good' ? '良好' : '一般' }}
                  </NTag>
                </div>
                <h4 class="text-sm font-semibold text-gray-800 mt-2 truncate">{{ goods.title }}</h4>
                <p class="text-[#3B82F6] font-bold text-base mt-1">{{ formatPrice(goods.price) }}</p>
                <div class="flex items-center justify-between mt-1">
                  <div class="flex items-center gap-1">
                    <NAvatar
                      :src="getImageUrl(goods.sellerAvatar || goods.userAvatar)"
                      :size="20"
                      round
                      style="background-color: #3B82F6"
                    >
                      {{ (goods.sellerNickname || goods.userNickname)?.charAt(0) || 'U' }}
                    </NAvatar>
                    <span class="text-xs text-gray-400">{{ goods.sellerNickname || goods.userNickname || '匿名' }}</span>
                  </div>
                  <span class="text-xs text-gray-400">{{ formatDate(goods.createTime || goods.createdAt) }}</span>
                </div>
              </NCard>
            </NGi>
          </NGrid>
        </div>
        <NEmpty v-else-if="!loading" description="暂无推荐商品" />
      </NSpin>
    </div>

    <!-- Latest Goods -->
    <div>
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-bold text-gray-800">最新发布</h3>
        <NButton text type="primary" @click="router.push('/search?sortBy=latest')">查看更多</NButton>
      </div>

      <NSpin :show="loading && latestGoods.length === 0">
        <template v-if="latestGoods.length > 0">
          <NGrid :cols="2" :x-gap="12" :y-gap="12" responsive="screen" :screen-s="2" :screen-m="3" :screen-l="4">
            <NGi v-for="goods in latestGoods" :key="goods.id">
              <NCard
                hoverable
                class="goods-card cursor-pointer"
                :style="{ borderRadius: '12px', boxShadow: '0 4px 20px rgba(0,0,0,.06)' }"
                @click="goToGoods(goods.id)"
              >
                <div class="relative">
                  <img
                    :src="getImageUrl(goods.coverImage)"
                    :alt="goods.title"
                    class="w-full h-36 object-cover rounded-lg"
                    loading="lazy"
                    @error="(e: Event) => { (e.target as HTMLImageElement).src = 'https://picsum.photos/seed/goods2/300/200' }"
                  />
                  <NTag
                    v-if="goods.condition"
                    size="tiny"
                    class="absolute top-2 left-2"
                    :color="{ textColor: '#fff', borderColor: '#10B981', color: '#10B981' }"
                  >
                    {{ goods.condition === 'new' ? '全新' : goods.condition === 'like_new' ? '几乎全新' : goods.condition === 'good' ? '良好' : '一般' }}
                  </NTag>
                </div>
                <h4 class="text-sm font-semibold text-gray-800 mt-2 truncate">{{ goods.title }}</h4>
                <p class="text-[#3B82F6] font-bold text-base mt-1">{{ formatPrice(goods.price) }}</p>
                <div class="flex items-center justify-between mt-1">
                  <div class="flex items-center gap-1">
                    <NAvatar
                      :src="getImageUrl(goods.sellerAvatar || goods.userAvatar)"
                      :size="20"
                      round
                      style="background-color: #3B82F6"
                    >
                      {{ (goods.sellerNickname || goods.userNickname)?.charAt(0) || 'U' }}
                    </NAvatar>
                    <span class="text-xs text-gray-400">{{ goods.sellerNickname || goods.userNickname || '匿名' }}</span>
                  </div>
                  <span class="text-xs text-gray-400">{{ formatDate(goods.createTime || goods.createdAt) }}</span>
                </div>
              </NCard>
            </NGi>
          </NGrid>
        </template>
        <NEmpty v-else-if="!loading" description="暂无最新商品" />
      </NSpin>
    </div>
  </div>
</template>

<style scoped>
.goods-card :deep(.n-card__content) {
  padding: 12px;
}
.grid-cols-4 {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
}
</style>
