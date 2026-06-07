import { post, del, get } from '@/utils/request'
import type { Follow } from '@/types/entity'

export function addFollow(userId: number) {
  return post<null>('/follow', { followeeId: userId })
}

export function removeFollow(userId: number) {
  return del<null>(`/follow/${userId}`)
}

export function getFollowers(userId: number, page = 1, size = 20) {
  return get<Follow[]>('/follow/followers', { userId, page, size } as Record<string, unknown>)
}

export function getFollowing(userId: number, page = 1, size = 20) {
  return get<Follow[]>('/follow/following', { userId, page, size } as Record<string, unknown>)
}
