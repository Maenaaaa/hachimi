<script setup lang="ts">
import { ref, h, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NButton, NSelect, NTag, NSpin, NDataTable, NSpace, NModal, NInput, useMessage, NPagination, NImage, NRadioGroup, NRadioButton } from 'naive-ui'
import { getAdminAiJudgments, handleAiJudgmentManually, handleAiAppeal } from '@/api/ai-judgment'
import { getAdminGoods, takeDownGoods, approveGoods, getReportDetail, getAdminDisputes, handleDispute } from '@/api/admin'
import { getImageUrl, formatPrice } from '@/utils'
import type { AiJudgmentRecord, Goods } from '@/types/entity'

const router = useRouter()
const message = useMessage()

const judgments = ref<AiJudgmentRecord[]>([])
const loading = ref(true)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const filters = ref<{ sourceType: string; status: string }>({ sourceType: '', status: '' })

const showDetailModal = ref(false)
const showHandleModal = ref(false)
const showHandleAppealModal = ref(false)
const currentJudgment = ref<AiJudgmentRecord | null>(null)
const currentGoods = ref<Goods | null>(null)
const currentDispute = ref<any>(null)
const handleNote = ref('')
const handleAction = ref<'APPROVED' | 'REJECTED' | null>(null)
const appealHandleNote = ref('')
const appealOverride = ref<boolean | null>(null)

const sourceTypeOptions = [
  { label: '全部', value: '' },
  { label: '举报', value: 'REPORT' },
  { label: '争议', value: 'DISPUTE' },
  { label: '商品审核', value: 'GOODS_REVIEW' },
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '审核中', value: 'PROCESSING' },
  { label: '待处理', value: 'PENDING' },
  { label: '已处理', value: 'PROCESSED' },
  { label: '已上诉', value: 'APPEALED' },
  { label: '已推翻', value: 'OVERRIDDEN' },
  { label: '终审驳回', value: 'FINAL' },
  { label: '转人工', value: 'ESCALATED' },
]

const sourceTypeMap: Record<string, string> = {
  REPORT: '举报',
  DISPUTE: '争议',
  GOODS_REVIEW: '商品审核',
}

const verdictMap: Record<string, string> = {
  PENDING: '审核中',
  APPROVED: '通过',
  REJECTED: '拒绝',
  ESCALATED: '转人工',
}

const statusMap: Record<string, string> = {
  PROCESSING: '审核中',
  PENDING: '待处理',
  PROCESSED: '已处理',
  APPEALED: '已上诉',
  OVERRIDDEN: '已推翻',
  FINAL: '终审驳回',
  ESCALATED: '转人工',
}

const sourceTypeTagType: Record<string, 'success' | 'warning' | 'error' | 'info'> = {
  REPORT: 'error',
  DISPUTE: 'warning',
  GOODS_REVIEW: 'success',
}

const verdictTagType: Record<string, 'success' | 'error' | 'warning' | 'info'> = {
  PENDING: 'info',
  APPROVED: 'success',
  REJECTED: 'error',
  ESCALATED: 'warning',
}

const statusTagType: Record<string, 'success' | 'error' | 'warning' | 'info'> = {
  PROCESSING: 'info',
  PENDING: 'info',
  PROCESSED: 'success',
  APPEALED: 'warning',
  OVERRIDDEN: 'error',
  FINAL: 'error',
  ESCALATED: 'warning',
}

const isGoodsRelated = computed(() => {
  return currentJudgment.value?.sourceType === 'GOODS_REVIEW' || 
         currentJudgment.value?.sourceType === 'REPORT'
})

const isDispute = computed(() => {
  return currentJudgment.value?.sourceType === 'DISPUTE'
})

const columns = [
  { title: 'ID', key: 'id', width: 70 },
  {
    title: '来源', key: 'sourceType', width: 100,
    render(row: AiJudgmentRecord) {
      return h(NTag, { type: sourceTypeTagType[row.sourceType] || 'info', size: 'small' }, { default: () => sourceTypeMap[row.sourceType] || row.sourceType })
    }
  },
  { title: '关联ID', key: 'sourceId', width: 80 },
  {
    title: '判决', key: 'verdict', width: 90,
    render(row: AiJudgmentRecord) {
      return h(NTag, { type: verdictTagType[row.verdict] || 'info', size: 'small' }, { default: () => verdictMap[row.verdict] || row.verdict })
    }
  },
  {
    title: '置信度', key: 'confidence', width: 80,
    render(row: AiJudgmentRecord) {
      if (row.status === 'PROCESSING') return h('span', { style: { color: '#999' } }, '-')
      const pct = Math.round(row.confidence * 100)
      const color = pct >= 80 ? '#18a058' : pct >= 50 ? '#f0a020' : '#d03050'
      return h('span', { style: { color, fontWeight: 'bold' } }, `${pct}%`)
    }
  },
  {
    title: '状态', key: 'status', width: 90,
    render(row: AiJudgmentRecord) {
      return h(NTag, { type: statusTagType[row.status] || 'info', size: 'small' }, { default: () => statusMap[row.status] || row.status })
    }
  },
  {
    title: '自动处理', key: 'autoHandled', width: 80,
    render(row: AiJudgmentRecord) {
      if (row.status === 'PROCESSING') return h('span', { style: { color: '#999' } }, '-')
      return h(NTag, { type: row.autoHandled ? 'success' : 'info', size: 'small' }, { default: () => row.autoHandled ? '是' : '否' })
    }
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 180, fixed: 'right' as const,
    render(row: AiJudgmentRecord) {
      return h(NSpace, { size: 4 }, {
        default: () => [
          h(NButton, { size: 'tiny', onClick: () => viewDetail(row) }, { default: () => '详情' }),
          row.status === 'ESCALATED'
            ? h(NButton, { size: 'tiny', type: 'primary', onClick: () => openHandle(row) }, { default: () => '处理' })
            : null,
          row.status === 'APPEALED'
            ? h(NButton, { size: 'tiny', type: 'warning', onClick: () => openHandleAppeal(row) }, { default: () => '处理上诉' })
            : null,
        ].filter(Boolean)
      })
    }
  },
]

async function loadData() {
  loading.value = true
  try {
    const res = await getAdminAiJudgments({
      sourceType: filters.value.sourceType || undefined,
      status: filters.value.status || undefined,
      page: page.value,
      size: pageSize.value,
    })
    const pageData = res.data as unknown as { records: AiJudgmentRecord[]; total: number }
    judgments.value = pageData?.records || []
    total.value = pageData?.total || 0
  } catch {
    message.error('加载判决列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() { page.value = 1; loadData() }

async function viewDetail(row: AiJudgmentRecord) {
  currentJudgment.value = row
  currentGoods.value = null
  currentDispute.value = null

  if (row.sourceType === 'GOODS_REVIEW') {
    try {
      const res = await getAdminGoods({ page: 1, size: 100 })
      const goodsList = ((res.data as any)?.records || []) as any[]
      currentGoods.value = goodsList.find((g: any) => g.id === row.sourceId) || null
    } catch {}
  } else if (row.sourceType === 'REPORT') {
    try {
      const reportRes = await getReportDetail(row.sourceId)
      const report = reportRes.data as unknown as any
      if (report && report.targetId) {
        const goodsRes = await getAdminGoods({ page: 1, size: 100 })
        const goodsList = ((goodsRes.data as any)?.records || []) as any[]
        currentGoods.value = goodsList.find((g: any) => g.id === report.targetId) || null
      }
    } catch {}
  } else if (row.sourceType === 'DISPUTE') {
    try {
      const res = await getAdminDisputes({ page: 1, size: 100 })
      const disputeList = ((res.data as any)?.records || []) as any[]
      currentDispute.value = disputeList.find((d: any) => d.id === row.sourceId) || null
    } catch {}
  }

  showDetailModal.value = true
}

async function openHandle(row: AiJudgmentRecord) {
  currentJudgment.value = row
  handleNote.value = ''
  handleAction.value = null
  currentGoods.value = null
  currentDispute.value = null

  if (row.sourceType === 'GOODS_REVIEW') {
    try {
      const res = await getAdminGoods({ page: 1, size: 100 })
      const goodsList = ((res.data as any)?.records || []) as any[]
      currentGoods.value = goodsList.find((g: any) => g.id === row.sourceId) || null
    } catch {}
  } else if (row.sourceType === 'REPORT') {
    try {
      const reportRes = await getReportDetail(row.sourceId)
      const report = reportRes.data as unknown as any
      if (report && report.targetId) {
        const goodsRes = await getAdminGoods({ page: 1, size: 100 })
        const goodsList = ((goodsRes.data as any)?.records || []) as any[]
        currentGoods.value = goodsList.find((g: any) => g.id === report.targetId) || null
      }
    } catch {}
  } else if (row.sourceType === 'DISPUTE') {
    try {
      const res = await getAdminDisputes({ page: 1, size: 100 })
      const disputeList = ((res.data as any)?.records || []) as any[]
      currentDispute.value = disputeList.find((d: any) => d.id === row.sourceId) || null
    } catch {}
  }

  showHandleModal.value = true
}

async function submitHandle() {
  if (!currentJudgment.value) return

  try {
    if (isDispute.value) {
      const disputeStatus = handleAction.value === 'APPROVED' ? 'RESOLVED' : 'REJECTED'
      await handleDispute(currentJudgment.value.sourceId, disputeStatus, handleNote.value || '')
    } else if (isGoodsRelated.value && currentGoods.value && handleAction.value) {
      if (handleAction.value === 'APPROVED') {
        await approveGoods(currentGoods.value.id)
      } else if (handleAction.value === 'REJECTED') {
        await takeDownGoods(currentGoods.value.id, handleNote.value || 'AI审核未通过，管理员确认下架')
      }
    }

    await handleAiJudgmentManually(currentJudgment.value.id, handleNote.value || '')
    message.success('处理成功')
    showHandleModal.value = false
    loadData()
  } catch {
    message.error('处理失败')
  }
}

function openHandleAppeal(row: AiJudgmentRecord) {
  currentJudgment.value = row
  appealHandleNote.value = ''
  appealOverride.value = null
  currentGoods.value = null
  currentDispute.value = null

  if (row.sourceType === 'GOODS_REVIEW') {
    try {
      const res = getAdminGoods({ page: 1, size: 100 })
      res.then((r: any) => {
        const goodsList = (r.data?.records || []) as any[]
        currentGoods.value = goodsList.find((g: any) => g.id === row.sourceId) || null
      })
    } catch {}
  } else if (row.sourceType === 'REPORT') {
    try {
      const reportRes = getReportDetail(row.sourceId)
      reportRes.then((r: any) => {
        const report = r.data as unknown as any
        if (report && report.targetId) {
          const goodsRes = getAdminGoods({ page: 1, size: 100 })
          goodsRes.then((gr: any) => {
            const goodsList = (gr.data?.records || []) as any[]
            currentGoods.value = goodsList.find((g: any) => g.id === report.targetId) || null
          })
        }
      })
    } catch {}
  } else if (row.sourceType === 'DISPUTE') {
    try {
      const res = getAdminDisputes({ page: 1, size: 100 })
      res.then((r: any) => {
        const disputeList = (r.data?.records || []) as any[]
        currentDispute.value = disputeList.find((d: any) => d.id === row.sourceId) || null
      })
    } catch {}
  }

  showHandleAppealModal.value = true
}

async function submitHandleAppeal() {
  if (!currentJudgment.value || appealOverride.value === null) return

  try {
    await handleAiAppeal(currentJudgment.value.id, appealOverride.value, appealHandleNote.value || '')
    message.success(appealOverride.value ? '上诉成功，已推翻原判' : '上诉驳回，终审结束')
    showHandleAppealModal.value = false
    loadData()
  } catch {
    message.error('处理失败')
  }
}

function goToGoodsDetail(goodsId: number) {
  showDetailModal.value = false
  router.push(`/goods/${goodsId}`)
}

function getEvidenceList(evidence: string | null): string[] {
  if (!evidence) return []
  try { return JSON.parse(evidence) } catch { return [] }
}

onMounted(() => loadData())
</script>

<template>
  <div style="padding: 20px">
    <h2 style="margin: 0 0 20px 0">AI 判决管理</h2>

    <NSpace style="margin-bottom: 16px">
      <NSelect v-model:value="filters.sourceType" :options="sourceTypeOptions" placeholder="来源类型" clearable style="width: 140px" />
      <NSelect v-model:value="filters.status" :options="statusOptions" placeholder="状态" clearable style="width: 140px" />
      <NButton type="primary" @click="handleSearch">查询</NButton>
    </NSpace>

    <NDataTable
      :columns="columns"
      :data="judgments"
      :loading="loading"
      :scroll-x="1200"
      size="small"
      striped
    />

    <NPagination
      v-model:page="page"
      :page-size="pageSize"
      :item-count="total"
      style="margin-top: 16px; justify-content: flex-end"
      @update:page="loadData"
    />

    <!-- 详情弹窗 -->
    <NModal v-model:show="showDetailModal" preset="card" title="判决详情" style="width: 700px">
      <template v-if="currentJudgment">
        <NSpace vertical :size="12">
          <!-- 商品信息展示 -->
          <div v-if="currentGoods" style="background: #f8f9fa; padding: 12px; border-radius: 8px; cursor: pointer" @click="goToGoodsDetail(currentGoods.id)">
            <div style="display: flex; gap: 12px; align-items: center">
              <NImage
                v-if="currentGoods.coverImage"
                :src="getImageUrl(currentGoods.coverImage)"
                width="80"
                height="80"
                object-fit="cover"
                style="border-radius: 8px"
                preview-disabled
              />
              <div style="flex: 1">
                <div style="font-weight: bold; font-size: 16px; margin-bottom: 4px">{{ currentGoods.title }}</div>
                <div style="color: #666; margin-bottom: 4px">{{ currentGoods.categoryName }}</div>
                <div style="color: #3B82F6; font-weight: bold">{{ formatPrice(currentGoods.price) }}</div>
              </div>
              <NButton size="small" type="primary" quaternary>查看详情 →</NButton>
            </div>
          </div>

          <!-- 争议信息展示 -->
          <div v-if="currentDispute" style="background: #fff7e6; padding: 12px; border-radius: 8px">
            <div style="font-weight: bold; font-size: 16px; margin-bottom: 8px">争议信息</div>
            <div><strong>申请人：</strong>{{ currentDispute.applicantNickname }}</div>
            <div><strong>原因：</strong>{{ currentDispute.reason }}</div>
            <div><strong>状态：</strong>
              <NTag :type="currentDispute.status === 'PENDING' ? 'warning' : currentDispute.status === 'RESOLVED' ? 'success' : 'default'" size="small">
                {{ currentDispute.status === 'PENDING' ? '待处理' : currentDispute.status === 'RESOLVED' ? '已判取消' : '已驳回' }}
              </NTag>
            </div>
            <div v-if="currentDispute.handleNote"><strong>处理备注：</strong>{{ currentDispute.handleNote }}</div>
          </div>

          <div><strong>ID：</strong>{{ currentJudgment.id }}</div>
          <div><strong>来源：</strong>{{ sourceTypeMap[currentJudgment.sourceType] }}</div>
          <div><strong>关联ID：</strong>{{ currentJudgment.sourceId }}</div>
          <div><strong>AI模型：</strong>{{ currentJudgment.aiModel }}</div>
          <div>
            <strong>判决：</strong>
            <NTag :type="verdictTagType[currentJudgment.verdict]" size="small">{{ verdictMap[currentJudgment.verdict] }}</NTag>
          </div>
          <div>
            <strong>置信度：</strong>
            <span :style="{ color: currentJudgment.confidence >= 0.8 ? '#18a058' : currentJudgment.confidence >= 0.5 ? '#f0a020' : '#d03050', fontWeight: 'bold' }">
              {{ Math.round(currentJudgment.confidence * 100) }}%
            </span>
          </div>
          <div>
            <strong>状态：</strong>
            <NTag :type="statusTagType[currentJudgment.status]" size="small">{{ statusMap[currentJudgment.status] }}</NTag>
          </div>
          <div><strong>自动处理：</strong>{{ currentJudgment.autoHandled ? '是' : '否' }}</div>
          <div v-if="currentJudgment.reasoning">
            <strong>推理过程：</strong>
            <div style="background: #f5f5f5; padding: 8px; border-radius: 4px; margin-top: 4px; white-space: pre-wrap">{{ currentJudgment.reasoning }}</div>
          </div>
          <div v-if="getEvidenceList(currentJudgment.evidence).length">
            <strong>证据：</strong>
            <ul style="margin: 4px 0 0 20px">
              <li v-for="(item, idx) in getEvidenceList(currentJudgment.evidence)" :key="idx">{{ item }}</li>
            </ul>
          </div>
          <div v-if="currentJudgment.handleNote"><strong>处理备注：</strong>{{ currentJudgment.handleNote }}</div>
          <div v-if="currentJudgment.appealReason"><strong>上诉原因：</strong>{{ currentJudgment.appealReason }}</div>
        </NSpace>
      </template>
    </NModal>

    <!-- 处理弹窗 -->
    <NModal v-model:show="showHandleModal" preset="card" title="处理判决" style="width: 600px">
      <template v-if="currentJudgment">
        <NSpace vertical :size="16">
          <!-- 商品信息展示 -->
          <div v-if="currentGoods" style="background: #f0f7ff; padding: 12px; border-radius: 8px">
            <div style="display: flex; gap: 12px; align-items: center">
              <NImage
                v-if="currentGoods.coverImage"
                :src="getImageUrl(currentGoods.coverImage)"
                width="80"
                height="80"
                object-fit="cover"
                style="border-radius: 8px"
                preview-disabled
              />
              <div style="flex: 1">
                <div style="font-weight: bold; font-size: 16px; margin-bottom: 4px">{{ currentGoods.title }}</div>
                <div style="color: #666; margin-bottom: 4px">{{ currentGoods.categoryName }}</div>
                <div style="color: #3B82F6; font-weight: bold">{{ formatPrice(currentGoods.price) }}</div>
              </div>
            </div>
          </div>

          <!-- 争议信息展示 -->
          <div v-if="currentDispute" style="background: #fff7e6; padding: 12px; border-radius: 8px">
            <div style="font-weight: bold; font-size: 16px; margin-bottom: 8px">争议信息</div>
            <div><strong>申请人：</strong>{{ currentDispute.applicantNickname }}</div>
            <div><strong>原因：</strong>{{ currentDispute.reason }}</div>
            <div><strong>商品：</strong>{{ currentDispute.goodsTitle }}</div>
          </div>

          <!-- AI 判决信息 -->
          <div style="background: #fff7e6; padding: 12px; border-radius: 8px">
            <div><strong>AI 判决：</strong>
              <NTag :type="verdictTagType[currentJudgment.verdict]" size="small">{{ verdictMap[currentJudgment.verdict] }}</NTag>
            </div>
            <div style="margin-top: 8px"><strong>AI 推理：</strong></div>
            <div style="font-size: 13px; color: #666; margin-top: 4px; white-space: pre-wrap">{{ currentJudgment.reasoning }}</div>
          </div>

          <!-- 商品处理选项 -->
          <div v-if="isGoodsRelated && currentGoods">
            <div style="font-weight: bold; margin-bottom: 8px">请选择处理方式：</div>
            <NRadioGroup v-model:value="handleAction">
              <NSpace vertical>
                <NRadioButton value="APPROVED">
                  <span style="color: #18a058">✓ 通过</span> - 商品正常，维持上架
                </NRadioButton>
                <NRadioButton value="REJECTED">
                  <span style="color: #d03050">✗ 拒绝</span> - 商品违规，执行下架
                </NRadioButton>
              </NSpace>
            </NRadioGroup>
          </div>

          <!-- 争议处理选项 -->
          <div v-else-if="isDispute">
            <div style="font-weight: bold; margin-bottom: 8px">请选择处理方式：</div>
            <NRadioGroup v-model:value="handleAction">
              <NSpace vertical>
                <NRadioButton value="APPROVED">
                  <span style="color: #18a058">✓ 取消订单</span> - 同意取消，商品重新上架
                </NRadioButton>
                <NRadioButton value="REJECTED">
                  <span style="color: #d03050">✗ 驳回仲裁</span> - 继续交易
                </NRadioButton>
              </NSpace>
            </NRadioGroup>
          </div>

          <!-- 非商品类型直接选择 -->
          <div v-else>
            <div style="font-weight: bold; margin-bottom:8px">请选择处理方式：</div>
            <NRadioGroup v-model:value="handleAction">
              <NSpace vertical>
                <NRadioButton value="APPROVED">
                  <span style="color: #18a058">✓ 认可AI判决</span>
                </NRadioButton>
                <NRadioButton value="REJECTED">
                  <span style="color: #d03050">✗ 推翻AI判决</span>
                </NRadioButton>
              </NSpace>
            </NRadioGroup>
          </div>

          <NInput v-model:value="handleNote" type="textarea" placeholder="处理备注（选填）" :rows="2" />
        </NSpace>
      </template>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="showHandleModal = false">取消</NButton>
          <NButton type="primary" :disabled="!handleAction" @click="submitHandle">确认处理</NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- 处理上诉弹窗 -->
    <NModal v-model:show="showHandleAppealModal" preset="card" title="处理上诉" style="width: 600px">
      <template v-if="currentJudgment">
        <NSpace vertical :size="16">
          <!-- 上诉原因 -->
          <div style="background: #fff7e6; padding: 12px; border-radius: 8px">
            <div style="font-weight: bold; margin-bottom: 8px">上诉原因</div>
            <div style="font-size: 13px; color: #666">{{ currentJudgment.appealReason || '未填写' }}</div>
          </div>

          <!-- 原判信息 -->
          <div style="background: #f5f5f5; padding: 12px; border-radius: 8px">
            <div style="font-weight: bold; margin-bottom: 8px">原判信息</div>
            <div><strong>AI 判决：</strong>
              <NTag :type="verdictTagType[currentJudgment.verdict]" size="small">{{ verdictMap[currentJudgment.verdict] }}</NTag>
            </div>
            <div style="margin-top: 8px"><strong>AI 推理：</strong></div>
            <div style="font-size: 13px; color: #666; margin-top: 4px; white-space: pre-wrap">{{ currentJudgment.reasoning }}</div>
          </div>

          <!-- 处理选项 -->
          <div>
            <div style="font-weight: bold; margin-bottom: 8px">处理结果：</div>
            <NRadioGroup v-model:value="appealOverride">
              <NSpace vertical>
                <NRadioButton :value="true">
                  <span style="color: #18a058">✓ 推翻原判</span> - 上诉成功
                </NRadioButton>
                <NRadioButton :value="false">
                  <span style="color: #d03050">✗ 维持原判</span> - 终审驳回，不可再上诉
                </NRadioButton>
              </NSpace>
            </NRadioGroup>
          </div>

          <NInput v-model:value="appealHandleNote" type="textarea" placeholder="处理说明（选填）" :rows="2" />
        </NSpace>
      </template>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="showHandleAppealModal = false">取消</NButton>
          <NButton type="primary" :disabled="appealOverride === null" @click="submitHandleAppeal">确认处理</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>