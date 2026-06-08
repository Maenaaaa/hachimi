<script setup lang="ts">
import { ref, onMounted, watch, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getConversationList, getMessages, createConversation, markAsRead } from '@/api/chat'
import { useUserStore } from '@/stores/user'
import { useWebSocket } from '@/composables/useWebSocket'
import { formatDate } from '@/utils'
import { upload } from '@/utils/request'
import {
  NButton, NInput, NAvatar, NBadge, NSpin, NEmpty, NIcon, NImage, NCard, useMessage,
} from 'naive-ui'
import { Send24Filled, Chat24Filled, Gift24Filled, Image24Filled } from '@vicons/fluent'
import { getImageUrl, formatPrice, getAvatarUrl } from '@/utils'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const message = useMessage()
const { subscribe, send, connect, connected } = useWebSocket()

const userAvatarUrl = computed(() => {
  return getAvatarUrl(userStore.user?.avatar, 'original')
})

const conversations = ref<any[]>([])
let currentSub: { unsubscribe: () => void } | null = null
const messages = ref<any[]>([])
const activeConvId = ref<number | null>(null)
const inputText = ref('')
const loadingConvs = ref(true)
const loadingMsgs = ref(false)
const msgContainer = ref<HTMLElement | null>(null)
const imgInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)

const activeConv = computed(() => conversations.value.find(c => c.id === activeConvId.value))

async function loadConversations() {
  try {
    const res = await getConversationList()
    conversations.value = res.data || []
  } catch { /* ignore */ } finally { loadingConvs.value = false }
}

async function loadMessages() {
  if (!activeConvId.value) return
  loadingMsgs.value = true
  try {
    const res = await getMessages(activeConvId.value)
    messages.value = res.data || []
    await nextTick()
    scrollToBottom()
    // 自动标记已读并刷新会话列表
    markAsRead(activeConvId.value).catch(() => {})
    loadConversations()
  } catch { /* ignore */ } finally { loadingMsgs.value = false }
}

function selectConv(id: number) {
  activeConvId.value = id
  router.replace({ path: '/chat', query: { convId: String(id) } })
  loadMessages()
  // Resubscribe to specific conversation topic
  if (currentSub) currentSub.unsubscribe()
  const sub = subscribe(`/topic/chat/${id}`, onWsMessage)
  if (sub) currentSub = { unsubscribe: () => sub.unsubscribe() }
}

async function startChat() {
  const goodsId = route.query.goodsId
  if (!goodsId) { message.warning('未指定商品'); return }
  try {
    const res = await createConversation(Number(goodsId))
    const conv = res.data
    activeConvId.value = conv.id
    await loadConversations()
    // auto-send goods card
    const card = JSON.stringify({
      type: 'card',
      goodsId: conv.goodsId,
      title: conv.goodsTitle,
      coverImage: conv.goodsCoverImage,
    })
    send('/app/chat', JSON.stringify({
      conversationId: conv.id,
      content: card,
      messageType: 'CARD',
    }))
    await loadMessages()
    router.replace({ query: {} })
  } catch { message.error('创建会话失败') }
}

function handleSend() {
  const text = inputText.value.trim()
  if (!text || !activeConvId.value) return
  _send(text, 'TEXT')
}

function handleSendCard() {
  if (!activeConv.value || !activeConvId.value) return
  const card = JSON.stringify({
    type: 'card',
    goodsId: activeConv.value.goodsId,
    title: activeConv.value.goodsTitle,
    coverImage: activeConv.value.goodsCoverImage,
  })
  _send(card, 'CARD')
}

function _send(content: string, messageType: string) {
  send('/app/chat', JSON.stringify({
    conversationId: activeConvId.value,
    content,
    messageType,
  }))
  inputText.value = ''
}

function isCard(msg: any): boolean { 
  const type = msg.messageType || msg.type
  return type === 'CARD' || type === 'card'
}
function isImage(msg: any): boolean { 
  const type = msg.messageType || msg.type
  return type === 'IMAGE' || type === 'image'
}
function parseCard(msg: any): any { try { return JSON.parse(msg.content) } catch { return null } }

function formatLastMessage(message: any): string {
  if (!message) return '暂无消息'
  const content = typeof message === 'string' ? message : (message?.content || '')
  if (!content) return '暂无消息'
  // 直接用正则匹配
  if (content.includes('"type":"card"') || content.includes('"type": "card"')) return '(商品卡片)'
  if (content.includes('"type":"image"') || content.includes('"type": "image"')) return '(图片)'
  // 如果是图片URL
  if (/https?:\/\//.test(content) && /\.(jpg|jpeg|png|gif|webp)/i.test(content)) return '(图片)'
  // 如果是图片URL（MinIO地址）
  if (content.includes('http://192.') || content.includes('http://minio')) return '(图片)'
  // 尝试解析 JSON 获取 title
  try {
    const parsed = JSON.parse(content)
    if (parsed.title) return parsed.title
    if (parsed.content) return parsed.content
  } catch {}
  // 截取前20个字符显示
  return content.length > 20 ? content.substring(0, 20) + '...' : content
}

function triggerImageUpload() { imgInput.value?.click() }

async function handleImageUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { message.warning('图片不能超过 5MB'); return }
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    fd.append('bucket', 'chat-images')
    const res = await upload<string>('/upload', fd)
    _send(res.data, 'IMAGE')
  } catch { message.error('图片上传失败') } finally {
    uploading.value = false
    if (imgInput.value) imgInput.value.value = ''
  }
}

function scrollToBottom() {
  if (msgContainer.value) {
    msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  }
}

// WebSocket message listener
function onWsMessage(body: string) {
  try {
    const msg = JSON.parse(body)
    if (msg.conversationId === activeConvId.value) {
      messages.value.push(msg)
      nextTick(() => scrollToBottom())
    }
    loadConversations()
  } catch { /* ignore */ }
}

onMounted(async () => {
  connect()
  await loadConversations()
  const convId = route.query.convId ? Number(route.query.convId) : null
  if (convId && conversations.value.some(c => c.id === convId)) {
    selectConv(convId)
  } else if (route.query.goodsId) {
    startChat()
  }
})

watch(connected, (val) => { if (val) loadConversations() })
</script>

<style scoped>
/* 聊天页面中图片预览功能菜单按钮间距 */
:deep(.n-image-preview-toolbar) {
  gap: 8px !important;
}
:deep(.n-image-preview-toolbar .n-button) {
  margin-left: 4px !important;
  margin-right: 4px !important;
}
</style>

<template>
  <div style="height: calc(100vh - 80px)" class="flex dark:bg-gray-800 bg-white rounded-xl overflow-hidden shadow-sm">
      <!-- Sidebar -->
      <div class="w-300px dark:border-gray-700 border-r border-gray-100 flex flex-col shrink-0">
        <div class="p-4 dark:border-gray-700 border-b border-gray-100 font-bold dark:text-gray-100 text-gray-800">消息</div>
        <NSpin :show="loadingConvs">
          <div class="flex-1 overflow-y-auto">
            <div v-if="conversations.length > 0">
              <div v-for="conv in conversations" :key="conv.id"
                class="flex items-center gap-3 p-4 cursor-pointer dark:hover:bg-gray-700 hover:bg-gray-50 transition-colors dark:border-gray-700 border-b border-gray-50"
                :class="{ 'dark:bg-blue-900/30 bg-blue-50': conv.id === activeConvId }"
                @click="selectConv(conv.id)">
                <NBadge :value="conv.unreadCount" :max="99" :show="conv.unreadCount > 0">
                  <img :src="getAvatarUrl(conv.otherUserAvatar, 'thumb_64')" class="w-11 h-11 rounded-full object-cover" />
                </NBadge>
                <div class="flex-1 min-w-0">
                  <div class="flex justify-between items-center">
                    <span class="font-semibold text-sm truncate">{{ conv.otherUserNickname }}</span>
                    <span class="text-xs text-gray-400 shrink-0">{{ formatDate(conv.lastMessageTime) }}</span>
                  </div>
                  <p class="text-xs text-gray-400 truncate mt-1">{{ formatLastMessage(conv.lastMessage) }}</p>
                </div>
              </div>
            </div>
            <NEmpty v-else description="暂无会话" class="mt-20" />
          </div>
        </NSpin>
      </div>

      <!-- Chat Area -->
      <div class="flex-1 flex flex-col">
        <template v-if="activeConv">
          <div class="p-4 dark:border-gray-700 border-b border-gray-100 flex items-center gap-3">
            <img :src="getAvatarUrl(activeConv.otherUserAvatar, 'thumb_64')" class="w-9 h-9 rounded-full object-cover" />
            <div class="flex-1 min-w-0">
              <div class="font-semibold text-sm">{{ activeConv.otherUserNickname }}</div>
              <div class="text-xs text-gray-400">{{ activeConv.goodsTitle }}</div>
            </div>
          </div>

          <!-- Goods Card -->
          <div v-if="activeConv.goodsId"
            class="px-4 py-3 dark:border-gray-700 border-b border-gray-100 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors flex items-center gap-3"
            @click="router.push(`/goods/${activeConv.goodsId}`)">
            <img
              v-if="activeConv.goodsCoverImage"
              :src="getImageUrl(activeConv.goodsCoverImage)"
              class="w-12 h-12 object-cover rounded-lg shrink-0"
            />
            <div class="flex-1 min-w-0">
              <div class="text-xs text-gray-400 mb-0.5">💬 关于以下商品的对话</div>
              <div class="flex items-center gap-2">
                <span class="text-sm font-semibold text-gray-800 truncate">{{ activeConv.goodsTitle }}</span>
                <span v-if="activeConv.goodsTradeType === 'EXCHANGE'" class="text-xs text-green-600 shrink-0">仅置换</span>
                <span v-else-if="activeConv.goodsPrice != null" class="text-sm text-[#3B82F6] font-bold shrink-0">{{ formatPrice(activeConv.goodsPrice) }}</span>
              </div>
            </div>
            <NIcon size="16" class="text-gray-400 shrink-0"><Gift24Filled /></NIcon>
          </div>

          <div ref="msgContainer" class="flex-1 overflow-y-auto p-4 dark:bg-gray-900 bg-gray-50">
            <NSpin :show="loadingMsgs">
              <div v-if="messages.length > 0" class="space-y-3">
                <div v-for="msg in messages" :key="msg.id"
                  class="flex gap-2" :class="msg.senderId === userStore.user?.id ? 'justify-end' : 'justify-start'">
                  <img v-if="msg.senderId !== userStore.user?.id"
                    :src="getAvatarUrl(msg.senderAvatar, 'thumb_64')" class="w-7 h-7 rounded-full object-cover shrink-0" />
                  <div style="max-width: 70%">
                    <!-- Card message -->
                    <div v-if="isCard(msg) && parseCard(msg)" class="rounded-2xl overflow-hidden shadow-sm"
                      :class="msg.senderId === userStore.user?.id ? 'rounded-br-md' : 'rounded-bl-md'"
                      style="width: 220px; cursor: pointer" @click="router.push('/goods/' + parseCard(msg).goodsId)">
                      <img :src="parseCard(msg).coverImage || ''" class="w-full h-32 object-cover" />
                      <div class="p-3 dark:bg-gray-700 bg-white">
                        <div class="text-xs text-gray-400">商品卡片</div>
                        <div class="text-sm font-semibold dark:text-gray-100 text-gray-800 mt-1 truncate">{{ parseCard(msg).title }}</div>
                        <div class="text-[#3B82F6] font-bold text-sm mt-1">查看详情 →</div>
                      </div>
                    </div>
                    <!-- Image message -->
                    <NImage v-else-if="isImage(msg)" :src="msg.content"
                      width="280" class="rounded-2xl shadow-sm cursor-pointer"
                      :class="msg.senderId === userStore.user?.id ? 'rounded-br-md' : 'rounded-bl-md'"
                      :preview-disabled="false"
                      fallback-src="" />
                    <!-- Text message -->
                    <div v-else class="px-3 py-2 rounded-2xl text-sm"
                      :class="msg.senderId === userStore.user?.id
                        ? 'bg-[#3B82F6] text-white rounded-br-md'
                        : 'dark:bg-gray-700 dark:text-gray-100 bg-white text-gray-800 rounded-bl-md shadow-sm'">
                      {{ msg.content }}
                    </div>
                    <div class="text-xs text-gray-400 mt-1 px-1">
                      {{ formatDate(msg.createTime) }}
                      <span v-if="msg.senderId === userStore.user?.id && msg.isRead" class="ml-1">已读</span>
                    </div>
                  </div>
                  <img v-if="msg.senderId === userStore.user?.id"
                    :src="getAvatarUrl(userStore.user?.avatar, 'original')" 
                    class="w-7 h-7 rounded-full object-cover shrink-0"
                  />
                </div>
              </div>
              <NEmpty v-else-if="!loadingMsgs" description="暂无消息，打个招呼吧" class="mt-20" />
            </NSpin>
          </div>

          <div class="p-4 dark:border-gray-700 border-t border-gray-100 flex gap-3 items-center">
            <input ref="imgInput" type="file" accept="image/*" class="hidden" @change="handleImageUpload" />
            <NButton v-if="activeConv" quaternary circle title="发送商品卡片" @click="handleSendCard">
              <template #icon><NIcon size="20"><Gift24Filled /></NIcon></template>
            </NButton>
            <NButton quaternary circle :loading="uploading" title="发送图片" @click="triggerImageUpload">
              <template #icon><NIcon size="20"><Image24Filled /></NIcon></template>
            </NButton>
            <NInput v-model:value="inputText" placeholder="输入消息..."
              class="flex-1" @keyup.enter="handleSend" />
            <NButton type="primary" circle @click="handleSend">
              <template #icon><NIcon><Send24Filled /></NIcon></template>
            </NButton>
          </div>
        </template>
        <div v-else class="flex-1 flex items-center justify-center">
          <div class="text-center dark:text-gray-500 text-gray-400">
            <NIcon size="64"><Chat24Filled /></NIcon>
            <p class="mt-4">选择左侧会话开始聊天</p>
          </div>
        </div>
      </div>
  </div>
</template>
