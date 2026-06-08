<script setup lang="ts">
import { ref, computed } from 'vue'
import { Cropper } from 'vue-advanced-cropper'
import 'vue-advanced-cropper/dist/style.css'
import { NButton, NIcon, NProgress, NModal, useMessage } from 'naive-ui'
import { Camera24Filled } from '@vicons/fluent'
import { TOKEN_KEY } from '@/constants'

interface Props {
  currentAvatar?: string
  size?: number
}

const props = withDefaults(defineProps<Props>(), {
  currentAvatar: '',
  size: 72,
})

const emit = defineEmits<{
  uploaded: [url: string]
}>()

const message = useMessage()
const showModal = ref(false)
const imageSrc = ref('')
const cropperRef = ref<InstanceType<typeof Cropper> | null>(null)
const uploading = ref(false)
const progress = ref(0)
const fileInputRef = ref<HTMLInputElement | null>(null)

const avatarStyle = computed(() => ({
  width: `${props.size}px`,
  height: `${props.size}px`,
}))

function triggerFileInput() {
  fileInputRef.value?.click()
}

function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    message.error('仅支持图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    message.error('图片大小不能超过 5MB')
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    imageSrc.value = e.target?.result as string
    showModal.value = true
  }
  reader.readAsDataURL(file)
  target.value = ''
}

function handleDrop(event: DragEvent) {
  event.preventDefault()
  const file = event.dataTransfer?.files[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    message.error('仅支持图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    message.error('图片大小不能超过 5MB')
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    imageSrc.value = e.target?.result as string
    showModal.value = true
  }
  reader.readAsDataURL(file)
}

function handleDragOver(event: DragEvent) {
  event.preventDefault()
}

async function handleUpload() {
  if (!cropperRef.value) return

  const { canvas } = cropperRef.value.getResult()
  if (!canvas) return

  uploading.value = true
  progress.value = 0

  try {
    canvas.toBlob(async (blob: Blob | null) => {
      if (!blob) {
        message.error('图片处理失败')
        uploading.value = false
        return
      }

      // 确保是 PNG 格式
      const pngBlob = blob.type === 'image/png' ? blob : new Blob([blob], { type: 'image/png' })
      
      const formData = new FormData()
      formData.append('file', pngBlob, 'avatar.png')

      const xhr = new XMLHttpRequest()
      xhr.open('POST', '/api/user/avatar')

      // 添加 Authorization header
      const token = localStorage.getItem(TOKEN_KEY)
      if (token) {
        xhr.setRequestHeader('Authorization', `Bearer ${token}`)
      }

      xhr.upload.onprogress = (e) => {
        if (e.lengthComputable) {
          progress.value = Math.round((e.loaded / e.total) * 100)
        }
      }

      xhr.onload = () => {
        if (xhr.status === 200) {
          const response = JSON.parse(xhr.responseText)
          if (response.code === 0 || response.code === 200) {
            message.success('头像更新成功')
            emit('uploaded', response.data)
            showModal.value = false
          } else {
            message.error(response.message || '上传失败')
          }
        } else {
          message.error('上传失败')
        }
        uploading.value = false
      }

      xhr.onerror = () => {
        message.error('网络错误')
        uploading.value = false
      }

      xhr.send(formData)
    }, 'image/png')
  } catch {
    message.error('上传失败')
    uploading.value = false
  }
}

function handleClose() {
  showModal.value = false
  imageSrc.value = ''
}
</script>

<template>
  <div class="avatar-upload-container">
    <div
      class="avatar-wrapper"
      :style="avatarStyle"
      @drop="handleDrop"
      @dragover="handleDragOver"
    >
      <img
        v-if="currentAvatar"
        :src="currentAvatar"
        class="avatar-image"
        :style="avatarStyle"
      />
      <div v-else class="avatar-placeholder" :style="avatarStyle">
        <NIcon :size="size * 0.4"><Camera24Filled /></NIcon>
      </div>
      <div
        class="avatar-overlay"
        :style="avatarStyle"
        @click="triggerFileInput"
      >
        <NIcon color="white" :size="20"><Camera24Filled /></NIcon>
      </div>
    </div>

    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleFileSelect"
    />

    <NModal
      v-model:show="showModal"
      title="裁剪头像"
      preset="card"
      style="width: 400px; border-radius: 12px"
      :closable="!uploading"
      :mask-closable="!uploading"
    >
      <div class="cropper-container">
        <Cropper
          ref="cropperRef"
          :src="imageSrc"
          :stencil-props="{ aspectRatio: 1 }"
          class="cropper"
        />
      </div>

      <div v-if="uploading" class="progress-container">
        <NProgress
          type="line"
          :percentage="progress"
          :show-indicator="true"
          status="success"
        />
      </div>

      <template #footer>
        <div class="modal-footer">
          <NButton @click="handleClose" :disabled="uploading">取消</NButton>
          <NButton type="primary" @click="handleUpload" :loading="uploading">
            {{ uploading ? '上传中...' : '确认上传' }}
          </NButton>
        </div>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.avatar-upload-container {
  display: inline-block;
}

.avatar-wrapper {
  position: relative;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e0e0e0;
  color: #999;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.cropper-container {
  width: 100%;
  height: 300px;
  margin-bottom: 16px;
}

.cropper {
  width: 100%;
  height: 100%;
}

.progress-container {
  margin-top: 16px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
