<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { NCard, NSwitch, NSlider, NDivider, NSpace, useMessage } from 'naive-ui'
import { getAiJudgmentConfig, updateAiJudgmentConfig } from '@/api/ai-judgment'

const message = useMessage()

const config = reactive<Record<string, string>>({
  report_auto_review: 'true',
  dispute_auto_arbitrate: 'true',
  goods_auto_review: 'false',
  confidence_auto_threshold: '0.8',
  confidence_escalate_threshold: '0.5',
})

const autoThreshold = ref(80)
const escalateThreshold = ref(50)

async function loadConfig() {
  try {
    const res = await getAiJudgmentConfig()
    if (res.data) {
      Object.assign(config, res.data)
      autoThreshold.value = Math.round(parseFloat(config.confidence_auto_threshold) * 100)
      escalateThreshold.value = Math.round(parseFloat(config.confidence_escalate_threshold) * 100)
    }
  } catch {
    console.error('加载配置失败')
  }
}

async function handleSwitchChange(key: string, value: boolean) {
  try {
    await updateAiJudgmentConfig(key, value ? 'true' : 'false')
    config[key] = value ? 'true' : 'false'
    message.success('配置已更新')
  } catch {
    message.error('更新失败')
    loadConfig()
  }
}

async function handleThresholdChange(key: string, value: number) {
  try {
    const val = (value / 100).toString()
    await updateAiJudgmentConfig(key, val)
    config[key] = val
    message.success('阈值已更新')
  } catch {
    message.error('更新失败')
    loadConfig()
  }
}

onMounted(() => loadConfig())
</script>

<template>
  <div style="padding: 20px">
    <h2 style="margin: 0 0 20px 0">AI 配置管理</h2>

    <NCard title="自动审核开关">
      <NSpace vertical :size="20">
        <div style="display: flex; align-items: center; gap: 16px">
          <NSwitch
            :value="config.report_auto_review === 'true'"
            @update:value="(v: boolean) => handleSwitchChange('report_auto_review', v)"
          />
          <span>
            <strong>举报自动AI审核</strong>
            <div style="color: #999; font-size: 13px">开启后，新举报将自动调用AI审核</div>
          </span>
        </div>

        <div style="display: flex; align-items: center; gap: 16px">
          <NSwitch
            :value="config.dispute_auto_arbitrate === 'true'"
            @update:value="(v: boolean) => handleSwitchChange('dispute_auto_arbitrate', v)"
          />
          <span>
            <strong>争议自动AI仲裁</strong>
            <div style="color: #999; font-size: 13px">开启后，新争议将自动调用AI仲裁</div>
          </span>
        </div>

        <div style="display: flex; align-items: center; gap: 16px">
          <NSwitch
            :value="config.goods_auto_review === 'true'"
            @update:value="(v: boolean) => handleSwitchChange('goods_auto_review', v)"
          />
          <span>
            <strong>商品自动AI审核</strong>
            <div style="color: #999; font-size: 13px">开启后，新发布的商品将自动调用AI审核</div>
          </span>
        </div>
      </NSpace>
    </NCard>

    <NDivider />

    <NCard title="置信度阈值">
      <NSpace vertical :size="24">
        <div>
          <div style="margin-bottom: 8px">
            <strong>自动执行置信度阈值：</strong>
            <span style="color: #18a058; font-weight: bold">{{ autoThreshold }}%</span>
          </div>
          <NSlider
            v-model:value="autoThreshold"
            :min="0"
            :max="100"
            :step="5"
            :marks="{ 0: '0%', 50: '50%', 80: '80%', 100: '100%' }"
            @update:value="(v: number) => handleThresholdChange('confidence_auto_threshold', v)"
          />
          <div style="color: #999; font-size: 13px; margin-top: 4px">置信度高于此值将自动执行判决结果</div>
        </div>

        <div>
          <div style="margin-bottom: 8px">
            <strong>转人工置信度阈值：</strong>
            <span style="color: #f0a020; font-weight: bold">{{ escalateThreshold }}%</span>
          </div>
          <NSlider
            v-model:value="escalateThreshold"
            :min="0"
            :max="100"
            :step="5"
            :marks="{ 0: '0%', 50: '50%', 80: '80%', 100: '100%' }"
            @update:value="(v: number) => handleThresholdChange('confidence_escalate_threshold', v)"
          />
          <div style="color: #999; font-size: 13px; margin-top: 4px">置信度低于此值将直接转人工处理</div>
        </div>
      </NSpace>
    </NCard>
  </div>
</template>
