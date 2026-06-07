<script setup lang="ts">
import { useRouter } from 'vue-router'
import { NResult, NButton } from 'naive-ui'

const router = useRouter()
</script>

<template>
  <div class="flex items-center justify-center min-h-[calc(100vh-64px-32px)]">
    <NResult status="404" title="页面不存在" description="你访问的页面走丢了">
      <template #footer>
        <NButton type="primary" @click="router.push('/')">返回首页</NButton>
      </template>
    </NResult>
  </div>
</template>
