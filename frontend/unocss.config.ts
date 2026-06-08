import { defineConfig, presetUno, presetAttributify } from 'unocss'
import transformerDirectives from '@unocss/transformer-directives'

export default defineConfig({
  presets: [presetUno(), presetAttributify()],
  transformers: [transformerDirectives()],
  darkMode: 'class',
  shortcuts: {
    'btn-primary': 'bg-[#3B82F6] text-white px-4 py-2 rounded-lg hover:bg-[#2563EB] transition-colors',
    'card-default': 'dark:bg-gray-800 bg-white rounded-xl shadow-sm dark:border-gray-700 border border-gray-100',
    'text-primary': 'text-[#3B82F6]',
    'text-secondary': 'text-gray-500 dark:text-gray-400',
    'page-bg': 'dark:bg-[#0f1117] bg-[#F8FAFC]',
    'layout-header': 'dark:bg-gray-900 dark:border-gray-800 bg-white border-b',
    'container-main': 'max-w-[1400px] mx-auto px-6',
    'section-spacing': 'mb-10',
    'section-title': 'text-lg font-bold text-gray-800 dark:text-gray-100',
  },
  rules: [
    ['max-w-1200px', { 'max-width': '1200px' }],
    ['max-w-1400px', { 'max-width': '1400px' }],
    ['max-w-500px', { 'max-width': '500px' }],
    ['max-w-700px', { 'max-width': '700px' }],
    ['max-w-420px', { 'max-width': '420px' }],
    ['header-shadow', { 'box-shadow': '0 2px 8px rgba(0,0,0,0.06)' }],
    ['card-shadow', { 'box-shadow': '0 2px 12px rgba(0,0,0,0.08)' }],
    ['line-clamp-2', {
      'overflow': 'hidden',
      'display': '-webkit-box',
      '-webkit-box-orient': 'vertical',
      '-webkit-line-clamp': '2',
    }],
  ],
  theme: {
    colors: {
      primary: '#3B82F6',
      success: '#10B981',
      warning: '#F59E0B',
      danger: '#EF4444',
      bg: '#F8FAFC',
    },
  },
})
