import { get, post, put } from '@/utils/request'

export function getConversationList() {
  return get<any[]>('/conversation/list')
}

export function createConversation(goodsId: number) {
  return post<any>('/conversation', { goodsId })
}

export function getMessages(convId: number, page = 1, size = 50) {
  return get<any[]>(`/message/${convId}`, { page, size } as Record<string, unknown>)
}

export function markAsRead(convId: number) {
  return put<null>(`/message/${convId}/read`)
}

export function getUnreadCount() {
  return get<number>('/message/unread-count')
}
