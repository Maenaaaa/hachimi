<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { searchGoods } from '@/api/goods'
import { getCategories } from '@/api/category'
import { formatPrice, formatDate } from '@/utils'
import type { Category } from '@/types/entity'
import { GOODS_CONDITIONS } from '@/constants'
import {
  NInput,
  NButton,
  NSelect,
  NCard,
  NGrid,
  NGi,
  NTag,
  NSpin,
  NEmpty,
  NAvatar,
  NPagination,
  useMessage,
} from 'naive-ui'

const router = useRouter()
const route = useRoute()
const message = useMessage()

const keyword = ref((route.query.keyword as string) || '')
const categoryId = ref<number | null>(route.query.categoryId ? Number(route.query.categoryId) : null)
const condition = ref<string | null>(null)
const minPrice = ref<number | null>(null)
const maxPrice = ref<number | null>(null)
const sortBy = ref<string>('latest')

const categories = ref<Category[]>([])
const goodsList = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const pageSize = ref(20)

const conditionOptions = [
  { label: '全部', value: '' },
  { label: '全新', value: 'BRAND_NEW' },
  { label: '几乎全新', value: 'LIKE_NEW' },
  { label: '轻微使用', value: 'MINOR_WEAR' },
  { label: '明显使用', value: 'VISIBLE_WEAR' },
  { label: '使用较重', value: 'HEAVILY_USED' },
]

const sortOptions = [
  { label: '最新发布', value: 'latest' },
  { label: '价格从低到高', value: 'price_asc' },
  { label: '价格从高到低', value: 'price_desc' },
  { label: '最多浏览', value: 'views' },
  { label: '最受欢迎', value: 'popular' },
]

const conditionLabels: Record<string, string> = {
  BRAND_NEW: '全新', LIKE_NEW: '几乎全新', MINOR_WEAR: '轻微使用',
  VISIBLE_WEAR: '明显使用', HEAVILY_USED: '使用较重',
}

async function loadCategories() {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch { /* ignore */ }
}

async function doSearch() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      page: page.value,
      size: pageSize.value,
      sort: sortBy.value !== 'latest' ? sortBy.value : undefined,
      keyword: keyword.value.trim() || undefined,
      categoryId: categoryId.value || undefined,
      condition: condition.value || undefined,
      minPrice: minPrice.value ?? undefined,
      maxPrice: maxPrice.value ?? undefined,
    }
    const res = await searchGoods(params)
    goodsList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch {
    message.error('搜索失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  doSearch()
}

function goToGoods(id: number) {
  router.push(`/goods/${id}`)
}

watch(page, () => doSearch())

onMounted(() => {
  loadCategories()
  doSearch()
})
</script>

<template>
  <div class="space-y-4 pb-8">
    <div class="flex gap-2">
      <NInput
        v-model:value="keyword"
        placeholder="搜索闲置物品..."
        clearable
        size="large"
        class="flex-1"
        @keyup.enter="handleSearch"
      />
      <NButton type="primary" size="large" @click="handleSearch">搜索</NButton>
    </div>

    <NCard :bordered="true" style="border-radius: 12px">
      <div class="space-y-4">
        <div class="flex items-center gap-4 flex-wrap">
          <span class="text-sm text-gray-500 shrink-0">分类：</span>
          <NButton v-for="cat in categories" :key="cat.id"
            :type="categoryId === cat.id ? 'primary' : 'default'"
            size="small" quaternary
            @click="categoryId = categoryId === cat.id ? null : cat.id; handleSearch()"
          >{{ cat.name }}</NButton>
          <NButton v-if="categoryId" size="small" tertiary @click="categoryId = null; handleSearch()">清除</NButton>
        </div>

        <div class="flex items-center gap-4 flex-wrap">
          <span class="text-sm text-gray-500 shrink-0">成色：</span>
          <NButton v-for="opt in conditionOptions" :key="opt.value"
            :type="condition === opt.value || (!opt.value && !condition) ? 'primary' : 'default'"
            size="small" quaternary
            @click="condition = opt.value || null; handleSearch()"
          >{{ opt.label }}</NButton>
        </div>

        <div class="flex items-center gap-4 flex-wrap">
          <span class="text-sm text-gray-500">价格：</span>
          <NInput :value="minPrice !== null ? String(minPrice) : ''" placeholder="最低价"
            style="width: 100px" size="small"
            @update:value="(v: string) => { minPrice = v ? Number(v) : null }" />
          <span class="text-gray-400">-</span>
          <NInput :value="maxPrice !== null ? String(maxPrice) : ''" placeholder="最高价"
            style="width: 100px" size="small"
            @update:value="(v: string) => { maxPrice = v ? Number(v) : null }" />
          <NButton size="small" @click="handleSearch">确定</NButton>
        </div>

        <div class="flex items-center gap-4">
          <span class="text-sm text-gray-500">排序：</span>
          <NSelect v-model:value="sortBy" :options="sortOptions" size="small" style="width: 140px"
            @update:value="handleSearch" />
        </div>
      </div>
    </NCard>

    <div class="flex items-center justify-between">
      <span class="text-sm text-gray-400">共 {{ total }} 件商品</span>
    </div>

    <NSpin :show="loading">
      <template v-if="goodsList.length > 0">
        <NGrid :cols="2" :x-gap="12" :y-gap="12" responsive="screen" :screen-s="2" :screen-m="3" :screen-l="4">
          <NGi v-for="goods in goodsList" :key="goods.id">
            <NCard hoverable class="goods-card cursor-pointer"
              :style="{ borderRadius: '12px', boxShadow: '0 4px 20px rgba(0,0,0,.06)' }"
              @click="goToGoods(goods.id)">
              <div class="relative">
                <img :src="goods.coverImage || ''" :alt="goods.title"
                  class="w-full h-36 object-cover rounded-lg" loading="lazy"
                  @error="(e: Event) => { (e.target as HTMLImageElement).src = '' }" />
                <NTag v-if="goods.condition" size="tiny" class="absolute top-2 left-2"
                  :color="{ textColor: '#fff', borderColor: '#3B82F6', color: '#3B82F6' }">
                  {{ conditionLabels[goods.condition] || goods.condition }}
                </NTag>
                <NTag v-if="goods.tradeType === 'EXCHANGE'" size="tiny" class="absolute top-2 right-2"
                  :color="{ textColor: '#fff', borderColor: '#10B981', color: '#10B981' }">
                  置换
                </NTag>
              </div>
              <h4 class="text-sm font-semibold text-gray-800 mt-2 truncate">{{ goods.title }}</h4>
              <p v-if="goods.tradeType !== 'EXCHANGE'" class="text-[#3B82F6] font-bold text-base mt-1">{{ formatPrice(goods.price) }}</p>
              <p v-else class="text-[#10B981] font-bold text-base mt-1">仅置换</p>
              <div class="flex items-center justify-between mt-1">
                <div class="flex items-center gap-1">
                  <NAvatar :src="goods.sellerAvatar || undefined" :size="20" round style="background-color: #3B82F6">
                    {{ goods.sellerNickname?.charAt(0) || 'U' }}
                  </NAvatar>
                  <span class="text-xs text-gray-400">{{ goods.sellerNickname || '匿名' }}</span>
                </div>
                <span class="text-xs text-gray-400">{{ formatDate(goods.createTime) }}</span>
              </div>
            </NCard>
          </NGi>
        </NGrid>

        <div class="flex justify-center mt-6" v-if="total > pageSize">
          <NPagination v-model:page="page" :page-count="Math.ceil(total / pageSize)"
            :page-size="pageSize" :item-count="total" />
        </div>
      </template>
      <NEmpty v-else-if="!loading" description="没有找到符合条件的商品" />
    </NSpin>
  </div>
</template>

<style scoped>
.goods-card :deep(.n-card__content) {
  padding: 12px;
}
</style>
