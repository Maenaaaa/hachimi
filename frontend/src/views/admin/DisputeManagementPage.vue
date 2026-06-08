<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminDisputes, handleDispute } from '@/api/admin'
import { formatDate } from '@/utils'
import {
  NCard, NButton, NSelect, NTag, NAvatar, NSpin, NDataTable,
  NSpace, NModal, NInput, useMessage, useDialog, NPagination,
  type DataTableColumn,
} from 'naive-ui'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const disputeList = ref<any[]>([])
const loading = ref(true)
const statusFilter = ref<string | null>(null)
const page = ref(1)
const total = ref(0)
const pageSize = ref(20)

const showHandleModal = ref(false)
const handleNote = ref('')
const handleTargetId = ref<number | null>(null)
const handleAction = ref<'RESOLVED' | 'REJECTED'>('RESOLVED')

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待处理', value: 'PENDING' },
  { label: '已处理', value: 'RESOLVED' },
  { label: '已驳回', value: 'REJECTED' },
]

async function loadDisputes() {
  loading.value = true
  try {
    const res = await getAdminDisputes({
      page: page.value,
      size: pageSize.value,
      status: statusFilter.value || undefined,
    })
    disputeList.value = res.data as unknown as any[]
    total.value = (res.data as unknown as any[]).length
  } catch { message.error('加载争议列表失败') }
  finally { loading.value = false }
}

function handleSearch() { page.value = 1; loadDisputes() }

function openHandleModal(id: number, action: 'RESOLVED' | 'REJECTED') {
  handleTargetId.value = id
  handleAction.value = action
  handleNote.value = ''
  showHandleModal.value = true
}

async function confirmHandle() {
  if (!handleTargetId.value) return
  try {
    await handleDispute(handleTargetId.value, handleAction.value, handleNote.value)
    message.success(handleAction.value === 'RESOLVED' ? '已判取消' : '已驳回')
    showHandleModal.value = false
    loadDisputes()
  } catch { message.error('操作失败') }
}

const columns: DataTableColumn<any>[] = [
  {
    title: '申请人', key: 'applicantNickname', width: 120,
    render(row) {
      return h('div', { class: 'flex items-center gap-2' }, [
        h(NAvatar, { size: 24, round: true, style: { backgroundColor: '#3B82F6', fontSize: '12px' } },
          { default: () => row.applicantNickname?.charAt(0) || 'U' }),
        h('span', { class: 'text-sm' }, row.applicantNickname || '未知'),
      ])
    },
  },
  { title: '商品', key: 'goodsTitle', width: 150, ellipsis: { tooltip: true } },
  { title: '原因', key: 'reason', width: 200, ellipsis: { tooltip: true }, render(row) { return row.reason || '--' } },
  {
    title: '状态', key: 'status', width: 80,
    render(row) {
      const map: Record<string, { type: 'default' | 'success' | 'warning' | 'error'; label: string }> = {
        PENDING: { type: 'warning', label: '待处理' },
        RESOLVED: { type: 'success', label: '已判取消' },
        REJECTED: { type: 'default', label: '已驳回' },
      }
      const s = map[row.status] || map.PENDING
      return h(NTag, { type: s.type, size: 'small' }, { default: () => s.label })
    },
  },
  {
    title: '时间', key: 'createTime', width: 110,
    render(row) { return h('span', { class: 'text-xs text-gray-400' }, formatDate(row.createTime)) },
  },
  {
    title: '操作', key: 'actions', width: 180,
    render(row) {
      const buttons = []
      if (row.status === 'PENDING') {
        buttons.push(
          h(NButton, { size: 'tiny', type: 'error' as const, onClick: () => openHandleModal(row.id, 'RESOLVED') }, { default: () => '判取消' }),
          h(NButton, { size: 'tiny', onClick: () => openHandleModal(row.id, 'REJECTED') }, { default: () => '驳回' }),
        )
      }
      if (row.orderId) {
        buttons.push(h(NButton, { size: 'tiny', text: true, type: 'primary' as const, onClick: () => router.push(`/order/${row.orderId}`) }, { default: () => '查看订单' }))
      }
      return h(NSpace, { size: 4 }, { default: () => buttons })
    },
  },
]

onMounted(loadDisputes)
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-xl font-bold text-gray-800">仲裁管理</h2>
      <div class="flex gap-2">
        <NSelect v-model:value="statusFilter" :options="statusOptions" style="width: 120px" clearable @update:value="handleSearch" />
        <NButton type="primary" @click="handleSearch">筛选</NButton>
      </div>
    </div>

    <NCard :bordered="true" style="border-radius: 12px">
      <NSpin :show="loading">
        <NDataTable :columns="columns" :data="disputeList" :row-key="(row: any) => row.id" :bordered="false" :single-line="true" size="small" />
        <div class="flex justify-center mt-4" v-if="total > pageSize">
          <NPagination v-model:page="page" :page-count="Math.ceil(total / pageSize)" :page-size="pageSize" :item-count="total" @update:page="loadDisputes" />
        </div>
      </NSpin>
    </NCard>

    <NModal v-model:show="showHandleModal" :title="handleAction === 'RESOLVED' ? '判取消订单' : '驳回仲裁'" preset="card" style="width: 400px; border-radius: 12px">
      <div class="space-y-4">
        <p class="text-sm text-gray-500">{{ handleAction === 'RESOLVED' ? '判取消后订单将取消，商品重新上架' : '驳回后交易将继续进行' }}</p>
        <NInput v-model:value="handleNote" type="textarea" placeholder="处理说明（选填）" :rows="3" />
        <NButton block :type="handleAction === 'RESOLVED' ? 'error' : 'default'" @click="confirmHandle">
          确认{{ handleAction === 'RESOLVED' ? '判取消' : '驳回' }}
        </NButton>
      </div>
    </NModal>
  </div>
</template>
