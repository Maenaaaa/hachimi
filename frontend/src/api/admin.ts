import { get, put, post, del } from '@/utils/request'
import type { DashboardStats, User, Goods, Report, Announcement, Verification } from '@/types/entity'
import type { PageResult } from '@/types/api'

export function getDashboardStats() {
  return get<DashboardStats>('/admin/dashboard')
}

export function getAdminUsers(params: { page: number; size: number; keyword?: string }) {
  return get<PageResult<User>>('/admin/users', params as Record<string, unknown>)
}

export function disableUser(id: number) {
  return put<null>(`/admin/user/${id}/disable`)
}

export function enableUser(id: number) {
  return put<null>(`/admin/user/${id}/enable`)
}

export function getAdminGoods(params: { page: number; size: number; status?: string; keyword?: string }) {
  return get<PageResult<Goods>>('/admin/goods', params as Record<string, unknown>)
}

export function approveGoods(id: number) {
  return put<null>(`/admin/goods/${id}/approve`)
}

export function rejectGoods(id: number, reason?: string) {
  return put<null>(`/admin/goods/${id}/reject`, { reason })
}

export function takeDownGoods(id: number, reason?: string) {
  return put<null>(`/admin/goods/${id}/take-down`, { reason })
}

export function deleteGoods(id: number) {
  return del<null>(`/admin/goods/${id}`)
}

export function getAdminReports(params: { page: number; size: number; status?: string }) {
  return get<PageResult<Report>>('/admin/report', params as Record<string, unknown>)
}

export function getReportDetail(id: number) {
  return get<Report>(`/admin/report/${id}`)
}

export function handleReport(id: number, status: string, handleNote?: string) {
  return put<null>(`/admin/report/${id}/handle`, { status, handleNote })
}

export function getAdminAnnouncements() {
  return get<Announcement[]>('/announcement')
}

export function createAnnouncement(data: { title: string; content: string }) {
  return post<Announcement>('/admin/announcement', data)
}

export function updateAnnouncement(id: number, data: { title?: string; content?: string }) {
  return put<Announcement>(`/admin/announcement/${id}`, data)
}

export function deleteAnnouncement(id: number) {
  return del<null>(`/admin/announcement/${id}`)
}

export function getAdminVerifications(params: { page: number; size: number; status?: string }) {
  return get<PageResult<Verification>>('/admin/verifications', params as Record<string, unknown>)
}

export function approveVerification(id: number) {
  return put<null>(`/admin/verification/${id}/approve`)
}

export function rejectVerification(id: number, reason?: string) {
  return put<null>(`/admin/verification/${id}/reject`, { reason })
}

export function searchGoods(keyword: string) {
  return get<Goods[]>('/search', { keyword } as Record<string, unknown>)
}

export function getAdminDisputes(params: { page: number; size: number; status?: string }) {
  return get<PageResult<any>>('/admin/dispute', params as Record<string, unknown>)
}

export function handleDispute(id: number, status: string, handleNote?: string) {
  return put<null>(`/admin/dispute/${id}/handle`, { status, handleNote })
}
