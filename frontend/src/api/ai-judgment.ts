import { get, put } from '@/utils/request'
import type { AiJudgmentRecord } from '@/types/entity'
import type { PageResult } from '@/types/api'

export function getAiJudgmentById(id: number) {
  return get<AiJudgmentRecord>(`/ai-judgment/${id}`)
}

export function getAiJudgmentBySource(type: string, sourceId: number) {
  return get<AiJudgmentRecord>(`/ai-judgment/source/${type}/${sourceId}`)
}

export function getAiJudgmentsByGoodsId(goodsId: number) {
  return get<{ goodsReview: AiJudgmentRecord; report: AiJudgmentRecord }>(`/ai-judgment/goods/${goodsId}`)
}

export function appealAiJudgment(id: number, reason: string) {
  return put<null>(`/ai-judgment/${id}/appeal`, { reason })
}

export function handleAiAppeal(id: number, override: boolean, note: string) {
  return put<null>(`/ai-judgment/${id}/handle-appeal`, { override, note })
}

export function getAiJudgmentConfig() {
  return get<Record<string, string>>('/ai-judgment/config')
}

export function updateAiJudgmentConfig(key: string, value: string) {
  return put<null>(`/ai-judgment/config/${key}`, { value })
}

export function getAdminAiJudgments(params?: { sourceType?: string; status?: string; page?: number; size?: number }) {
  return get<PageResult<AiJudgmentRecord>>('/admin/ai-judgment', params)
}

export function getAdminAiJudgmentById(id: number) {
  return get<AiJudgmentRecord>(`/admin/ai-judgment/${id}`)
}

export function executeAiVerdict(id: number) {
  return put<null>(`/admin/ai-judgment/${id}/execute`)
}

export function handleAiJudgmentManually(id: number, note: string) {
  return put<null>(`/admin/ai-judgment/${id}/handle`, { note })
}
