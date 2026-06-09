<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyGoods, deleteGoods, toggleGoodsStatus } from '@/api/goods'
import { getImageUrl, formatPrice, formatDate } from '@/utils'
import { GOODS_STATUS } from '@/constants'
import type { Goods } from '@/types/entity'
import {
  NCard,
  NButton,
  NIcon,
  NTag,
  NEmpty,
  NSpin,
  NDataTable,
  NSpace,
  NModal,
  NInput,
  useMessage,
  useDialog,
  type DataTableColumn,
} from 'naive-ui'
import { Edit24Filled, Delete24Filled } from '@vicons/fluent'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const goodsList = ref<Goods[]>([])
const loading = ref(true)

async function loadGoods() {
  loading.value = true
  try {
    const res = await getMyGoods()
    goodsList.value = res.data
  } catch {
    message.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleEdit(goods: Goods) {
  router.push(`/goods/${goods.id}/edit`)
}

function goToGoods(id: number) {
  router.push(`/goods/${id}`)
}

async function handleDelete(id: number) {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除该商品吗？此操作不可恢复。',
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteGoods(id)
        message.success('删除成功')
        loadGoods()
      } catch {
        message.error('删除失败')
      }
    },
  })
}

function handleRelist(id: number) {
  dialog.info({
    title: '重新上架',
    content: '将重新提交审核，审核通过后商品将恢复上架',
    positiveText: '确认上架',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await toggleGoodsStatus(id, 'PENDING_REVIEW')
        message.success('已重新提交审核')
        loadGoods()
      } catch {
        message.error('操作失败')
      }
    },
  })
}

const columns: DataTableColumn<Goods>[] = [
  {
    title: '商品',
    key: 'title',
    width: 200,
    render(row) {
      return h('div', { class: 'flex items-center gap-3 cursor-pointer', onClick: () => goToGoods(row.id) }, [
        h('img', { src: getImageUrl(row.coverImage || row.images?.[0] || ''), class: 'w-12 h-12 object-cover rounded-lg', onerror: (e: Event) => { (e.target as HTMLImageElement).src = '' } }),
        h('span', { class: 'text-sm font-semibold truncate' }, row.title),
      ])
    },
  },
  {
    title: '价格',
    key: 'price',
    width: 100,
    render(row) {
      return h('span', { class: 'text-[#3B82F6] font-bold' }, formatPrice(row.price))
    },
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      const statusMap: Record<string, 'default' | 'success' | 'warning' | 'error'> = {
        ACTIVE: 'success',
        INACTIVE: 'default',
        PENDING_REVIEW: 'warning',
        REJECTED: 'error',
        TAKEN_DOWN: 'error',
      }
      return h(NTag, { type: statusMap[row.status] || 'default', size: 'small' }, { default: () => GOODS_STATUS[row.status] || row.status })
    },
  },
  {
    title: '浏览量',
    key: 'viewCount',
    width: 80,
    render(row) {
      return String(row.viewCount || 0)
    },
  },
  {
    title: '发布时间',
    key: 'createTime',
    width: 100,
    render(row) {
      return h('span', { class: 'text-xs text-gray-400' }, formatDate((row as any).createTime || (row as any).createdAt))
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    render(row) {
      const buttons = []
      if (row.status !== 'FINAL_REJECTED') {
        buttons.push(
          h(NButton, { size: 'tiny', quaternary: true, onClick: () => handleEdit(row) }, {
            icon: () => h(NIcon, null, { default: () => h(Edit24Filled) }),
            default: () => '编辑',
          }),
        )
      }
      if (row.status === 'TAKEN_DOWN' || row.status === 'INACTIVE') {
        buttons.push(
          h(NButton, { size: 'tiny', quaternary: true, type: 'success' as const, onClick: () => handleRelist(row.id) }, { default: () => '重新上架' }),
        )
      }
      buttons.push(
        h(NButton, { size: 'tiny', quaternary: true, type: 'error' as const, onClick: () => handleDelete(row.id) }, {
          icon: () => h(NIcon, null, { default: () => h(Delete24Filled) }),
          default: () => '删除',
        }),
      )
      return h(NSpace, { size: 4 }, { default: () => buttons })
    },
  },
]

onMounted(loadGoods)
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-xl font-bold text-gray-800">我的商品</h2>
      <NButton type="primary" @click="router.push('/publish')">发布商品</NButton>
    </div>

    <NCard :bordered="true" style="border-radius: 12px">
      <NSpin :show="loading">
        <NDataTable
          v-if="goodsList.length > 0"
          :columns="columns"
          :data="goodsList"
          :row-key="(row: Goods) => row.id"
          :bordered="false"
          :single-line="true"
          size="small"
        />
        <NEmpty v-else description="还没有发布商品" />
      </NSpin>
    </NCard>
  </div>
</template>
