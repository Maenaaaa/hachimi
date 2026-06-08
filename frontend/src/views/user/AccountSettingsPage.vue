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
  useMessage,
  type FormInst,
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
const verifyForm = ref({ realName: '', idNumber: '' })

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

async function handleVerify() {
  if (!verifyForm.value.realName || !verifyForm.value.idNumber) {
    message.warning('请填写完整信息')
    return
  }
  try {
    const { verifyUser } = await import('@/api/user')
    await verifyUser(verifyForm.value)
    message.success('实名认证成功')
    showVerifyModal.value = false
    userStore.fetchProfile()
  } catch {
    message.error('认证失败')
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
    <NModal v-model:show="showVerifyModal" title="实名认证" preset="card" style="width: 400px; border-radius: 12px">
      <div class="space-y-4">
        <NInput v-model:value="verifyForm.realName" placeholder="真实姓名" />
        <NInput v-model:value="verifyForm.idNumber" placeholder="身份证号" />
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
