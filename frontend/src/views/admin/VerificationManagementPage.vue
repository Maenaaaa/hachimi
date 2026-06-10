<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { getAdminVerifications, approveVerification, rejectVerification } from '@/api/admin'
import { getImageUrl, getAvatarUrl, formatDate } from '@/utils'
import type { Verification } from '@/types/entity'
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

const verificationList = ref<Verification[]>([])
const loading = ref(true)
const statusFilter = ref<string | null>(null)
const page = ref(1)
const total = ref(0)
const pageSize = ref(20)

const showRejectModal = ref(false)
const rejectReason = ref('')
const rejectVerificationId = ref<number | null>(null)
const showDetailModal = ref(false)
const detailRecord = ref<Verification | null>(null)

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
]

async function loadVerifications() {
  loading.value = true
  try {
    const res = await getAdminVerifications({
      page: page.value,
      size: pageSize.value,
      status: statusFilter.value || undefined,
    })
    const pageData = res.data as unknown as { records: Verification[]; total: number }
    verificationList.value = pageData?.records || []
    total.value = pageData?.total || 0
  } catch {
    message.error('加载认证列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  loadVerifications()
}

function handleApprove(authId: number) {
  dialog.success({
    title: '确认通过',
    content: '确定要通过该用户的实名认证吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await approveVerification(authId)
        message.success('认证已通过')
        loadVerifications()
      } catch {
        message.error('操作失败')
      }
    },
  })
}

function handleReject(authId: number) {
  rejectVerificationId.value = authId
  rejectReason.value = ''
  showRejectModal.value = true
}

function handleDetail(row: Verification) {
  detailRecord.value = row
  showDetailModal.value = true
}

async function confirmReject() {
  if (!rejectVerificationId.value) return
  try {
    await rejectVerification(rejectVerificationId.value, rejectReason.value)
    message.success('已拒绝')
    showRejectModal.value = false
    loadVerifications()
  } catch {
    message.error('操作失败')
  }
}

const columns: DataTableColumn<Verification>[] = [
  {
    title: '用户',
    key: 'nickname',
    width: 160,
    render(row) {
      return h('div', { class: 'flex items-center gap-3' }, [
        h('img', { src: getAvatarUrl(row.avatar, 'thumb_64'), class: 'w-9 h-9 rounded-full object-cover' }),
        h('div', null, [
          h('div', { class: 'font-semibold text-sm' }, row.nickname),
          h('div', { class: 'text-xs text-gray-400' }, '@' + row.username),
        ]),
      ])
    },
  },
  {
    title: '真实姓名',
    key: 'realName',
    width: 90,
  },
  {
    title: '学号',
    key: 'studentId',
    width: 110,
  },
  {
    title: '认证称号',
    key: 'authTitle',
    width: 110,
  },
  {
    title: '认证状态',
    key: 'verificationStatus',
    width: 90,
    render(row) {
      const statusMap: Record<string, { type: 'default' | 'success' | 'warning' | 'error'; label: string }> = {
        PENDING: { type: 'warning', label: '待审核' },
        APPROVED: { type: 'success', label: '已通过' },
        REJECTED: { type: 'error', label: '已拒绝' },
      }
      const status = statusMap[row.verificationStatus || 'PENDING'] || statusMap.PENDING
      return h(NTag, { type: status.type, size: 'small' }, {
        default: () => status.label,
      })
    },
  },
  {
    title: '申请时间',
    key: 'createTime',
    width: 110,
    render(row) {
      return h('span', { class: 'text-xs text-gray-400' }, formatDate(row.createTime))
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    render(row) {
      if (row.verificationStatus === 'PENDING') {
        return h(NSpace, null, {
          default: () => [
            h(NButton, { size: 'tiny', onClick: () => handleDetail(row) }, { default: () => '查看' }),
            h(NButton, { size: 'tiny', type: 'success' as const, onClick: () => handleApprove(row.authId) }, { default: () => '通过' }),
            h(NButton, { size: 'tiny', type: 'error' as const, onClick: () => handleReject(row.authId) }, { default: () => '拒绝' }),
          ],
        })
      }
      return h('span', { class: 'text-xs text-gray-400' }, '--')
    },
  },
]

onMounted(loadVerifications)
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-xl font-bold text-gray-800">实名认证审核</h2>
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
          :data="verificationList"
          :row-key="(row: Verification) => row.authId"
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
            @update:page="loadVerifications"
          />
        </div>
      </NSpin>
    </NCard>

    <!-- Reject Modal -->
    <NModal
      v-model:show="showRejectModal"
      title="拒绝认证"
      preset="card"
      style="width: 400px; border-radius: 12px"
    >
      <div class="space-y-4">
        <NInput
          v-model:value="rejectReason"
          type="textarea"
          placeholder="输入拒绝原因（选填）"
          :rows="3"
        />
        <NButton type="error" block @click="confirmReject">确认拒绝</NButton>
      </div>
    </NModal>

    <!-- Detail Modal -->
    <NModal
      v-model:show="showDetailModal"
      title="认证详情"
      preset="card"
      style="width: 500px; border-radius: 12px"
    >
      <div v-if="detailRecord" class="space-y-3">
        <div class="flex justify-center">
          <img
            v-if="detailRecord.idCardImage"
            :src="getImageUrl(detailRecord.idCardImage)"
            style="max-width: 100%; max-height: 400px; border-radius: 8px; object-fit: contain"
          />
          <div v-else class="text-gray-400">无图片</div>
        </div>
        <div><strong>真实姓名：</strong>{{ detailRecord.realName }}</div>
        <div><strong>学号：</strong>{{ detailRecord.studentId }}</div>
        <div><strong>认证称号：</strong>{{ detailRecord.authTitle }}</div>
      </div>
    </NModal>
  </div>
</template>
