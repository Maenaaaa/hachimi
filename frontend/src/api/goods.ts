import { get, post, put, del } from '@/utils/request'
import type { Goods, GoodsPublishForm, SearchParams } from '@/types/entity'
import type { PageResult } from '@/types/api'

export function getGoodsList(params: SearchParams) {
  return get<PageResult<Goods>>('/goods', params as Record<string, unknown>)
}

export function searchGoods(params: Record<string, unknown>) {
  return get<PageResult<any>>('/search', params)
}

export function getGoodsRecommend() {
  return get<Goods[]>('/goods/recommend')
}

export function getGoodsLatest() {
  return get<Goods[]>('/goods/latest')
}

export function getGoodsDetail(id: number) {
  return get<Goods>(`/goods/${id}`)
}

export function getMyGoods() {
  return get<any[]>('/goods/my')
}

export function getUserGoods(userId: number) {
  return get<any[]>(`/goods/user/${userId}`)
}

export function createGoods(data: GoodsPublishForm) {
  return post<Goods>('/goods', data)
}

export function updateGoods(id: number, data: Partial<GoodsPublishForm>) {
  return put<Goods>(`/goods/${id}`, data)
}

export function deleteGoods(id: number) {
  return del<null>(`/goods/${id}`)
}

export function toggleGoodsStatus(id: number, status: string) {
  return put<null>(`/goods/${id}/status`, { status })
}
