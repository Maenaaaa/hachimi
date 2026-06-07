<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import {
  NModal,
  NInput,
  NButton,
  NForm,
  NFormItem,
  NIcon,
  NSpace,
  NAlert,
  NTabs,
  NTabPane,
  useMessage,
} from 'naive-ui'
import { Person24Filled, LockClosed24Filled } from '@vicons/fluent'
import type { FormInst } from 'naive-ui'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()
const message = useMessage()

const activeTab = ref<'login' | 'register'>('login')
const loading = ref(false)
const errorMsg = ref('')

const loginForm = ref({ username: '', password: '' })
const loginFormRef = ref<FormInst | null>(null)
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const registerForm = ref({ username: '', password: '', confirmPassword: '', nickname: '' })
const registerFormRef = ref<FormInst | null>(null)
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名3-20个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (_: unknown, value: string) => value === registerForm.value.password, message: '两次密码不一致', trigger: 'blur' },
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
}

async function handleLogin() {
  try { await loginFormRef.value?.validate() } catch { return }
  loading.value = true
  errorMsg.value = ''
  try {
    await userStore.login({ username: loginForm.value.username, password: loginForm.value.password })
    message.success('登录成功')
    appStore.closeLoginModal()
    // Full reload to ensure MainLayout remounts with correct auth state
    const target = userStore.isAdmin ? '/admin' : (appStore.pendingRedirect || '/')
    window.location.href = target
  } catch (err: unknown) {
    errorMsg.value = (err as { message?: string }).message || '登录失败'
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  try { await registerFormRef.value?.validate() } catch { return }
  loading.value = true
  errorMsg.value = ''
  try {
    await userStore.register({
      username: registerForm.value.username,
      password: registerForm.value.password,
      confirmPassword: registerForm.value.confirmPassword,
      nickname: registerForm.value.nickname,
    })
    message.success('注册成功')
    appStore.closeLoginModal()
    window.location.href = '/'
  } catch (err: unknown) {
    errorMsg.value = (err as { message?: string }).message || '注册失败'
  } finally {
    loading.value = false
  }
}

function onClose() {
  if (!loading.value) {
    appStore.closeLoginModal()
  }
}

function switchTab(tab: 'login' | 'register') {
  activeTab.value = tab
  errorMsg.value = ''
}
</script>

<template>
  <NModal
    :show="appStore.showLoginModal"
    :mask-closable="!loading"
    :closable="!loading"
    @update:show="(val: boolean) => { if (!val) onClose() }"
  >
    <div class="w-400px bg-white rounded-2xl p-8" style="max-width: 90vw">
      <div class="text-center mb-6">
        <div class="w-14 h-14 bg-[#3B82F6] rounded-2xl flex items-center justify-center mx-auto mb-3">
          <span class="text-white text-xl font-bold">C</span>
        </div>
        <h2 class="text-xl font-bold text-gray-800">校园闲置交换</h2>
      </div>

      <NTabs v-model:value="activeTab" type="line" justify-content="center" @update:value="switchTab">
        <NTabPane name="login" tab="登录">
          <NAlert v-if="errorMsg" type="error" :show-icon="true" closable class="mb-4" @close="errorMsg = ''">
            {{ errorMsg }}
          </NAlert>

          <NForm ref="loginFormRef" :model="loginForm" :rules="loginRules" size="large">
            <NFormItem path="username">
              <NInput v-model:value="loginForm.username" placeholder="用户名" clearable>
                <template #prefix><NIcon><Person24Filled /></NIcon></template>
              </NInput>
            </NFormItem>
            <NFormItem path="password">
              <NInput v-model:value="loginForm.password" type="password" placeholder="密码" show-password-on="click">
                <template #prefix><NIcon><LockClosed24Filled /></NIcon></template>
              </NInput>
            </NFormItem>
            <NButton type="primary" block :loading="loading" @click="handleLogin">登录</NButton>
          </NForm>

          <div class="text-center text-sm mt-4 text-gray-400">
            还没有账号？<span class="text-[#3B82F6] cursor-pointer" @click="switchTab('register')">立即注册</span>
          </div>
        </NTabPane>

        <NTabPane name="register" tab="注册">
          <NAlert v-if="errorMsg" type="error" :show-icon="true" closable class="mb-4" @close="errorMsg = ''">
            {{ errorMsg }}
          </NAlert>

          <NForm ref="registerFormRef" :model="registerForm" :rules="registerRules" size="large">
            <NFormItem path="username">
              <NInput v-model:value="registerForm.username" placeholder="用户名" clearable />
            </NFormItem>
            <NFormItem path="nickname">
              <NInput v-model:value="registerForm.nickname" placeholder="昵称" clearable />
            </NFormItem>
            <NFormItem path="password">
              <NInput v-model:value="registerForm.password" type="password" placeholder="密码（至少6位）" show-password-on="click" />
            </NFormItem>
            <NFormItem path="confirmPassword">
              <NInput v-model:value="registerForm.confirmPassword" type="password" placeholder="确认密码" show-password-on="click" />
            </NFormItem>
            <NButton type="primary" block :loading="loading" @click="handleRegister">注册</NButton>
          </NForm>

          <div class="text-center text-sm mt-4 text-gray-400">
            已有账号？<span class="text-[#3B82F6] cursor-pointer" @click="switchTab('login')">去登录</span>
          </div>
        </NTabPane>
      </NTabs>
    </div>
  </NModal>
</template>
