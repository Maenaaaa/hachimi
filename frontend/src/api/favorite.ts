import { post, del, get } from '@/utils/request'
import type { Favorite } from '@/types/entity'

export function addFavorite(goodsId: number) {
  return post<null>('/favorite', { goodsId })
}

export function removeFavorite(goodsId: number) {
  return del<null>(`/favorite/${goodsId}`)
}

export function getMyFavorites() {
  return get<Favorite[]>('/favorite/my')
}
