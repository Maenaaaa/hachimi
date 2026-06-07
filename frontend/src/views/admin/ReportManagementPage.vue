<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { getAdminReports, handleReport } from '@/api/admin'
import { formatDate, getImageUrl } from '@/utils'
import { REPORT_STATUS } from '@/constants'
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
const handleAction = ref<'resolve' | 'dismiss'>('resolve')

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待处理', value: 'pending' },
  { label: '已处理', value: 'resolved' },
  { label: '已驳回', value: 'dismissed' },
]

async function loadReports() {
  loading.value = true
  try {
    const res = await getAdminReports({
      page: page.value,
      size: pageSize.value,
      status: statusFilter.value || undefined,
    })
    reportList.value = res.data as unknown as any[]
    total.value = (res.data as unknown as any[]).length
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

function openHandleModal(id: number, action: 'resolve' | 'dismiss') {
  handleTargetId.value = id
  handleAction.value = action
  handleNote.value = ''
  showHandleModal.value = true
}

async function confirmHandle() {
  if (!handleTargetId.value) return
  try {
    const status = handleAction.value === 'resolve' ? 'APPROVED' : 'REJECTED'
    await handleReport(handleTargetId.value, status, handleNote.value)
    message.success(handleAction.value === 'resolve' ? '已处理' : '已驳回')
    showHandleModal.value = false
    loadReports()
  } catch {
    message.error('操作失败')
  }
}

const columns: DataTableColumn<Report>[] = [
  {
    title: '举报人',
    key: 'reporterId',
    width: 120,
    render(row) {
      return h('div', { class: 'flex items-center gap-2' }, [
        h(NAvatar, {
          src: getImageUrl(row.reporter?.avatar),
          size: 24,
          round: true,
          style: { backgroundColor: '#3B82F6', fontSize: '12px' },
        }, { default: () => row.reporter?.nickname?.charAt(0) || 'U' }),
        h('span', { class: 'text-sm' }, row.reporter?.nickname),
      ])
    },
  },
  {
    title: '举报类型',
    key: 'targetType',
    width: 100,
    render(row) {
      return h(NTag, { type: row.targetType === 'goods' ? 'info' : 'warning', size: 'small' }, {
        default: () => row.targetType === 'goods' ? '商品' : '用户',
      })
    },
  },
  {
    title: '原因',
    key: 'reason',
    width: 150,
    ellipsis: true,
  },
  {
    title: '描述',
    key: 'description',
    width: 200,
    ellipsis: true,
    render(row) {
      return row.description || '--'
    },
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      const typeMap: Record<string, 'default' | 'success' | 'warning' | 'error'> = {
        pending: 'warning',
        resolved: 'success',
        dismissed: 'default',
      }
      return h(NTag, { type: typeMap[row.status] || 'default', size: 'small' }, {
        default: () => REPORT_STATUS[row.status] || row.status,
      })
    },
  },
  {
    title: '时间',
    key: 'createdAt',
    width: 100,
    render(row) {
      return h('span', { class: 'text-xs text-gray-400' }, formatDate(row.createdAt))
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render(row) {
      if (row.status === 'pending') {
        return h(NSpace, null, {
          default: () => [
            h(NButton, { size: 'tiny', type: 'success' as const, onClick: () => openHandleModal(row.id, 'resolve') }, { default: () => '处理' }),
            h(NButton, { size: 'tiny', onClick: () => openHandleModal(row.id, 'dismiss') }, { default: () => '驳回' }),
          ],
        })
      }
      return h('span', { class: 'text-xs text-gray-400' }, row.handleNote || '--')
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
      :title="handleAction === 'resolve' ? '处理举报' : '驳回举报'"
      preset="card"
      style="width: 400px; border-radius: 12px"
    >
      <div class="space-y-4">
        <NInput
          v-model:value="handleNote"
          type="textarea"
          :placeholder="`输入${handleAction === 'resolve' ? '处理' : '驳回'}备注（选填）`"
          :rows="3"
        />
        <NButton
          block
          :type="handleAction === 'resolve' ? 'success' : 'default'"
          @click="confirmHandle"
        >
          确认{{ handleAction === 'resolve' ? '处理' : '驳回' }}
        </NButton>
      </div>
    </NModal>
  </div>
</template>
