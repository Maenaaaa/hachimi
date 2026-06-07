<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
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
import { Person24Filled, LockClosed24Filled } from '@vicons/fluent'
import type { FormInst } from 'naive-ui'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const message = useMessage()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const errorMsg = ref('')

const formData = ref({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
}

async function handleLogin() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    await userStore.login({
      username: formData.value.username,
      password: formData.value.password,
    })
    message.success('登录成功')
    if (userStore.isAdmin) {
      router.push('/admin')
    } else {
      const redirect = (route.query.redirect as string) || '/'
      router.push(redirect)
    }
  } catch (err: unknown) {
    const e = err as { message?: string }
    errorMsg.value = e.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-[calc(100vh-64px-32px)] flex items-center justify-center p-4">
    <NCard class="w-full max-w-420px" :bordered="true" style="border-radius: 12px">
      <div class="text-center mb-6">
        <div class="w-16 h-16 bg-[#3B82F6] rounded-2xl flex items-center justify-center mx-auto mb-4">
          <span class="text-white text-2xl font-bold">C</span>
        </div>
        <h2 class="text-2xl font-bold text-gray-800">欢迎回来</h2>
        <p class="text-gray-400 text-sm mt-1">登录你的校园闲置交换账号</p>
      </div>

      <NAlert v-if="errorMsg" type="error" :show-icon="true" closable class="mb-4" @close="errorMsg = ''">
        {{ errorMsg }}
      </NAlert>

      <NForm ref="formRef" :model="formData" :rules="rules" label-placement="top" size="large">
        <NFormItem label="用户名" path="username">
          <NInput
            v-model:value="formData.username"
            placeholder="请输入用户名"
            clearable
          >
            <template #prefix>
              <NIcon><Person24Filled /></NIcon>
            </template>
          </NInput>
        </NFormItem>

        <NFormItem label="密码" path="password">
          <NInput
            v-model:value="formData.password"
            type="password"
            placeholder="请输入密码"
            show-password-on="click"
          >
            <template #prefix>
              <NIcon><LockClosed24Filled /></NIcon>
            </template>
          </NInput>
        </NFormItem>

        <NSpace vertical :size="12" class="w-full">
          <NButton type="primary" block :loading="loading" @click="handleLogin">
            登录
          </NButton>
        </NSpace>
      </NForm>

      <NDivider />
      <div class="text-center text-sm">
        <span class="text-gray-400">还没有账号？</span>
        <router-link to="/register" class="text-[#3B82F6] hover:underline">立即注册</router-link>
      </div>
    </NCard>
  </div>
</template>

<style scoped>
.max-w-420px {
  max-width: 420px;
}
</style>
