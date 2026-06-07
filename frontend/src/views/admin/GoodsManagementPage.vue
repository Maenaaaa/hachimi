<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { getAdminGoods, approveGoods, rejectGoods, takeDownGoods } from '@/api/admin'
import { formatPrice, formatDate, getImageUrl } from '@/utils'
import { GOODS_STATUS } from '@/constants'
import type { Goods } from '@/types/entity'
import {
  NCard,
  NButton,
  NInput,
  NSelect,
  NTag,
  NAvatar,
  NSpin,
  NDataTable,
  NSpace,
  NModal,
  NInput as NInputModal,
  useMessage,
  useDialog,
  NPagination,
  type DataTableColumn,
} from 'naive-ui'

const message = useMessage()
const dialog = useDialog()

const goodsList = ref<Goods[]>([])
const loading = ref(true)
const keyword = ref('')
const statusFilter = ref<string | null>(null)
const page = ref(1)
const total = ref(0)
const pageSize = ref(20)

const showRejectModal = ref(false)
const rejectReason = ref('')
const rejectGoodsId = ref<number | null>(null)

const statusOptions = [
  { label: '全部', value: '' },
  { label: '在售', value: 'ACTIVE' },
  { label: '待审核', value: 'PENDING_REVIEW' },
  { label: '已下架', value: 'INACTIVE' },
  { label: '未通过', value: 'REJECTED' },
  { label: '强制下架', value: 'TAKEN_DOWN' },
]

async function loadGoods() {
  loading.value = true
  try {
    const res = await getAdminGoods({
      page: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
    })
    goodsList.value = res.data as unknown as any[]
    total.value = (res.data as unknown as any[]).length
  } catch {
    message.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  loadGoods()
}

function handleApprove(id: number) {
  dialog.success({
    title: '确认审核',
    content: '确定要通过该商品审核吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await approveGoods(id)
        message.success('审核通过')
        loadGoods()
      } catch {
        message.error('操作失败')
      }
    },
  })
}

function handleReject(id: number) {
  rejectGoodsId.value = id
  rejectReason.value = ''
  showRejectModal.value = true
}

async function confirmReject() {
  if (!rejectGoodsId.value) return
  try {
    await rejectGoods(rejectGoodsId.value, rejectReason.value)
    message.success('已驳回')
    showRejectModal.value = false
    loadGoods()
  } catch {
    message.error('操作失败')
  }
}

function handleTakeDown(id: number) {
  dialog.warning({
    title: '确认下架',
    content: '确定要下架该商品吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await takeDownGoods(id)
        message.success('已下架')
        loadGoods()
      } catch {
        message.error('操作失败')
      }
    },
  })
}

const columns: DataTableColumn<any>[] = [
  {
    title: '商品',
    key: 'title',
    width: 220,
    render(row) {
      return h('div', { class: 'flex items-center gap-3' }, [
        h('img', { src: row.coverImage || '', class: 'w-12 h-12 object-cover rounded-lg' }),
        h('div', null, [
          h('div', { class: 'text-sm font-semibold truncate max-w-150px' }, row.title),
          h('div', { class: 'text-xs text-gray-400' }, row.categoryName),
        ]),
      ])
    },
  },
  {
    title: '卖家',
    key: 'userId',
    width: 120,
    render(row) {
      return h('div', { class: 'flex items-center gap-2' }, [
        h(NAvatar, {
          src: row.userAvatar || undefined,
          size: 24,
          round: true,
          style: { backgroundColor: '#3B82F6', fontSize: '12px' },
        }, { default: () => row.userNickname?.charAt(0) || 'U' }),
        h('span', { class: 'text-sm' }, row.userNickname),
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
    width: 90,
    render(row) {
      const typeMap: Record<string, 'default' | 'success' | 'warning' | 'error' | 'info'> = {
        ACTIVE: 'success',
        PENDING_REVIEW: 'warning',
        INACTIVE: 'default',
        REJECTED: 'error',
        TAKEN_DOWN: 'default',
      }
      return h(NTag, { type: typeMap[row.status] || 'default', size: 'small' }, {
        default: () => GOODS_STATUS[row.status] || row.status,
      })
    },
  },
  {
    title: '发布时间',
    key: 'createTime',
    width: 110,
    render(row) {
      return h('span', { class: 'text-xs text-gray-400' }, formatDate(row.createTime))
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render(row) {
      if (row.status === 'PENDING_REVIEW') {
        return h(NSpace, null, {
          default: () => [
            h(NButton, { size: 'tiny', type: 'success' as const, onClick: () => handleApprove(row.id) }, { default: () => '通过' }),
            h(NButton, { size: 'tiny', type: 'error' as const, onClick: () => handleReject(row.id) }, { default: () => '驳回' }),
          ],
        })
      }
      if (row.status === 'ACTIVE') {
        return h(NButton, { size: 'tiny', type: 'warning' as const, onClick: () => handleTakeDown(row.id) }, { default: () => '下架' })
      }
      return h('span', { class: 'text-xs text-gray-400' }, '--')
    },
  },
]

onMounted(loadGoods)
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-xl font-bold text-gray-800">商品管理</h2>
      <div class="flex gap-2">
        <NSelect
          v-model:value="statusFilter"
          :options="statusOptions"
          style="width: 120px"
          placeholder="状态"
          clearable
          @update:value="handleSearch"
        />
        <NInput
          v-model:value="keyword"
          placeholder="搜索商品"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
        <NButton type="primary" @click="handleSearch">搜索</NButton>
      </div>
    </div>

    <NCard :bordered="true" style="border-radius: 12px">
      <NSpin :show="loading">
        <NDataTable
          :columns="columns"
          :data="goodsList"
          :row-key="(row: any) => row.id"
          :bordered="false"
          :single-line="true"
          size="small"
        />
        <div class="flex justify-center mt-4" v-if="total > pageSize">
          <NPagination
            v-model:page="page"
            :page-count="Math.ceil(total / pageSize)"
            :page-size="pageSize"
            :item-count="total"
            @update:page="loadGoods"
          />
        </div>
      </NSpin>
    </NCard>

    <!-- Reject Modal -->
    <NModal v-model:show="showRejectModal" title="驳回原因" preset="card" style="width: 400px; border-radius: 12px">
      <div class="space-y-4">
        <NInput
          v-model:value="rejectReason"
          type="textarea"
          placeholder="输入驳回原因（选填）"
          :rows="3"
        />
        <NButton type="primary" block @click="confirmReject">确认驳回</NButton>
      </div>
    </NModal>
  </div>
</template>
