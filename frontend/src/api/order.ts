import { get, post, put } from '@/utils/request'
import type { Order } from '@/types/entity'

export function createOrder(data: { goodsId: number; exchangeGoodsId?: number; remark?: string; meetTime?: string; meetPlace?: string }) {
  return post<Order>('/order', data)
}

export function getBuyerOrders() {
  return get<Order[]>('/order/buyer')
}

export function getSellerOrders() {
  return get<Order[]>('/order/seller')
}

export function confirmOrder(id: number) {
  return put<Order>(`/order/${id}/confirm`)
}

export function cancelOrder(id: number) {
  return put<Order>(`/order/${id}/cancel`)
}

export function completeOrder(id: number) {
  return put<Order>(`/order/${id}/complete`)
}

export function getOrderDetail(id: number) {
  return get<Order>(`/order/${id}`)
}

export function requestCancelOrder(id: number, reason: string) {
  return put<Order>(`/order/${id}/cancel-request`, { reason })
}

export function approveCancelOrder(id: number) {
  return put<Order>(`/order/${id}/cancel-approve`)
}

export function rejectCancelOrder(id: number) {
  return put<Order>(`/order/${id}/cancel-reject`)
}

export function createDispute(id: number, reason: string) {
  return put<null>(`/order/${id}/dispute`, { reason })
}
