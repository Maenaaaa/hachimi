<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { createGoods } from '@/api/goods'
import { getCategories } from '@/api/category'
import { upload } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import type { Category, GoodsPublishForm } from '@/types/entity'
import {
  NButton,
  NCard,
  NInput,
  NForm,
  NFormItem,
  NSelect,
  NUpload,
  NIcon,
  NSteps,
  NStep,
  NRadio,
  NRadioGroup,
  NSpace,
  NSpin,
  useMessage,
  type FormInst,
  type UploadFileInfo,
} from 'naive-ui'
import { Image24Filled } from '@vicons/fluent'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

const currentStep = ref(0)
const formRef = ref<FormInst | null>(null)
const submitting = ref(false)
const categories = ref<Category[]>([])
const priceInput = ref('')
const originalPriceInput = ref('')
const uploadedFiles = ref<UploadFileInfo[]>([])

const formData = ref<GoodsPublishForm>({
  title: '',
  description: '',
  price: 0,
  originalPrice: null,
  images: [],
  condition: 'good',
  categoryId: 0,
  campus: '主校区',
  tradeType: 'SELL',
})

// Sync form model for validation
const formModel = computed(() => ({
  title: formData.value.title,
  price: priceInput.value,
  categoryId: formData.value.categoryId,
  condition: formData.value.condition,
}))

const conditionOptions = [
  { label: '全新', value: 'new' },
  { label: '几乎全新', value: 'like_new' },
  { label: '良好', value: 'good' },
  { label: '一般', value: 'fair' },
]

const categoryOptions = computed(() =>
  categories.value.map((c) => ({ label: c.name, value: c.id })),
)

const rules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度2-50字', trigger: 'blur' },
  ],
  price: [
    {
      required: true, message: '请输入价格', trigger: 'blur',
      validator: (_rule: unknown, value: string) => {
        if (formData.value.tradeType === 'EXCHANGE') return true
        const n = Number(value)
        return !isNaN(n) && n > 0
      },
    },
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change', type: 'number' },
  ],
  condition: [{ required: true, message: '请选择成色', trigger: 'change' }],
}

onMounted(async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
    if (res.data.length > 0) formData.value.categoryId = res.data[0].id
  } catch {
    message.error('加载分类失败')
  }
})

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
    } catch {
      return
    }
  } else if (currentStep.value === 1) {
    if (formData.value.images.length === 0) {
      message.warning('请至少上传一张图片')
      return
    }
    currentStep.value = 2
  } else if (currentStep.value === 2) {
    if (!formData.value.description.trim()) {
      message.warning('请填写商品描述')
      return
    }
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
    if (formData.value.tradeType === 'SELL' && (!price || price <= 0)) {
      message.warning('请输入大于0的有效价格')
      submitting.value = false
      return
    }
    const res = await createGoods({
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
    message.success('发布成功，等待管理员审核')
    router.push(`/goods/${res.data.id}`)
  } catch (err: unknown) {
    const e = err as { message?: string }
    message.error(e.message || '发布失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="max-w-700px mx-auto pb-8">
    <NCard :bordered="true" style="border-radius: 12px">
      <h2 class="text-xl font-bold text-gray-800 mb-6">发布闲置物品</h2>

      <NSteps :current="currentStep + 1" class="mb-8">
        <NStep title="基本信息" description="标题、价格" />
        <NStep title="上传图片" description="商品图片" />
        <NStep title="商品描述" description="详细说明" />
        <NStep title="确认发布" description="预览并发布" />
      </NSteps>

      <!-- Step 1: Basic Info -->
      <div v-show="currentStep === 0">
        <NForm ref="formRef" :model="formModel" :rules="rules" label-placement="top">
          <NFormItem label="商品标题" path="title">
            <NInput
              v-model:value="formData.title"
              placeholder="给你的商品起个名字"
              :maxlength="50"
              show-count
            />
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
            <NFormItem label="价格 (元)" path="price">
              <NInput
                v-model:value="priceInput"
                placeholder="0.00"
              />
            </NFormItem>

            <NFormItem label="原价 (选填)">
              <NInput
                v-model:value="originalPriceInput"
                placeholder="选填"
              />
            </NFormItem>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <NFormItem label="分类" path="categoryId">
              <NSelect
                v-model:value="formData.categoryId"
                :options="categoryOptions"
                placeholder="选择分类"
              />
            </NFormItem>

            <NFormItem label="成色" path="condition">
              <NRadioGroup v-model:value="formData.condition">
                <NSpace>
                  <NRadio
                    v-for="opt in conditionOptions"
                    :key="opt.value"
                    :value="opt.value"
                  >
                    {{ opt.label }}
                  </NRadio>
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
          :default-file-list="uploadedFiles"
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
            <NInput
              v-model:value="formData.description"
              type="textarea"
              placeholder="详细描述商品的状况、使用时间、购买渠道等信息..."
              :rows="8"
              :maxlength="2000"
              show-count
            />
          </NFormItem>
        </NForm>
      </div>

      <!-- Step 4: Confirm -->
      <div v-show="currentStep === 3">
        <NCard :bordered="true" style="border-radius: 12px">
          <div class="space-y-4">
            <div v-if="formData.images.length > 0">
              <img
                :src="formData.images[0]"
                class="w-full h-48 object-cover rounded-lg mb-3"
                @error="(e: Event) => { (e.target as HTMLImageElement).style.display = 'none' }"
              />
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
            <div v-else class="text-lg text-green-600 font-bold">🔄 仅支持置换</div>
            <p class="text-sm text-gray-600 whitespace-pre-wrap">{{ formData.description }}</p>
          </div>
        </NCard>
      </div>

      <!-- Navigation Buttons -->
      <div class="flex justify-between mt-8">
        <NButton v-if="currentStep > 0" @click="prevStep">上一步</NButton>
        <div v-else />

        <NButton
          v-if="currentStep < 3"
          type="primary"
          @click="nextStep"
        >
          下一步
        </NButton>
        <NButton
          v-else
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          确认发布
        </NButton>
      </div>
    </NCard>
  </div>
</template>

<style scoped>
.max-w-700px {
  max-width: 700px;
}
</style>
