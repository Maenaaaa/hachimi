<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  NCard,
  NInput,
  NButton,
  NForm,
  NFormItem,
  NIcon,
  NSpace,
  NAlert,
  NDivider,
  useMessage,
} from 'naive-ui'
import { Person24Filled, LockClosed24Filled, Edit24Filled } from '@vicons/fluent'
import type { FormInst } from 'naive-ui'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const errorMsg = ref('')

const formData = ref({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string) => value === formData.value.password,
      message: '两次密码不一致',
      trigger: 'blur',
    },
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 20, message: '昵称长度1-20位', trigger: 'blur' },
  ],
}

async function handleRegister() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    await userStore.register({
      username: formData.value.username,
      password: formData.value.password,
      confirmPassword: formData.value.confirmPassword,
      nickname: formData.value.nickname,
    })
    message.success('注册成功，请登录')
    router.push('/login')
  } catch (err: unknown) {
    const e = err as { message?: string }
    errorMsg.value = e.message || '注册失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-[calc(100vh-64px-32px)] flex items-center justify-center p-4">
    <NCard class="w-full max-w-420px" :bordered="true" style="border-radius: 12px">
      <div class="text-center mb-6">
        <div class="w-16 h-16 bg-[#10B981] rounded-2xl flex items-center justify-center mx-auto mb-4">
          <span class="text-white text-2xl font-bold">C</span>
        </div>
        <h2 class="text-2xl font-bold text-gray-800">创建账号</h2>
        <p class="text-gray-400 text-sm mt-1">加入校园淘社区</p>
      </div>

      <NAlert v-if="errorMsg" type="error" :show-icon="true" closable class="mb-4" @close="errorMsg = ''">
        {{ errorMsg }}
      </NAlert>

      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="top" size="large">
        <NFormItem label="用户名" path="username">
          <NInput v-model:value="formData.username" placeholder="请输入用户名" clearable>
            <template #prefix><NIcon><Person24Filled /></NIcon></template>
          </NInput>
        </NFormItem>

        <NFormItem label="昵称" path="nickname">
          <NInput v-model:value="formData.nickname" placeholder="请输入昵称" clearable>
            <template #prefix><NIcon><Edit24Filled /></NIcon></template>
          </NInput>
        </NFormItem>

        <NFormItem label="密码" path="password">
          <NInput
            v-model:value="formData.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            show-password-on="click"
          >
            <template #prefix><NIcon><LockClosed24Filled /></NIcon></template>
          </NInput>
        </NFormItem>

        <NFormItem label="确认密码" path="confirmPassword">
          <NInput
            v-model:value="formData.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password-on="click"
          >
            <template #prefix><NIcon><LockClosed24Filled /></NIcon></template>
          </NInput>
        </NFormItem>

        <NSpace vertical :size="12" class="w-full">
          <NButton type="primary" block :loading="loading" @click="handleRegister">
            注册
          </NButton>
        </NSpace>
      </NForm>

      <NDivider />
      <div class="text-center text-sm">
        <span class="text-gray-400">已有账号？</span>
        <router-link to="/login" class="text-[#3B82F6] hover:underline">立即登录</router-link>
      </div>
    </NCard>
  </div>
</template>

<style scoped>
.max-w-420px {
  max-width: 420px;
}
</style>
