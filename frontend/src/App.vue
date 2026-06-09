<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useAppStore } from '@/stores/app'
import { NConfigProvider, NMessageProvider, NDialogProvider, darkTheme, zhCN, dateZhCN } from 'naive-ui'

const appStore = useAppStore()

const themeOverrides = computed(() => {
  const common = {
    primaryColor: '#3B82F6',
    primaryColorHover: '#2563EB',
    primaryColorPressed: '#1D4ED8',
    successColor: '#10B981',
    warningColor: '#F59E0B',
    errorColor: '#EF4444',
    borderRadius: '12px',
  }

  if (appStore.isDark) {
    return {
      common,
      Card: { color: '#1f2937', colorEmbedded: '#111827', borderColor: '#374151' },
      Tabs: { tabTextColorActiveLine: '#60a5fa' },
      Layout: { siderColor: '#1f2937' },
      Menu: { itemTextColor: '#d1d5db', itemTextColorActive: '#60a5fa', itemTextColorActiveHover: '#93c5fd' },
      Input: {
        border: '1px solid #374151',
        borderHover: '1px solid #4B5563',
        borderFocus: '1px solid #3B82F6',
        color: '#1f2937',
        colorFocus: 'rgba(31, 41, 55, 0.9)',
        placeholderColor: '#6B7280',
      },
      Button: { textColorPrimary: '#fff', colorPrimary: '#3B82F6', colorPrimaryHover: '#2563EB' },
      Table: { thColor: '#1a1f2c', tdColor: '#1f2937', tdColorStriped: '#1a1f2c', borderColor: '#374151' },
      Tag: { borderRadius: '8px' },
    }
  }

  return {
    common: { ...common, boxShadow1: '0 4px 20px rgba(0,0,0,.06)' },
  }
})

onMounted(() => {
  if (appStore.isDark) {
    document.documentElement.classList.add('dark')
  }
})
</script>

<template>
  <NConfigProvider
    :theme="appStore.isDark ? darkTheme : null"
    :theme-overrides="themeOverrides"
    :locale="zhCN"
    :date-locale="dateZhCN"
  >
    <NMessageProvider>
      <NDialogProvider>
        <router-view />
      </NDialogProvider>
    </NMessageProvider>
  </NConfigProvider>
</template>

<style>
@import './styles/global.css';
</style>
