<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getGoodsDetail, updateGoods } from '@/api/goods'
import { getCategories } from '@/api/category'
import { upload } from '@/utils/request'
import { getImageUrl } from '@/utils'
import type { Category } from '@/types/entity'
import {
  NButton, NCard, NInput, NForm, NFormItem, NSelect,
  NUpload, NIcon, NSteps, NStep, NRadio, NRadioGroup,
  NSpace, NSpin, useMessage, type FormInst, type UploadFileInfo,
} from 'naive-ui'
import { Image24Filled } from '@vicons/fluent'

const router = useRouter()
const route = useRoute()
const message = useMessage()

const goodsId = Number(route.params.id)
const currentStep = ref(0)
const formRef = ref<FormInst | null>(null)
const submitting = ref(false)
const categories = ref<Category[]>([])
const priceInput = ref('')
const originalPriceInput = ref('')
const uploadedFiles = ref<UploadFileInfo[]>([])
const loading = ref(true)

const formData = ref({
  title: '',
  description: '',
  price: 0 as number | null,
  originalPrice: null as number | null,
  images: [] as string[],
  condition: 'MINOR_WEAR',
  categoryId: 0,
  campus: '主校区',
  tradeType: 'SELL',
})

const formModel = computed(() => ({
  title: formData.value.title,
  price: priceInput.value,
  categoryId: formData.value.categoryId,
  condition: formData.value.condition,
}))

const conditionOptions = [
  { label: '全新', value: 'BRAND_NEW' },
  { label: '几乎全新', value: 'LIKE_NEW' },
  { label: '轻微使用', value: 'MINOR_WEAR' },
  { label: '明显使用', value: 'VISIBLE_WEAR' },
  { label: '使用较重', value: 'HEAVILY_USED' },
]

const categoryOptions = computed(() =>
  categories.value.map((c) => ({ label: c.name, value: c.id })),
)

const rules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度2-50字', trigger: 'blur' },
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change', type: 'number' },
  ],
  condition: [{ required: true, message: '请选择成色', trigger: 'change' }],
}

async function loadGoods() {
  loading.value = true
  try {
    const [goodsRes, catRes] = await Promise.all([
      getGoodsDetail(goodsId),
      getCategories(),
    ])
    categories.value = catRes.data
    const g = goodsRes.data
    formData.value.title = g.title
    formData.value.description = g.description || ''
    formData.value.price = g.price
    formData.value.originalPrice = g.originalPrice
    formData.value.condition = g.condition
    formData.value.categoryId = g.categoryId
    formData.value.campus = g.campus || '主校区'
    formData.value.tradeType = g.tradeType || 'SELL'
    priceInput.value = g.price != null ? String(g.price) : ''
    originalPriceInput.value = g.originalPrice != null ? String(g.originalPrice) : ''

    if (g.images && g.images.length > 0) {
      formData.value.images = g.images
      uploadedFiles.value = g.images.map((url: string, i: number) => ({
        id: String(i),
        name: `image-${i}`,
        url,
        status: 'finished' as const,
      }))
    }
  } catch {
    message.error('加载商品信息失败')
    router.back()
  } finally {
    loading.value = false
  }
}

async function handleImageUpload({ file, onFinish, onError }: { file: UploadFileInfo; onFinish: () => void; onError: () => void }) {
  if (!file.file) return
  const fd = new FormData()
  fd.append('file', file.file)
  try {
    const res = await upload<string>('/upload', fd)
    formData.value.images.push(res.data)
    uploadedFiles.value.push({ ...file, url: res.data, status: 'finished' })
    onFinish()
  } catch {
    onError()
    message.error('图片上传失败')
  }
}

function handleImageRemove(options: { file: UploadFileInfo; fileList: UploadFileInfo[] }) {
  const idx = uploadedFiles.value.findIndex(f => f.id === options.file.id)
  if (idx >= 0) {
    uploadedFiles.value.splice(idx, 1)
    formData.value.images = uploadedFiles.value.filter(f => f.url).map(f => f.url || '')
  }
}

async function nextStep() {
  if (currentStep.value === 0) {
    try {
      await formRef.value?.validate()
      currentStep.value = 1
    } catch { return }
  } else if (currentStep.value === 1) {
    currentStep.value = 2
  } else if (currentStep.value === 2) {
    currentStep.value = 3
  }
}

function prevStep() {
  if (currentStep.value > 0) currentStep.value--
}

async function handleSubmit() {
  submitting.value = true
  try {
    const price = Number(priceInput.value)
    const originalPrice = originalPriceInput.value ? Number(originalPriceInput.value) : null
    await updateGoods(goodsId, {
      title: formData.value.title,
      description: formData.value.description,
      price,
      originalPrice,
      images: formData.value.images,
      condition: formData.value.condition,
      categoryId: formData.value.categoryId,
      campus: formData.value.campus,
      tradeType: formData.value.tradeType,
    })
    message.success('修改成功')
    router.push(`/goods/${goodsId}`)
  } catch (err: unknown) {
    const e = err as { message?: string }
    message.error(e.message || '修改失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadGoods)
</script>

<template>
  <div class="max-w-700px mx-auto pb-8">
    <NSpin :show="loading">
      <NCard :bordered="true" style="border-radius: 12px">
        <h2 class="text-xl font-bold text-gray-800 mb-6">编辑商品</h2>

        <NSteps :current="currentStep + 1" class="mb-8">
          <NStep title="基本信息" description="标题、价格" />
          <NStep title="上传图片" description="商品图片" />
          <NStep title="商品描述" description="详细说明" />
          <NStep title="确认修改" description="预览并提交" />
        </NSteps>

        <!-- Step 1: Basic Info -->
        <div v-show="currentStep === 0">
          <NForm ref="formRef" :model="formModel" :rules="rules" label-placement="top">
            <NFormItem label="商品标题" path="title">
              <NInput v-model:value="formData.title" placeholder="给你的商品起个名字" :maxlength="50" show-count />
            </NFormItem>
            <NFormItem label="交易方式">
              <NRadioGroup v-model:value="formData.tradeType">
                <NSpace>
                  <NRadio value="SELL">出售</NRadio>
                  <NRadio value="EXCHANGE">置换</NRadio>
                </NSpace>
              </NRadioGroup>
            </NFormItem>
            <div v-if="formData.tradeType === 'SELL'" class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <NFormItem label="价格 (元)">
                <NInput v-model:value="priceInput" placeholder="0.00" />
              </NFormItem>
              <NFormItem label="原价 (选填)">
                <NInput v-model:value="originalPriceInput" placeholder="选填" />
              </NFormItem>
            </div>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <NFormItem label="分类" path="categoryId">
                <NSelect v-model:value="formData.categoryId" :options="categoryOptions" placeholder="选择分类" />
              </NFormItem>
              <NFormItem label="成色" path="condition">
                <NRadioGroup v-model:value="formData.condition">
                  <NSpace>
                    <NRadio v-for="opt in conditionOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</NRadio>
                  </NSpace>
                </NRadioGroup>
              </NFormItem>
            </div>
          </NForm>
        </div>

        <!-- Step 2: Images -->
        <div v-show="currentStep === 1">
          <p class="text-sm text-gray-500 mb-4">上传商品图片，建议 1:1 比例，最多 9 张</p>
          <NUpload
            :multiple="false"
            :max="9"
            v-model:file-list="uploadedFiles"
            :custom-request="handleImageUpload"
            accept="image/*"
            list-type="image-card"
            @remove="handleImageRemove"
          >
            <NButton v-if="uploadedFiles.length < 9">
              <template #icon><NIcon><Image24Filled /></NIcon></template>
              上传图片
            </NButton>
          </NUpload>
        </div>

        <!-- Step 3: Description -->
        <div v-show="currentStep === 2">
          <NForm label-placement="top">
            <NFormItem label="所在校区">
              <NInput v-model:value="formData.campus" placeholder="如：主校区、东校区、西校区" />
            </NFormItem>
            <NFormItem label="商品描述">
              <NInput v-model:value="formData.description" type="textarea" placeholder="详细描述商品的状况、使用时间、购买渠道等信息..." :rows="8" :maxlength="2000" show-count />
            </NFormItem>
          </NForm>
        </div>

        <!-- Step 4: Confirm -->
        <div v-show="currentStep === 3">
          <NCard :bordered="true" style="border-radius: 12px">
            <div class="space-y-4">
              <div v-if="formData.images.length > 0">
                <img :src="getImageUrl(formData.images[0])" class="w-full h-48 object-cover rounded-lg mb-3" />
              </div>
              <h3 class="text-lg font-bold">{{ formData.title }}</h3>
              <div class="flex gap-2">
                <span class="text-xs px-2 py-1 rounded" :class="formData.tradeType === 'EXCHANGE' ? 'bg-green-100 text-green-600' : 'bg-blue-100 text-blue-600'">
                  {{ formData.tradeType === 'EXCHANGE' ? '置换' : '出售' }}
                </span>
                <span class="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded">{{ categoryOptions.find(c => c.value === formData.categoryId)?.label }}</span>
                <span class="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded">{{ conditionOptions.find(c => c.value === formData.condition)?.label }}</span>
              </div>
              <div v-if="formData.tradeType === 'SELL'" class="flex items-baseline gap-2">
                <span class="text-2xl font-bold text-[#3B82F6]">¥{{ priceInput || '0' }}</span>
                <span v-if="originalPriceInput" class="text-sm text-gray-400 line-through">¥{{ originalPriceInput }}</span>
              </div>
              <p class="text-sm text-gray-600 whitespace-pre-wrap">{{ formData.description }}</p>
            </div>
          </NCard>
        </div>

        <!-- Navigation Buttons -->
        <div class="flex justify-between mt-8">
          <NButton v-if="currentStep > 0" @click="prevStep">上一步</NButton>
          <div v-else />
          <NButton v-if="currentStep < 3" type="primary" @click="nextStep">下一步</NButton>
          <NButton v-else type="primary" :loading="submitting" @click="handleSubmit">确认修改</NButton>
        </div>
      </NCard>
    </NSpin>
  </div>
</template>

<style scoped>
.max-w-700px { max-width: 700px; }
</style>
