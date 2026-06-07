import { get, put } from '@/utils/request'
import type { Notification } from '@/types/entity'

export function getNotifications() {
  return get<Notification[]>('/notification')
}

export function readNotification(id: number) {
  return put<null>(`/notification/${id}/read`)
}

export function readAllNotifications() {
  return put<null>('/notification/read-all')
}

export function getUnreadCount() {
  return get<number>('/notification/unread-count')
}
