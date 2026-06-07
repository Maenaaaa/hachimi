import { get, post } from '@/utils/request'

export function createComment(data: { goodsId: number; content: string; parentId?: number }) {
  return post<any>('/comment', data)
}

export function getComments(goodsId: number) {
  return get<any[]>(`/comment/goods/${goodsId}`)
}
