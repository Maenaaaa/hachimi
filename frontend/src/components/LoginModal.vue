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
const passwordInputRef = ref<InstanceType<typeof import('naive-ui')['NInput']> | null>(null)
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const registerForm = ref({ username: '', password: '', confirmPassword: '', nickname: '' })
const registerFormRef = ref<FormInst | null>(null)
const registerNicknameRef = ref<InstanceType<typeof import('naive-ui')['NInput']> | null>(null)
const registerPasswordRef = ref<InstanceType<typeof import('naive-ui')['NInput']> | null>(null)
const registerConfirmRef = ref<InstanceType<typeof import('naive-ui')['NInput']> | null>(null)
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
    <div class="w-400px bg-white dark:bg-gray-800 rounded-2xl p-8" style="max-width: 90vw">
      <div class="text-center mb-6">
        <div class="logo-icon mx-auto mb-3">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M17 1l4 4-4 4" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M3 11V9a4 4 0 0 1 4-4h14" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M7 23l-4-4 4-4" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M21 13v2a4 4 0 0 1-4 4H3" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h2 class="text-xl font-bold text-gray-800 dark:text-gray-100">校园淘</h2>
      </div>

      <NTabs v-model:value="activeTab" type="line" justify-content="center" @update:value="switchTab">
        <NTabPane name="login" tab="登录">
          <NAlert v-if="errorMsg" type="error" :show-icon="true" closable class="mb-4" @close="errorMsg = ''">
            {{ errorMsg }}
          </NAlert>

          <NForm ref="loginFormRef" :model="loginForm" :rules="loginRules" size="large">
            <NFormItem path="username">
              <NInput v-model:value="loginForm.username" placeholder="用户名" clearable
                @keyup.enter="() => { passwordInputRef?.focus() }">
                <template #prefix><NIcon><Person24Filled /></NIcon></template>
              </NInput>
            </NFormItem>
            <NFormItem path="password">
              <NInput ref="passwordInputRef" v-model:value="loginForm.password" type="password" placeholder="密码" show-password-on="click"
                @keyup.enter="handleLogin">
                <template #prefix><NIcon><LockClosed24Filled /></NIcon></template>
              </NInput>
            </NFormItem>
            <NButton type="primary" block :loading="loading" @click="handleLogin">登录</NButton>
          </NForm>

          <div class="text-center text-sm mt-4 text-gray-400 dark:text-gray-500">
            还没有账号？<span class="text-[#3B82F6] cursor-pointer" @click="switchTab('register')">立即注册</span>
          </div>
        </NTabPane>

        <NTabPane name="register" tab="注册">
          <NAlert v-if="errorMsg" type="error" :show-icon="true" closable class="mb-4" @close="errorMsg = ''">
            {{ errorMsg }}
          </NAlert>

          <NForm ref="registerFormRef" :model="registerForm" :rules="registerRules" size="large">
            <NFormItem path="username">
              <NInput v-model:value="registerForm.username" placeholder="用户名" clearable
                @keyup.enter="() => { registerNicknameRef?.focus() }" />
            </NFormItem>
            <NFormItem path="nickname">
              <NInput ref="registerNicknameRef" v-model:value="registerForm.nickname" placeholder="昵称" clearable
                @keyup.enter="() => { registerPasswordRef?.focus() }" />
            </NFormItem>
            <NFormItem path="password">
              <NInput ref="registerPasswordRef" v-model:value="registerForm.password" type="password" placeholder="密码（至少6位）" show-password-on="click"
                @keyup.enter="() => { registerConfirmRef?.focus() }" />
            </NFormItem>
            <NFormItem path="confirmPassword">
              <NInput ref="registerConfirmRef" v-model:value="registerForm.confirmPassword" type="password" placeholder="确认密码" show-password-on="click"
                @keyup.enter="handleRegister" />
            </NFormItem>
            <NButton type="primary" block :loading="loading" @click="handleRegister">注册</NButton>
          </NForm>

          <div class="text-center text-sm mt-4 text-gray-400 dark:text-gray-500">
            已有账号？<span class="text-[#3B82F6] cursor-pointer" @click="switchTab('login')">去登录</span>
          </div>
        </NTabPane>
      </NTabs>
    </div>
  </NModal>
</template>

<style scoped>
.logo-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}
</style>
