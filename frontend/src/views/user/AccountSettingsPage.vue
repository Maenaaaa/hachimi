<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getAvatarUrl, updateAvatarCache } from '@/utils'
import {
  NCard,
  NInput,
  NButton,
  NForm,
  NFormItem,
  NModal,
  NTag,
  NUpload,
  NUploadDragger,
  NIcon,
  useMessage,
  type FormInst,
  type UploadFileInfo,
} from 'naive-ui'
import AvatarUpload from '@/components/AvatarUpload.vue'
import type { ProfileForm, PasswordForm } from '@/types/entity'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

const avatarUrl = computed(() => {
  return getAvatarUrl(userStore.user?.avatar, 'original')
})

const profileFormRef = ref<FormInst | null>(null)
const passwordFormRef = ref<FormInst | null>(null)
const saving = ref(false)
const showVerifyModal = ref(false)
const verifyForm = ref({ realName: '', studentId: '', authTitle: '' })
const verifyImage = ref<File | null>(null)

const profileForm = ref<ProfileForm>({
  nickname: '',
  email: '',
  phone: '',
  school: '',
})

const passwordForm = ref<PasswordForm>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const profileRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string) => value === passwordForm.value.newPassword,
      message: '两次密码不一致',
      trigger: 'blur',
    },
  ],
}

onMounted(() => {
  if (userStore.user) {
    profileForm.value = {
      nickname: userStore.user.nickname,
      email: userStore.user.email || '',
      phone: userStore.user.phone || '',
      school: (userStore.user as any).school || '',
    }
  }
})

function handleAvatarUploaded() {
  updateAvatarCache()
  userStore.fetchProfile()
}

async function saveProfile() {
  try {
    await profileFormRef.value?.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    await userStore.updateProfile(profileForm.value)
    message.success('资料更新成功')
  } catch (err: unknown) {
    const e = err as { message?: string }
    message.error(e.message || '更新失败')
  } finally {
    saving.value = false
  }
}

async function changePassword() {
  try {
    await passwordFormRef.value?.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    await userStore.changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
      confirmPassword: passwordForm.value.confirmPassword,
    })
    message.success('密码修改成功')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (err: unknown) {
    const e = err as { message?: string }
    message.error(e.message || '修改失败')
  } finally {
    saving.value = false
  }
}

function handleVerifyFileChange(options: { file: UploadFileInfo }) {
  const file = options.file.file
  if (file) {
    verifyImage.value = file
  }
}

async function handleVerify() {
  if (!verifyForm.value.realName || !verifyForm.value.studentId || !verifyForm.value.authTitle) {
    message.warning('请填写完整信息')
    return
  }
  if (!verifyImage.value) {
    message.warning('请上传学生证/身份证照片')
    return
  }
  try {
    const { verifyUser } = await import('@/api/user')
    const formData = new FormData()
    const dtoBlob = new Blob([JSON.stringify({
      realName: verifyForm.value.realName,
      studentId: verifyForm.value.studentId,
      authTitle: verifyForm.value.authTitle,
    })], { type: 'application/json' })
    formData.append('dto', dtoBlob)
    formData.append('image', verifyImage.value)
    await verifyUser(formData)
    message.success('认证申请已提交，请等待审核')
    showVerifyModal.value = false
    verifyForm.value = { realName: '', studentId: '', authTitle: '' }
    verifyImage.value = null
  } catch {
    message.error('认证提交失败')
  }
}
</script>

<template>
  <div class="max-w-700px mx-auto pb-8 space-y-6">
    <h2 class="text-xl font-bold text-gray-800">个人设置</h2>

    <!-- Avatar Section -->
    <NCard :bordered="true" style="border-radius: 12px">
      <div class="flex items-center gap-6">
        <AvatarUpload
          :current-avatar="avatarUrl"
          :size="72"
          @uploaded="handleAvatarUploaded"
        />
        <div>
          <div class="font-bold text-gray-800">{{ userStore.user?.nickname }}</div>
          <div class="text-sm text-gray-400">@{{ userStore.user?.username }}</div>
          <div class="mt-1">
            <NTag v-if="userStore.user?.verified" size="small" type="success">已实名认证</NTag>
            <NButton v-else size="tiny" @click="showVerifyModal = true">去认证</NButton>
          </div>
        </div>
      </div>
    </NCard>

    <!-- Edit Profile -->
    <NCard :bordered="true" style="border-radius: 12px">
      <h3 class="font-bold text-gray-800 mb-4">编辑资料</h3>
      <NForm ref="profileFormRef" :model="profileForm" :rules="profileRules" label-placement="top">
        <NFormItem label="昵称" path="nickname">
          <NInput v-model:value="profileForm.nickname" placeholder="你的昵称" />
        </NFormItem>
        <NFormItem label="邮箱">
          <NInput v-model:value="profileForm.email" placeholder="邮箱地址" />
        </NFormItem>
        <NFormItem label="手机号">
          <NInput v-model:value="profileForm.phone" placeholder="手机号" />
        </NFormItem>
        <NFormItem label="学校">
          <NInput v-model:value="profileForm.school" placeholder="学校名称" />
        </NFormItem>
        <NButton type="primary" :loading="saving" @click="saveProfile">保存</NButton>
      </NForm>
    </NCard>

    <!-- Change Password -->
    <NCard :bordered="true" style="border-radius: 12px">
      <h3 class="font-bold text-gray-800 mb-4">修改密码</h3>
      <NForm ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-placement="top">
        <NFormItem label="原密码" path="oldPassword">
          <NInput
            v-model:value="passwordForm.oldPassword"
            type="password"
            placeholder="输入原密码"
            show-password-on="click"
          />
        </NFormItem>
        <NFormItem label="新密码" path="newPassword">
          <NInput
            v-model:value="passwordForm.newPassword"
            type="password"
            placeholder="输入新密码（至少6位）"
            show-password-on="click"
          />
        </NFormItem>
        <NFormItem label="确认新密码" path="confirmPassword">
          <NInput
            v-model:value="passwordForm.confirmPassword"
            type="password"
            placeholder="再次输入新密码"
            show-password-on="click"
          />
        </NFormItem>
        <NButton type="primary" :loading="saving" @click="changePassword">修改密码</NButton>
      </NForm>
    </NCard>

    <!-- Verify Modal -->
    <NModal v-model:show="showVerifyModal" title="实名认证" preset="card" style="width: 480px; border-radius: 12px">
      <div class="space-y-4">
        <NInput v-model:value="verifyForm.realName" placeholder="真实姓名" />
        <NInput v-model:value="verifyForm.studentId" placeholder="学号" />
        <NInput v-model:value="verifyForm.authTitle" placeholder="认证称号（如：校园达人、优质卖家）" />
        <NUpload
          accept="image/*"
          :max="1"
          :default-upload="false"
          @change="handleVerifyFileChange"
        >
          <NUploadDragger>
            <div style="margin-bottom: 12px">
              <NIcon size="48" :depth="3">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
              </NIcon>
            </div>
            <div style="margin-bottom: 8px"><strong>点击或拖拽上传</strong></div>
            <div style="font-size: 12px; color: #999">请上传学生证或身份证照片（JPG/PNG，≤5MB）</div>
          </NUploadDragger>
        </NUpload>
        <NButton type="primary" block @click="handleVerify">提交认证</NButton>
      </div>
    </NModal>
  </div>
</template>

<style scoped>
.max-w-700px {
  max-width: 700px;
}
</style>
