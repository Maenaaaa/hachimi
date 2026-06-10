<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminReports, handleReport } from '@/api/admin'
import { formatDate, getAvatarUrl } from '@/utils'
import type { Report } from '@/types/entity'
import {
  NCard,
  NButton,
  NSelect,
  NTag,
  NAvatar,
  NSpin,
  NDataTable,
  NSpace,
  NModal,
  NInput,
  useMessage,
  useDialog,
  NPagination,
  type DataTableColumn,
} from 'naive-ui'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const reportList = ref<Report[]>([])
const loading = ref(true)
const statusFilter = ref<string | null>(null)
const page = ref(1)
const total = ref(0)
const pageSize = ref(20)

const showHandleModal = ref(false)
const handleNote = ref('')
const handleTargetId = ref<number | null>(null)
const handleAction = ref<'APPROVED' | 'REJECTED'>('APPROVED')

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待处理', value: 'PENDING' },
  { label: '已处理', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
]

const reasonLabels: Record<string, string> = {
  FALSE_INFO: '虚假信息',
  FRAUD: '欺诈行为',
  AD: '广告内容',
  VIOLATION: '违规内容',
}

async function loadReports() {
  loading.value = true
  try {
    const res = await getAdminReports({
      page: page.value,
      size: pageSize.value,
      status: statusFilter.value || undefined,
    })
    const pageData = res.data as unknown as { records: Report[]; total: number }
    reportList.value = pageData?.records || []
    total.value = pageData?.total || 0
  } catch {
    message.error('加载举报列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  loadReports()
}

function openHandleModal(id: number, action: 'APPROVED' | 'REJECTED') {
  handleTargetId.value = id
  handleAction.value = action
  handleNote.value = ''
  showHandleModal.value = true
}

async function confirmHandle() {
  if (!handleTargetId.value) return
  try {
    await handleReport(handleTargetId.value, handleAction.value, handleNote.value)
    message.success(handleAction.value === 'APPROVED' ? '已处理' : '已驳回')
    showHandleModal.value = false
    loadReports()
  } catch {
    message.error('操作失败')
  }
}

const columns: DataTableColumn<Report>[] = [
  {
    title: '举报人',
    key: 'reporterNickname',
    width: 120,
    render(row) {
      return h('div', { class: 'flex items-center gap-2' }, [
        h('img', {
          src: getAvatarUrl(row.reporterAvatar, 'thumb_64'),
          class: 'w-6 h-6 rounded-full object-cover',
          onError: (e: Event) => { (e.target as HTMLImageElement).src = '/default-avatar.svg' },
        }),
        h('span', { class: 'text-sm' }, row.reporterNickname || '未知用户'),
      ])
    },
  },
  {
    title: '举报类型',
    key: 'type',
    width: 100,
    render(row) {
      return h(NTag, { type: row.type === 'GOODS' ? 'info' : 'warning', size: 'small' }, {
        default: () => row.type === 'GOODS' ? '商品' : '用户',
      })
    },
  },
  {
    title: '原因',
    key: 'reason',
    width: 120,
    render(row) {
      return reasonLabels[row.reason] || row.reason
    },
  },
  {
    title: '描述',
    key: 'description',
    width: 200,
    ellipsis: { tooltip: true },
    render(row) {
      return row.description || '--'
    },
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      const statusMap: Record<string, 'default' | 'success' | 'warning' | 'error'> = {
        PENDING: 'warning',
        APPROVED: 'success',
        REJECTED: 'default',
      }
      const labelMap: Record<string, string> = {
        PENDING: '待处理',
        APPROVED: '已处理',
        REJECTED: '已驳回',
      }
      return h(NTag, { type: statusMap[row.status] || 'default', size: 'small' }, {
        default: () => labelMap[row.status] || row.status,
      })
    },
  },
  {
    title: '时间',
    key: 'createTime',
    width: 110,
    render(row) {
      return h('span', { class: 'text-xs text-gray-400' }, formatDate((row as any).createTime || (row as any).createTime))
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render(row) {
      const buttons = []
      if (row.status === 'PENDING') {
        buttons.push(
          h(NButton, { size: 'tiny', type: 'success' as const, onClick: () => openHandleModal(row.id, 'APPROVED') }, { default: () => '处理' }),
          h(NButton, { size: 'tiny', type: 'error' as const, onClick: () => openHandleModal(row.id, 'REJECTED') }, { default: () => '驳回' }),
        )
      }
      if (row.type === 'GOODS' && row.targetId) {
        buttons.push(
          h(NButton, { size: 'tiny', text: true, type: 'primary' as const, onClick: () => { window.open(`/goods/${row.targetId}`, '_blank') } }, { default: () => '查看商品' }),
        )
      }
      if (buttons.length === 0) {
        return h('span', { class: 'text-xs text-gray-400' }, row.handleNote || '--')
      }
      return h(NSpace, { size: 4 }, { default: () => buttons })
    },
  },
]

onMounted(loadReports)
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-xl font-bold text-gray-800">举报管理</h2>
      <div class="flex gap-2">
        <NSelect
          v-model:value="statusFilter"
          :options="statusOptions"
          style="width: 120px"
          placeholder="状态"
          clearable
          @update:value="handleSearch"
        />
        <NButton type="primary" @click="handleSearch">筛选</NButton>
      </div>
    </div>

    <NCard :bordered="true" style="border-radius: 12px">
      <NSpin :show="loading">
        <NDataTable
          :columns="columns"
          :data="reportList"
          :row-key="(row: Report) => row.id"
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
            @update:page="loadReports"
          />
        </div>
      </NSpin>
    </NCard>

    <!-- Handle Modal -->
    <NModal
      v-model:show="showHandleModal"
      :title="handleAction === 'APPROVED' ? '处理举报' : '驳回举报'"
      preset="card"
      style="width: 400px; border-radius: 12px"
    >
      <div class="space-y-4">
        <NInput
          v-model:value="handleNote"
          type="textarea"
          :placeholder="`输入${handleAction === 'APPROVED' ? '处理' : '驳回'}备注（选填）`"
          :rows="3"
        />
        <NButton
          block
          :type="handleAction === 'APPROVED' ? 'success' : 'error'"
          @click="confirmHandle"
        >
          确认{{ handleAction === 'APPROVED' ? '处理' : '驳回' }}
        </NButton>
      </div>
    </NModal>
  </div>
</template>
