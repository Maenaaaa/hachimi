import { get, post } from '@/utils/request'
import type { Report } from '@/types/entity'

export function createReport(data: { targetId: number; targetType: 'goods' | 'user'; reason: string; description?: string }) {
  return post<Report>('/report', data)
}

export function getMyReports() {
  return get<Report[]>('/report/my')
}
