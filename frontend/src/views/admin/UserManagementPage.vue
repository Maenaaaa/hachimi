<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { getAdminUsers, disableUser, enableUser } from '@/api/admin'
import { getImageUrl, getAvatarUrl, formatDate } from '@/utils'
import type { User } from '@/types/entity'
import {
  NCard,
  NButton,
  NInput,
  NTag,
  NAvatar,
  NSpin,
  NDataTable,
  NSpace,
  NDrawer,
  NDrawerContent,
  NDescriptions,
  NDescriptionsItem,
  useMessage,
  useDialog,
  NPagination,
  type DataTableColumn,
} from 'naive-ui'

const message = useMessage()
const dialog = useDialog()

const users = ref<User[]>([])
const loading = ref(true)
const keyword = ref('')
const page = ref(1)
const total = ref(0)
const pageSize = ref(20)
const selectedUser = ref<User | null>(null)
const showDrawer = ref(false)

async function loadUsers() {
  loading.value = true
  try {
    const res = await getAdminUsers({
      page: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
    })
    const pageData = res.data as unknown as { records: User[]; total: number }
    users.value = pageData?.records || []
    total.value = pageData?.total || 0
  } catch {
    message.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  loadUsers()
}

function handleDisable(user: User) {
  dialog.warning({
    title: '确认操作',
    content: `确定要${user.status === 'DISABLED' ? '启用' : '禁用'}用户 "${user.nickname}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        if (user.status === 'DISABLED') {
          await enableUser(user.id)
        } else {
          await disableUser(user.id)
        }
        message.success(`${user.status === 'DISABLED' ? '启用' : '禁用'}成功`)
        loadUsers()
      } catch {
        message.error('操作失败')
      }
    },
  })
}

function showUserDetail(user: User) {
  selectedUser.value = user
  showDrawer.value = true
}

const columns: DataTableColumn<User>[] = [
  {
    title: '用户',
    key: 'nickname',
    width: 200,
    render(row) {
      return h('div', { class: 'flex items-center gap-3 cursor-pointer', onClick: () => showUserDetail(row) }, [
        h('img', { src: getAvatarUrl(row.avatar, 'thumb_64'), class: 'w-9 h-9 rounded-full object-cover' }),
        h('div', null, [
          h('div', { class: 'font-semibold text-sm' }, row.nickname),
          h('div', { class: 'text-xs text-gray-400' }, '@' + row.username),
        ]),
      ])
    },
  },
  {
    title: '角色',
    key: 'role',
    width: 80,
    render(row) {
      return h(NTag, { type: row.role?.toUpperCase() === 'ADMIN' ? 'warning' : 'default', size: 'small' }, {
        default: () => row.role?.toUpperCase() === 'ADMIN' ? '管理员' : '用户',
      })
    },
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      return h(NTag, { type: row.status === 'ACTIVE' ? 'success' : 'error', size: 'small' }, {
        default: () => row.status === 'ACTIVE' ? '正常' : '已禁用',
      })
    },
  },
  {
    title: '认证',
    key: 'realName',
    width: 70,
    render(row) {
      return h(NTag, { type: row.realName ? 'success' : 'default', size: 'small' }, {
        default: () => row.realName ? '已认证' : '未认证',
      })
    },
  },
  {
    title: '注册时间',
    key: 'createTime',
    width: 100,
    render(row) {
      return h('span', { class: 'text-xs text-gray-400' }, formatDate(row.createTime))
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'tiny', quaternary: true, onClick: () => showUserDetail(row) }, { default: () => '详情' }),
          h(NButton, {
            size: 'tiny',
            quaternary: true,
            type: (row.status === 'DISABLED' ? 'success' : 'error') as 'success' | 'error',
            onClick: () => handleDisable(row),
          }, { default: () => row.status === 'DISABLED' ? '启用' : '禁用' }),
        ],
      })
    },
  },
]

onMounted(loadUsers)
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-xl font-bold text-gray-800">用户管理</h2>
      <div class="flex gap-2">
        <NInput
          v-model:value="keyword"
          placeholder="搜索用户名/昵称"
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
          :data="users"
          :row-key="(row: User) => row.id"
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
            @update:page="loadUsers"
          />
        </div>
      </NSpin>
    </NCard>

    <!-- User Detail Drawer -->
    <NDrawer v-model:show="showDrawer" :width="400" placement="right">
      <NDrawerContent title="用户详情" closable>
        <template v-if="selectedUser">
          <div class="text-center mb-6">
            <img
              :src="getAvatarUrl(selectedUser.avatar, 'thumb_256')"
              class="w-20 h-20 rounded-full object-cover mx-auto"
            />
            <h3 class="text-lg font-bold mt-2">{{ selectedUser.nickname }}</h3>
            <p class="text-sm text-gray-400">@{{ selectedUser.username }}</p>
          </div>

          <NDescriptions :column="1" label-placement="left" bordered size="small">
            <NDescriptionsItem label="邮箱">{{ selectedUser.email || '未设置' }}</NDescriptionsItem>
            <NDescriptionsItem label="手机号">{{ selectedUser.phone || '未设置' }}</NDescriptionsItem>
            <NDescriptionsItem label="角色">{{ selectedUser.role?.toUpperCase() === 'ADMIN' ? '管理员' : '普通用户' }}</NDescriptionsItem>
            <NDescriptionsItem label="状态">{{ selectedUser.status === 'ACTIVE' ? '正常' : '已禁用' }}</NDescriptionsItem>
            <NDescriptionsItem label="实名认证">{{ selectedUser.realName ? '已认证' : '未认证' }}</NDescriptionsItem>
            <NDescriptionsItem label="注册时间">{{ formatDate(selectedUser.createTime) }}</NDescriptionsItem>
          </NDescriptions>
        </template>
      </NDrawerContent>
    </NDrawer>
  </div>
</template>
