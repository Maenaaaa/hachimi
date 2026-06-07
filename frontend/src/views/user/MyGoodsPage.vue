<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyGoods, deleteGoods, updateGoods } from '@/api/goods'
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
const showEditModal = ref(false)
const editForm = ref({ title: '', price: 0, description: '' })
const editingId = ref<number | null>(null)

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
  editingId.value = goods.id
  editForm.value = {
    title: goods.title,
    price: goods.price,
    description: goods.description,
  }
  showEditModal.value = true
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

async function saveEdit() {
  if (!editingId.value) return
  try {
    await updateGoods(editingId.value, editForm.value)
    message.success('修改成功')
    showEditModal.value = false
    loadGoods()
  } catch {
    message.error('修改失败')
  }
}

function goToGoods(id: number) {
  router.push(`/goods/${id}`)
}

const columns: DataTableColumn<Goods>[] = [
  {
    title: '商品',
    key: 'title',
    width: 200,
    render(row) {
      return h('div', { class: 'flex items-center gap-3 cursor-pointer', onClick: () => goToGoods(row.id) }, [
        h('img', { src: getImageUrl(row.images?.[0]), class: 'w-12 h-12 object-cover rounded-lg' }),
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
        active: 'success',
        inactive: 'default',
        sold: 'warning',
        pending: 'warning',
        rejected: 'error',
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
    key: 'createdAt',
    width: 100,
    render(row) {
      return h('span', { class: 'text-xs text-gray-400' }, formatDate(row.createdAt))
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton,
            { size: 'tiny', quaternary: true, onClick: () => handleEdit(row) },
            {
              icon: () => h(NIcon, null, { default: () => h(Edit24Filled) }),
              default: () => '编辑',
            },
          ),
          h(NButton,
            { size: 'tiny', quaternary: true, type: 'error' as const, onClick: () => handleDelete(row.id) },
            {
              icon: () => h(NIcon, null, { default: () => h(Delete24Filled) }),
              default: () => '删除',
            },
          ),
        ],
      })
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

    <NModal v-model:show="showEditModal" title="编辑商品" preset="card" style="width: 500px; border-radius: 12px">
      <div class="space-y-4">
        <NInput v-model:value="editForm.title" placeholder="商品标题" />
        <NInput :value="editForm.price ? String(editForm.price) : ''" placeholder="价格" :input-props="{ type: 'number', min: 0, step: 0.01 }" @update:value="(v: string) => { editForm.price = v ? Number(v) : 0 }" />
        <NInput v-model:value="editForm.description" type="textarea" placeholder="描述" :rows="4" />
        <NButton type="primary" block @click="saveEdit">保存修改</NButton>
      </div>
    </NModal>
  </div>
</template>
