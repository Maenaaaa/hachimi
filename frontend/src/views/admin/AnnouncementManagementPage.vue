<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { getAdminAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/admin'
import { formatDate } from '@/utils'
import type { Announcement } from '@/types/entity'
import {
  NCard,
  NButton,
  NIcon,
  NTag,
  NSpin,
  NDataTable,
  NSpace,
  NModal,
  NInput,
  NSelect,
  useMessage,
  useDialog,
  type DataTableColumn,
} from 'naive-ui'
import { Edit24Filled, Delete24Filled, Add24Filled } from '@vicons/fluent'

const message = useMessage()
const dialog = useDialog()

const announcements = ref<Announcement[]>([])
const loading = ref(true)
const showModal = ref(false)
const isEditing = ref(false)
const submitting = ref(false)

const formData = ref({
  title: '',
  content: '',
  id: 0,
})

const priorityOptions = [
  { label: '低', value: 'low' },
  { label: '普通', value: 'normal' },
  { label: '高', value: 'high' },
]

const statusOptions = [
  { label: '发布', value: 'published' },
  { label: '草稿', value: 'draft' },
]

async function loadAnnouncements() {
  loading.value = true
  try {
    const res = await getAdminAnnouncements()
    announcements.value = res.data
  } catch {
    message.error('加载公告列表失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEditing.value = false
  formData.value = { title: '', content: '', priority: 'normal', status: 'published', id: 0 }
  showModal.value = true
}

function openEdit(ann: Announcement) {
  isEditing.value = true
  formData.value = {
    title: ann.title,
    content: ann.content,
    priority: ann.priority,
    status: ann.status,
    id: ann.id,
  }
  showModal.value = true
}

async function handleSubmit() {
  if (!formData.value.title.trim()) {
    message.warning('请输入公告标题')
    return
  }
  if (!formData.value.content.trim()) {
    message.warning('请输入公告内容')
    return
  }

  submitting.value = true
  try {
    if (isEditing.value) {
      await updateAnnouncement(formData.value.id, {
        title: formData.value.title,
        content: formData.value.content,
      })
      message.success('公告已更新')
    } else {
      await createAnnouncement({
        title: formData.value.title,
        content: formData.value.content,
      })
      message.success('公告已创建')
    }
    showModal.value = false
    loadAnnouncements()
  } catch {
    message.error('操作失败')
  } finally {
    submitting.value = false
  }
}

function handleDelete(id: number) {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除该公告吗？',
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteAnnouncement(id)
        message.success('删除成功')
        loadAnnouncements()
      } catch {
        message.error('删除失败')
      }
    },
  })
}

const priorityTag: Record<string, 'default' | 'success' | 'warning' | 'error'> = {
  low: 'default',
  normal: 'warning',
  high: 'error',
}

const priorityLabel: Record<string, string> = {
  low: '低',
  normal: '普通',
  high: '高',
}

const columns: DataTableColumn<Announcement>[] = [
  {
    title: '标题',
    key: 'title',
    width: 200,
    ellipsis: true,
  },
  {
    title: '内容',
    key: 'content',
    width: 300,
    ellipsis: true,
  },
  {
    title: '优先级',
    key: 'priority',
    width: 80,
    render(row) {
      return h(NTag, { type: priorityTag[row.priority] || 'default', size: 'small' }, {
        default: () => priorityLabel[row.priority] || row.priority,
      })
    },
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      return h(NTag, { type: row.status === 'published' ? 'success' : 'default', size: 'small' }, {
        default: () => row.status === 'published' ? '已发布' : '草稿',
      })
    },
  },
  {
    title: '创建时间',
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
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'tiny', quaternary: true, onClick: () => openEdit(row) }, {
            icon: () => h(NIcon, null, { default: () => h(Edit24Filled) }),
            default: () => '编辑',
          }),
          h(NButton, { size: 'tiny', quaternary: true, type: 'error' as const, onClick: () => handleDelete(row.id) }, {
            icon: () => h(NIcon, null, { default: () => h(Delete24Filled) }),
            default: () => '删除',
          }),
        ],
      })
    },
  },
]

onMounted(loadAnnouncements)
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-xl font-bold text-gray-800">公告管理</h2>
      <NButton type="primary" @click="openCreate">
        <template #icon><NIcon><Add24Filled /></NIcon></template>
        新建公告
      </NButton>
    </div>

    <NCard :bordered="true" style="border-radius: 12px">
      <NSpin :show="loading">
        <NDataTable
          :columns="columns"
          :data="announcements"
          :row-key="(row: Announcement) => row.id"
          :bordered="false"
          :single-line="true"
          size="small"
        />
      </NSpin>
    </NCard>

    <!-- Create/Edit Modal -->
    <NModal
      v-model:show="showModal"
      :title="isEditing ? '编辑公告' : '新建公告'"
      preset="card"
      style="width: 600px; border-radius: 12px"
    >
      <div class="space-y-4">
        <NInput
          v-model:value="formData.title"
          placeholder="公告标题"
          :maxlength="100"
          show-count
        />
        <NInput
          v-model:value="formData.content"
          type="textarea"
          placeholder="公告内容"
          :rows="6"
          :maxlength="2000"
          show-count
        />
        <div class="flex gap-4">
          <NSelect
            v-model:value="formData.priority"
            :options="priorityOptions"
            placeholder="优先级"
            style="width: 120px"
          />
          <NSelect
            v-model:value="formData.status"
            :options="statusOptions"
            placeholder="状态"
            style="width: 120px"
          />
        </div>
        <NButton type="primary" block :loading="submitting" @click="handleSubmit">
          {{ isEditing ? '更新' : '创建' }}
        </NButton>
      </div>
    </NModal>
  </div>
</template>
