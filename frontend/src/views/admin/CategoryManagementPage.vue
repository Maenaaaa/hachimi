<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { getAdminCategories, createCategory, updateCategory, deleteCategory } from '@/api/admin'
import { CATEGORY_ICONS, CATEGORY_COLORS } from '@/constants/category'
import {
  NCard, NButton, NDataTable, NSpace, NModal, NInput, NInputNumber,
  useMessage, useDialog, type DataTableColumn,
} from 'naive-ui'

const message = useMessage()
const dialog = useDialog()

const categoryList = ref<any[]>([])
const loading = ref(true)

const showModal = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const form = ref({ name: '', icon: '', sortOrder: 0 })

const iconOptions = Object.keys(CATEGORY_ICONS)

function getIconHtml(icon: string) {
  return CATEGORY_ICONS[icon] || CATEGORY_ICONS['其他']
}

function getIconColor(icon: string) {
  return CATEGORY_COLORS[icon]?.bg || CATEGORY_COLORS['其他'].bg
}

async function loadCategories() {
  loading.value = true
  try {
    const res = await getAdminCategories()
    categoryList.value = res.data || []
  } catch { message.error('加载分类失败') } finally { loading.value = false }
}

function openAdd() {
  isEdit.value = false
  editId.value = null
  form.value = { name: '', icon: '', sortOrder: categoryList.value.length }
  showModal.value = true
}

function openEdit(row: any) {
  isEdit.value = true
  editId.value = row.id
  form.value = { name: row.name, icon: row.icon || '', sortOrder: row.sortOrder || 0 }
  showModal.value = true
}

function selectIcon(icon: string) {
  form.value.icon = icon
  if (!form.value.name) {
    form.value.name = icon
  }
}

async function handleSubmit() {
  if (!form.value.name.trim()) { message.warning('请输入分类名称'); return }
  try {
    if (isEdit.value && editId.value) {
      await updateCategory(editId.value, form.value)
      message.success('修改成功')
    } else {
      await createCategory(form.value)
      message.success('添加成功')
    }
    showModal.value = false
    loadCategories()
  } catch (err: any) { message.error(err.message || '操作失败') }
}

function handleDelete(row: any) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除分类「${row.name}」吗？`,
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteCategory(row.id)
        message.success('已删除')
        loadCategories()
      } catch { message.error('删除失败') }
    },
  })
}

const columns: DataTableColumn<any>[] = [
  { title: 'ID', key: 'id', width: 60 },
  {
    title: '图标', key: 'icon', width: 60,
    render(row) {
      return h('div', {
        innerHTML: getIconHtml(row.icon),
        style: {
          width: '36px', height: '36px', borderRadius: '10px',
          background: getIconColor(row.icon),
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: '#fff',
        },
      })
    },
  },
  { title: '分类名称', key: 'name', width: 150 },
  { title: '排序', key: 'sortOrder', width: 80 },
  {
    title: '操作', key: 'actions', width: 140,
    render(row) {
      return h(NSpace, { size: 4 }, {
        default: () => [
          h(NButton, { size: 'tiny', onClick: () => openEdit(row) }, { default: () => '编辑' }),
          h(NButton, { size: 'tiny', type: 'error' as const, onClick: () => handleDelete(row) }, { default: () => '删除' }),
        ],
      })
    },
  },
]

onMounted(loadCategories)
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-xl font-bold text-gray-800">分类管理</h2>
      <NButton type="primary" @click="openAdd">添加分类</NButton>
    </div>

    <NCard :bordered="true" style="border-radius: 12px">
      <NDataTable
        :columns="columns"
        :data="categoryList"
        :row-key="(row: any) => row.id"
        :bordered="false"
        :single-line="true"
        size="small"
      />
    </NCard>

    <NModal v-model:show="showModal" :title="isEdit ? '编辑分类' : '添加分类'" preset="card" style="width: 500px; border-radius: 12px">
      <div class="space-y-4">
        <div>
          <div class="text-sm font-medium mb-2">选择图标</div>
          <div class="icon-grid">
            <div
              v-for="icon in iconOptions"
              :key="icon"
              class="icon-item"
              :class="{ active: form.icon === icon }"
              @click="selectIcon(icon)"
            >
              <div
                class="icon-preview"
                :style="{ background: getIconColor(icon) }"
                v-html="getIconHtml(icon)"
              />
              <span class="icon-label">{{ icon }}</span>
            </div>
          </div>
        </div>
        <div>
          <div class="text-sm font-medium mb-1">分类名称</div>
          <NInput v-model:value="form.name" placeholder="输入分类名称" />
        </div>
        <div>
          <div class="text-sm font-medium mb-1">排序序号</div>
          <NInputNumber v-model:value="form.sortOrder" :min="0" style="width: 100%" />
        </div>
        <NButton type="primary" block @click="handleSubmit">{{ isEdit ? '保存' : '添加' }}</NButton>
      </div>
    </NModal>
  </div>
</template>

<style scoped>
.icon-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 4px;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.icon-item:hover {
  background: #f3f4f6;
}

.icon-item.active {
  border-color: #3B82F6;
  background: #eff6ff;
}

.icon-preview {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.icon-label {
  font-size: 11px;
  color: #6b7280;
  text-align: center;
}
</style>
