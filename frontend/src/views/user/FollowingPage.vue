<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getFollowing, removeFollow, type UserPublicVO } from '@/api/follow'
import { getAvatarUrl } from '@/utils'
import { NButton, NSpin, NEmpty, NIcon, useMessage } from 'naive-ui'
import { ArrowLeft24Filled } from '@vicons/fluent'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

const followingList = ref<UserPublicVO[]>([])
const loading = ref(true)
const removingId = ref<number | null>(null)

async function loadFollowing() {
  if (!userStore.user?.id) return
  loading.value = true
  try {
    const res = await getFollowing(userStore.user.id)
    followingList.value = res.data || []
  } catch {
    followingList.value = []
  } finally {
    loading.value = false
  }
}

async function handleUnfollow(userId: number) {
  removingId.value = userId
  try {
    await removeFollow(userId)
    followingList.value = followingList.value.filter(u => u.id !== userId)
    message.success('已取消关注')
  } catch {
    message.error('操作失败')
  } finally {
    removingId.value = null
  }
}

function goToProfile(userId: number) {
  router.push(`/profile?userId=${userId}`)
}

onMounted(loadFollowing)
</script>

<template>
  <div class="following-page">
    <div class="page-header">
      <NButton quaternary @click="router.back()">
        <template #icon>
          <NIcon><ArrowLeft24Filled /></NIcon>
        </template>
      </NButton>
      <h2 class="page-title">我的关注</h2>
      <span class="page-count">{{ followingList.length }} 人</span>
    </div>

    <NSpin :show="loading">
      <div v-if="followingList.length > 0" class="following-list">
        <div
          v-for="user in followingList"
          :key="user.id"
          class="following-item"
        >
          <div class="user-info" @click="goToProfile(user.id)">
            <img
              :src="getAvatarUrl(user.avatar, 'thumb_64')"
              class="user-avatar"
            />
            <div class="user-details">
              <span class="user-name">{{ user.nickname || '用户' }}</span>
              <span class="user-school">{{ user.school || '' }}</span>
            </div>
          </div>
          <NButton
            size="small"
            :loading="removingId === user.id"
            @click="handleUnfollow(user.id)"
          >
            取消关注
          </NButton>
        </div>
      </div>
      <NEmpty v-else-if="!loading" description="暂无关注的用户" class="empty-state" />
    </NSpin>
  </div>
</template>

<style scoped>
.following-page {
  max-width: 600px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  flex: 1;
}

.dark .page-title {
  color: #f3f4f6;
}

.page-count {
  font-size: 14px;
  color: #64748b;
}

.following-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.following-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  transition: all 0.2s ease;
}

.following-item:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.dark .following-item {
  background: #1f2937;
  border: 1px solid #374151;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  flex: 1;
  min-width: 0;
}

.user-info:hover .user-name {
  color: #3B82F6;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  border: 2px solid #e0e7ff;
}

.user-details {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  transition: color 0.2s ease;
}

.dark .user-name {
  color: #f3f4f6;
}

.user-school {
  font-size: 13px;
  color: #64748b;
  margin-top: 2px;
}

.empty-state {
  padding: 60px 0;
}
</style>
