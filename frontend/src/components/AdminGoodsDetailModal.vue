<script setup lang="ts">
import { ref, watch } from 'vue'
import { getGoodsDetail } from '@/api/goods'
import { getImageUrl, getAvatarUrl, formatPrice, formatDate } from '@/utils'
import { GOODS_CONDITIONS, GOODS_STATUS } from '@/constants'
import type { Goods } from '@/types/entity'
import { NModal, NSpin, NTag, NImage, NGi, NGrid, NButton, NIcon } from 'naive-ui'

const props = defineProps<{ goodsId: number | null }>()
const emit = defineEmits<{ close: [] }>()

const goods = ref<Goods | null>(null)
const loading = ref(false)

watch(() => props.goodsId, async (id) => {
  if (!id) { goods.value = null; return }
  loading.value = true
  try {
    const res = await getGoodsDetail(id)
    goods.value = res.data
  } catch { goods.value = null } finally { loading.value = false }
})
</script>

<template>
  <NModal :show="!!goodsId" preset="card" style="width: 680px; max-height: 80vh; border-radius: 12px"
    @update:show="(v) => { if (!v) emit('close') }">
    <template #header>
      <div class="font-bold text-gray-800">商品详情</div>
    </template>
    <NSpin :show="loading">
      <div v-if="goods" class="space-y-4 overflow-y-auto" style="max-height: 60vh">
        <NImage :src="getImageUrl(goods.coverImage)" style="width: 100%; border-radius: 8px; max-height: 280px; object-fit: cover" />
        <div>
          <div class="flex items-center gap-2 mb-1">
            <NTag :type="goods.status === 'ACTIVE' ? 'success' : goods.status === 'FINAL_REJECTED' ? 'error' : 'default'" size="small">
              {{ GOODS_STATUS[goods.status] || goods.status }}
            </NTag>
            <NTag v-if="(goods as any).aiReviewStatus" size="small" type="info">
              AI: {{ (goods as any).aiReviewStatus }}
            </NTag>
          </div>
          <h3 class="text-lg font-bold text-gray-800">{{ goods.title }}</h3>
        </div>
        <div class="flex items-center gap-4 text-sm text-gray-500">
          <span class="text-[#3B82F6] font-bold text-xl">{{ formatPrice(goods.price) }}</span>
          <span v-if="goods.originalPrice">原价 {{ formatPrice(goods.originalPrice) }}</span>
          <NTag size="tiny">{{ GOODS_CONDITIONS[goods.condition] || goods.condition }}</NTag>
        </div>
        <div class="text-sm text-gray-600 whitespace-pre-wrap">{{ goods.description || '暂无描述' }}</div>
        <div class="flex items-center gap-3 text-xs text-gray-400 pt-2 border-t">
          <img :src="getAvatarUrl((goods as any).userAvatar, 'thumb_64')" class="w-6 h-6 rounded-full" />
          <span>{{ (goods as any).userNickname || '未知用户' }}</span>
          <span>·</span>
          <span>{{ goods.viewCount || 0 }} 浏览</span>
          <span>·</span>
          <span>{{ goods.favoriteCount || 0 }} 收藏</span>
          <span>·</span>
          <span>{{ formatDate(goods.createTime) }}</span>
        </div>
      </div>
      <div v-else-if="!loading" class="text-center text-gray-400 py-10">商品不存在</div>
    </NSpin>
  </NModal>
</template>
