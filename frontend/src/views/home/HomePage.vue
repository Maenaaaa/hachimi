<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getGoodsRecommend, getGoodsLatest } from '@/api/goods'
import { getCategories } from '@/api/category'
import { getAnnouncements } from '@/api/announcement'
import { formatPrice, formatDate, getImageUrl, getAvatarUrl } from '@/utils'
import type { Goods, Category, Announcement } from '@/types/entity'
import { useUserStore } from '@/stores/user'
import { getUserPublicProfile, type UserPublicVO } from '@/api/user'
import {
  NCarousel,
  NTag,
  NButton,
  NSpin,
  NEmpty,
  NModal,
  NCheckbox,
  NIcon,
  NTooltip,
  useMessage,
} from 'naive-ui'
import {
  Heart24Filled,
  Heart24Regular,
  Star24Filled,
  Star24Regular,
  Clock24Filled,
  ThumbLike24Filled,
  Tag24Filled,
} from '@vicons/fluent'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const banners = [
  { id: 1, image: 'https://picsum.photos/seed/banner1/1200/400', title: '闲置变宝藏', subtitle: '让你的闲置物品找到新主人' },
  { id: 2, image: 'https://picsum.photos/seed/banner2/1200/400', title: '绿色校园', subtitle: '低碳生活，从闲置交换开始' },
  { id: 3, image: 'https://picsum.photos/seed/banner3/1200/400', title: '校园集市', subtitle: '发现身边的好物' },
]

const hotTags = ['教材', 'iPad', '自行车', '键盘', '显示器', '考研', '运动鞋', '相机']

const categories = ref<Category[]>([])
const recommendGoods = ref<Goods[]>([])
const latestGoods = ref<Goods[]>([])
const announcements = ref<Announcement[]>([])
const loading = ref(true)
const currentBannerIndex = ref(0)
const userStats = ref<{ goodsCount: number; followingCount: number }>({ goodsCount: 0, followingCount: 0 })

const showAnnouncementModal = ref(false)
const currentAnnouncementIndex = ref(0)
const hideAnnouncements = ref(false)
const currentAnnouncement = computed(() => announcements.value[currentAnnouncementIndex.value] || null)
const hasMoreAnnouncements = computed(() => currentAnnouncementIndex.value < announcements.value.length - 1)
const dismissedKey = 'announcement_dismissed_ids'

function getDismissedIds(): Set<number> {
  try {
    const raw = localStorage.getItem(dismissedKey)
    return raw ? new Set(JSON.parse(raw)) : new Set()
  } catch {
    return new Set()
  }
}

function saveDismissedIds(ids: Set<number>) {
  localStorage.setItem(dismissedKey, JSON.stringify([...ids]))
}

function dismissCurrentAnnouncement() {
  if (!currentAnnouncement.value) return
  const dismissed = getDismissedIds()
  dismissed.add(currentAnnouncement.value.id)
  saveDismissedIds(dismissed)
}

function handleNextAnnouncement() {
  if (hideAnnouncements.value) {
    dismissCurrentAnnouncement()
  }
  if (hasMoreAnnouncements.value) {
    currentAnnouncementIndex.value++
    hideAnnouncements.value = false
  } else {
    showAnnouncementModal.value = false
  }
}

watch(showAnnouncementModal, (val) => {
  if (!val && hideAnnouncements.value) {
    dismissCurrentAnnouncement()
  }
})

onMounted(async () => {
  try {
    const [catRes, recRes, latestRes, annRes] = await Promise.allSettled([
      getCategories(),
      getGoodsRecommend(),
      getGoodsLatest(),
      getAnnouncements(),
    ])

    if (catRes.status === 'fulfilled') categories.value = catRes.value.data
    if (recRes.status === 'fulfilled') recommendGoods.value = recRes.value.data
    if (latestRes.status === 'fulfilled') latestGoods.value = latestRes.value.data
    if (annRes.status === 'fulfilled') {
      const dismissed = getDismissedIds()
      const unread = (annRes.value.data as Announcement[]).filter(a => !dismissed.has(a.id))
      if (unread.length > 0) {
        announcements.value = unread
        currentAnnouncementIndex.value = 0
        showAnnouncementModal.value = true
      }
    }
  } finally {
    loading.value = false
  }
})

async function fetchUserStats() {
  if (!userStore.user?.id) return
  try {
    const res = await getUserPublicProfile(userStore.user.id)
    const data = res.data as UserPublicVO
    userStats.value = { goodsCount: data.goodsCount || 0, followingCount: data.followingCount || 0 }
  } catch { /* ignore */ }
}

watch(() => userStore.user, (val) => {
  if (val?.id) fetchUserStats()
}, { immediate: true })

function goToCategory(catId: number) {
  router.push({ path: '/search', query: { categoryId: String(catId) } })
}

function goToGoods(id: number) {
  router.push(`/goods/${id}`)
}

function goToSearch(keyword: string) {
  router.push({ path: '/search', query: { keyword } })
}

function toggleFavorite(goods: Goods, event: Event) {
  event.stopPropagation()
  message.info('收藏功能开发中')
}

const categoryIcons: Record<string, string> = {
  '教材书籍': `<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>`,
  '数码产品': `<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"/><line x1="12" y1="18" x2="12.01" y2="18"/></svg>`,
  '宿舍用品': `<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>`,
  '体育用品': `<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/><path d="M2 12h20"/></svg>`,
  '自行车': `<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="5.5" cy="17.5" r="3.5"/><circle cx="18.5" cy="17.5" r="3.5"/><path d="M15 6a1 1 0 1 0 0-2 1 1 0 0 0 0 2zm-3 11.5V14l-3-3 4-3 2 3h3"/></svg>`,
  '服饰鞋包': `<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20.38 3.46L16 2 12 5 8 2 3.62 3.46a2 2 0 0 0-1.34 2.23l.58 3.47a1 1 0 0 0 .99.84H6v10c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V10h2.15a1 1 0 0 0 .99-.84l.58-3.47a2 2 0 0 0-1.34-2.23z"/></svg>`,
  '其他': `<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/></svg>`,
}

const categoryColors: Record<string, { bg: string; text: string }> = {
  '教材书籍': { bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', text: '#fff' },
  '数码产品': { bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', text: '#fff' },
  '宿舍用品': { bg: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)', text: '#fff' },
  '体育用品': { bg: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', text: '#fff' },
  '自行车': { bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', text: '#fff' },
  '服饰鞋包': { bg: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)', text: '#fff' },
  '其他': { bg: 'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)', text: '#fff' },
}

function getCategoryIcon(name: string) {
  return categoryIcons[name] || categoryIcons['其他']
}

function getCategoryColor(name: string) {
  return categoryColors[name] || categoryColors['其他']
}
</script>

<template>
  <div class="home-container">
    <!-- 左侧主内容 -->
    <div class="main-content">
      <!-- Banner Carousel -->
      <div class="banner-wrapper">
        <NCarousel
          autoplay
          :interval="4000"
          effect="slide"
          show-arrow
          class="banner-carousel"
          @update:current-index="currentBannerIndex = $event"
        >
          <div v-for="banner in banners" :key="banner.id" class="banner-slide">
            <img :src="banner.image" :alt="banner.title" class="banner-image" />
            <div class="banner-overlay">
              <div class="banner-text">
                <h2 class="banner-title">{{ banner.title }}</h2>
                <p class="banner-subtitle">{{ banner.subtitle }}</p>
              </div>
            </div>
          </div>
          <template #dots>
            <div class="banner-dots">
              <div
                v-for="(_, index) in banners"
                :key="index"
                class="banner-dot"
                :class="{ active: currentBannerIndex === index }"
              />
            </div>
          </template>
        </NCarousel>
      </div>

      <!-- Search Tags -->
      <div class="search-tags-section">
        <div class="tags-header">
          <NIcon :size="16" class="text-[#3B82F6]"><ThumbLike24Filled /></NIcon>
          <span class="tags-label">热门搜索</span>
        </div>
        <div class="tags-list">
          <span
            v-for="tag in hotTags"
            :key="tag"
            class="search-tag"
            @click="goToSearch(tag)"
          >
            {{ tag }}
          </span>
        </div>
      </div>

      <!-- Category Navigation -->
      <div class="category-section">
        <div class="section-header">
          <h3 class="section-title">分类浏览</h3>
          <NButton text type="primary" @click="router.push('/search')">全部分类 →</NButton>
        </div>
        <NSpin :show="loading && categories.length === 0">
          <div class="category-grid">
            <div
              v-for="cat in categories"
              :key="cat.id"
              class="category-card"
              @click="goToCategory(cat.id)"
            >
              <div class="category-icon" :style="{ background: getCategoryColor(cat.name).bg }">
                <div v-html="getCategoryIcon(cat.name)"></div>
              </div>
              <span class="category-name">{{ cat.name }}</span>
            </div>
          </div>
        </NSpin>
      </div>

      <!-- Recommended Goods -->
      <div class="goods-section">
        <div class="section-header">
          <h3 class="section-title">推荐好物</h3>
          <NButton text type="primary" @click="router.push('/search')">查看更多 →</NButton>
        </div>

        <NSpin :show="loading && recommendGoods.length === 0">
          <div v-if="recommendGoods.length > 0" class="goods-grid">
            <div
              v-for="goods in recommendGoods"
              :key="goods.id"
              class="goods-card"
              @click="goToGoods(goods.id)"
            >
              <div class="goods-image-wrapper">
                <img
                  :src="getImageUrl(goods.coverImage)"
                  :alt="goods.title"
                  class="goods-image"
                  loading="lazy"
                  @error="(e: Event) => { (e.target as HTMLImageElement).src = 'https://picsum.photos/seed/goods/300/200' }"
                />
                <div class="goods-actions">
                  <NTooltip trigger="hover">
                    <template #trigger>
                      <button class="action-btn" @click="toggleFavorite(goods, $event)">
                        <NIcon :size="16"><Heart24Regular /></NIcon>
                      </button>
                    </template>
                    收藏
                  </NTooltip>
                </div>
                <NTag
                  v-if="goods.condition"
                  size="tiny"
                  class="goods-condition-tag"
                  :color="{ textColor: '#fff', borderColor: '#3B82F6', color: '#3B82F6' }"
                >
                  {{ goods.condition === 'new' ? '全新' : goods.condition === 'like_new' ? '几乎全新' : goods.condition === 'good' ? '良好' : '一般' }}
                </NTag>
              </div>
              <div class="goods-info">
                <h4 class="goods-title line-clamp-2">{{ goods.title }}</h4>
                <div class="goods-price-row">
                  <p v-if="goods.tradeType !== 'EXCHANGE'" class="goods-price">{{ formatPrice(goods.price) }}</p>
                  <p v-else class="goods-exchange">🔄 仅置换</p>
                </div>
                <div class="goods-meta">
                  <div class="goods-seller">
                    <img
                      :src="getAvatarUrl(goods.sellerAvatar || goods.userAvatar, 'thumb_64')"
                      class="seller-avatar"
                    />
                    <span class="seller-name">{{ goods.sellerNickname || goods.userNickname || '匿名' }}</span>
                  </div>
                  <span class="goods-views">{{ goods.viewCount || 0 }}次浏览</span>
                </div>
              </div>
            </div>
          </div>
          <NEmpty v-else-if="!loading" description="暂无推荐商品" />
        </NSpin>
      </div>

      <!-- Latest Goods -->
      <div class="goods-section">
        <div class="section-header">
          <h3 class="section-title">最新发布</h3>
          <NButton text type="primary" @click="router.push('/search?sortBy=latest')">查看更多 →</NButton>
        </div>

        <NSpin :show="loading && latestGoods.length === 0">
          <div v-if="latestGoods.length > 0" class="goods-grid">
            <div
              v-for="goods in latestGoods"
              :key="goods.id"
              class="goods-card"
              @click="goToGoods(goods.id)"
            >
              <div class="goods-image-wrapper">
                <img
                  :src="getImageUrl(goods.coverImage)"
                  :alt="goods.title"
                  class="goods-image"
                  loading="lazy"
                  @error="(e: Event) => { (e.target as HTMLImageElement).src = 'https://picsum.photos/seed/goods2/300/200' }"
                />
                <div class="goods-actions">
                  <NTooltip trigger="hover">
                    <template #trigger>
                      <button class="action-btn" @click="toggleFavorite(goods, $event)">
                        <NIcon :size="16"><Heart24Regular /></NIcon>
                      </button>
                    </template>
                    收藏
                  </NTooltip>
                </div>
                <NTag
                  v-if="goods.condition"
                  size="tiny"
                  class="goods-condition-tag"
                  :color="{ textColor: '#fff', borderColor: '#10B981', color: '#10B981' }"
                >
                  {{ goods.condition === 'new' ? '全新' : goods.condition === 'like_new' ? '几乎全新' : goods.condition === 'good' ? '良好' : '一般' }}
                </NTag>
              </div>
              <div class="goods-info">
                <h4 class="goods-title line-clamp-2">{{ goods.title }}</h4>
                <div class="goods-price-row">
                  <p v-if="goods.tradeType !== 'EXCHANGE'" class="goods-price">{{ formatPrice(goods.price) }}</p>
                  <p v-else class="goods-exchange">🔄 仅置换</p>
                </div>
                <div class="goods-meta">
                  <div class="goods-seller">
                    <img
                      :src="getAvatarUrl(goods.sellerAvatar || goods.userAvatar, 'thumb_64')"
                      class="seller-avatar"
                    />
                    <span class="seller-name">{{ goods.sellerNickname || goods.userNickname || '匿名' }}</span>
                  </div>
                  <span class="goods-views">{{ goods.viewCount || 0 }}次浏览</span>
                </div>
              </div>
            </div>
          </div>
          <NEmpty v-else-if="!loading" description="暂无最新商品" />
        </NSpin>
      </div>
    </div>

    <!-- 右侧边栏 -->
    <aside class="sidebar">
      <!-- 用户信息卡片 -->
      <div class="sidebar-card user-card">
        <template v-if="userStore.isLoggedIn">
          <div class="user-info">
            <img :src="getAvatarUrl(userStore.user?.avatar, 'original')" class="user-avatar" />
            <div class="user-details">
              <span class="user-name">{{ userStore.user?.nickname || '用户' }}</span>
              <span class="user-school">{{ userStore.user?.school || '校园用户' }}</span>
            </div>
          </div>
          <div class="user-stats">
            <div class="stat-item clickable" @click="router.push('/my-goods')">
              <span class="stat-value">{{ userStats.goodsCount }}</span>
              <span class="stat-label">发布</span>
            </div>
            <div class="stat-item clickable" @click="router.push('/following')">
              <span class="stat-value">{{ userStats.followingCount }}</span>
              <span class="stat-label">关注</span>
            </div>
          </div>
          <NButton type="primary" block @click="router.push('/publish')">
            <template #icon>
              <NIcon><Tag24Filled /></NIcon>
            </template>
            发布闲置
          </NButton>
        </template>
        <template v-else>
          <div class="login-prompt">
            <div class="login-icon">👋</div>
            <p class="login-text">登录后发布闲置物品</p>
            <NButton type="primary" block @click="router.push('/login')">
              立即登录
            </NButton>
          </div>
        </template>
      </div>

      <!-- 快捷入口 -->
      <div class="sidebar-card quick-links">
        <h4 class="card-title">
          <NIcon :size="16" class="text-[#10B981]"><Clock24Filled /></NIcon>
          快捷入口
        </h4>
        <div class="links-grid">
          <div class="link-item" @click="router.push('/my-orders')">
            <div class="link-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>
            </div>
            <span class="link-text">我的订单</span>
          </div>
          <div class="link-item" @click="router.push('/profile?tab=favorites')">
            <div class="link-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
            </div>
            <span class="link-text">我的收藏</span>
          </div>
          <div class="link-item" @click="router.push('/notifications')">
            <div class="link-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
            </div>
            <span class="link-text">消息通知</span>
          </div>
          <div class="link-item" @click="router.push('/chat')">
            <div class="link-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            </div>
            <span class="link-text">聊天消息</span>
          </div>
        </div>
      </div>

      <!-- 公告 -->
      <div class="sidebar-card announcement-card" v-if="announcements.length > 0">
        <h4 class="card-title">
          <NIcon :size="16" class="text-[#F59E0B]"><Tag24Filled /></NIcon>
          平台公告
        </h4>
        <div class="announcement-list">
          <div
            v-for="ann in announcements.slice(0, 3)"
            :key="ann.id"
            class="announcement-item"
            @click="showAnnouncementModal = true"
          >
            <span class="announcement-dot"></span>
            <span class="announcement-title line-clamp-1">{{ ann.title }}</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- Announcement Modal -->
    <NModal
      v-model:show="showAnnouncementModal"
      :closable="true"
      :mask-closable="true"
      preset="card"
      class="announcement-modal"
    >
      <template #header>
        <div class="modal-header">
          <span class="modal-icon">📢</span>
          <span class="modal-title">平台公告</span>
          <span v-if="announcements.length > 1" class="modal-count">
            {{ currentAnnouncementIndex + 1 }} / {{ announcements.length }}
          </span>
        </div>
      </template>

      <div v-if="currentAnnouncement" class="announcement-content">
        <h3 class="announcement-heading">{{ currentAnnouncement.title }}</h3>
        <p class="announcement-text">{{ currentAnnouncement.content }}</p>
        <p class="announcement-time">{{ formatDate(currentAnnouncement.createTime) }}</p>
      </div>

      <template #footer>
        <div class="modal-footer">
          <NCheckbox v-model:checked="hideAnnouncements">不再弹出</NCheckbox>
          <NButton type="primary" @click="handleNextAnnouncement">
            {{ hasMoreAnnouncements ? '下一条' : '我知道了' }}
          </NButton>
        </div>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.home-container {
  display: flex;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.main-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.sidebar {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 24px;
  align-self: flex-start;
  max-height: calc(100vh - 88px);
  overflow-y: auto;
}

.sidebar::-webkit-scrollbar {
  width: 4px;
}

.sidebar::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 2px;
}

.dark .sidebar::-webkit-scrollbar-thumb {
  background: #4b5563;
}

/* Banner */
.banner-wrapper {
  border-radius: 16px;
  overflow: hidden;
  position: relative;
}

.banner-carousel {
  height: 220px;
}

.banner-slide {
  height: 100%;
  position: relative;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(0,0,0,0.6) 0%, transparent 100%);
  display: flex;
  align-items: center;
  padding: 0 40px;
}

.banner-title {
  color: white;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.banner-subtitle {
  color: rgba(255,255,255,0.85);
  font-size: 16px;
}

.banner-dots {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
}

.banner-dot {
  width: 8px;
  height: 8px;
  border-radius: 4px;
  background: rgba(255,255,255,0.4);
  transition: all 0.3s ease;
}

.banner-dot.active {
  width: 24px;
  background: white;
}

/* Search Tags */
.search-tags-section {
  background: white;
  border-radius: 16px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.dark .search-tags-section {
  background: #1f2937;
  border: 1px solid #374151;
}

.tags-header {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.tags-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.dark .tags-label {
  color: #e5e7eb;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.search-tag {
  padding: 6px 14px;
  background: #f1f5f9;
  border-radius: 20px;
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.search-tag:hover {
  background: #3B82F6;
  color: white;
}

.dark .search-tag {
  background: #374151;
  color: #9ca3af;
}

.dark .search-tag:hover {
  background: #3B82F6;
  color: white;
}

/* Section Common */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.dark .section-title {
  color: #f3f4f6;
}

/* Category Section */
.category-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.dark .category-section {
  background: #1f2937;
  border: 1px solid #374151;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 8px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: transparent;
}

.category-card:hover {
  background: #f8fafc;
  transform: translateY(-2px);
}

.dark .category-card:hover {
  background: #374151;
}

.category-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  color: white;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  transition: all 0.3s ease;
}

.category-card:hover .category-icon {
  transform: scale(1.08) rotate(3deg);
  box-shadow: 0 6px 20px rgba(0,0,0,0.2);
}

.category-name {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  text-align: center;
}

.dark .category-name {
  color: #e5e7eb;
}

/* Goods Section */
.goods-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.dark .goods-section {
  background: #1f2937;
  border: 1px solid #374151;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.goods-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
  cursor: pointer;
}

.dark .goods-card {
  background: #1f2937;
  border: 1px solid #374151;
}

.goods-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.12);
}

.goods-image-wrapper {
  position: relative;
  aspect-ratio: 4/3;
}

.goods-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.goods-actions {
  position: absolute;
  top: 12px;
  right: 12px;
  opacity: 0;
  transition: all 0.3s ease;
}

.goods-card:hover .goods-actions {
  opacity: 1;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255,255,255,0.95);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  transition: all 0.2s ease;
  color: #64748b;
}

.action-btn:hover {
  background: #EF4444;
  color: white;
  transform: scale(1.1);
}

.goods-condition-tag {
  position: absolute;
  top: 12px;
  left: 12px;
}

.goods-info {
  padding: 14px 16px;
}

.goods-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.5;
  height: 42px;
  margin-bottom: 8px;
}

.dark .goods-title {
  color: #f3f4f6;
}

.goods-price-row {
  margin-bottom: 10px;
}

.goods-price {
  font-size: 18px;
  font-weight: 700;
  color: #3B82F6;
}

.goods-exchange {
  font-size: 14px;
  font-weight: 600;
  color: #10B981;
}

.goods-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
}

.dark .goods-meta {
  border-top-color: #374151;
}

.goods-seller {
  display: flex;
  align-items: center;
  gap: 6px;
}

.seller-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
}

.seller-name {
  font-size: 12px;
  color: #64748b;
}

.goods-views {
  font-size: 12px;
  color: #94a3b8;
}

/* Sidebar Cards */
.sidebar-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.dark .sidebar-card {
  background: #1f2937;
  border: 1px solid #374151;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.dark .card-title {
  color: #f3f4f6;
}

/* User Card */
.user-card .user-info {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
}

.user-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e0e7ff;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.dark .user-name {
  color: #f3f4f6;
}

.user-school {
  font-size: 13px;
  color: #64748b;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  padding: 14px 0;
  margin-bottom: 16px;
  border-top: 1px solid #f1f5f9;
  border-bottom: 1px solid #f1f5f9;
}

.dark .user-stats {
  border-color: #374151;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-item.clickable {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.stat-item.clickable:hover {
  background: #f1f5f9;
}

.stat-item.clickable:hover .stat-value {
  color: #3B82F6;
}

.dark .stat-item.clickable:hover {
  background: #374151;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.dark .stat-value {
  color: #f3f4f6;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
}

.login-prompt {
  text-align: center;
  padding: 20px 0;
}

.login-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.login-text {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 16px;
}

/* Stats Card */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat-block {
  text-align: center;
  padding: 12px 8px;
  background: #f8fafc;
  border-radius: 10px;
}

.dark .stat-block {
  background: #374151;
}

.stat-number {
  display: block;
  font-size: 16px;
  font-weight: 700;
  color: #3B82F6;
  margin-bottom: 4px;
}

.stat-desc {
  font-size: 12px;
  color: #64748b;
}

/* Quick Links */
.links-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.link-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.link-item:hover {
  background: #f8fafc;
  transform: translateY(-2px);
}

.dark .link-item:hover {
  background: #374151;
}

.link-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.link-text {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
}

.dark .link-text {
  color: #e5e7eb;
}

/* Announcement Card */
.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-item {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px 0;
}

.announcement-item:hover .announcement-title {
  color: #3B82F6;
}

.announcement-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #F59E0B;
  flex-shrink: 0;
}

.announcement-title {
  font-size: 13px;
  color: #374151;
  transition: color 0.2s ease;
}

.dark .announcement-title {
  color: #e5e7eb;
}

/* Modal */
.announcement-modal {
  width: 480px;
}

.modal-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.modal-icon {
  font-size: 18px;
}

.modal-title {
  font-weight: 700;
  font-size: 16px;
}

.modal-count {
  font-size: 13px;
  color: #94a3b8;
  margin-left: auto;
}

.announcement-content {
  padding: 8px 0;
}

.announcement-heading {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.announcement-text {
  font-size: 14px;
  color: #475569;
  line-height: 1.7;
  white-space: pre-wrap;
  margin-bottom: 12px;
}

.announcement-time {
  font-size: 12px;
  color: #94a3b8;
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* Utilities */
.line-clamp-1 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
}

.line-clamp-2 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

/* Responsive */
@media (max-width: 1200px) {
  .sidebar {
    width: 280px;
  }
  .goods-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 992px) {
  .home-container {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
    position: static;
    max-height: none;
  }
  .sidebar-card {
    flex: 1;
    min-width: 280px;
  }
  .goods-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .goods-grid {
    grid-template-columns: 1fr;
  }
  .category-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 480px) {
  .category-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
  }
  .category-card {
    padding: 12px 4px;
  }
  .category-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
  }
  .category-name {
    font-size: 11px;
  }
  .sidebar {
    flex-direction: column;
  }
  .sidebar-card {
    min-width: auto;
  }
}
</style>
