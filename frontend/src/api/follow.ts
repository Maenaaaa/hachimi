import { post, del, get } from '@/utils/request'

export interface UserPublicVO {
  id: number
  nickname: string
  avatar: string
  school: string
  creditScore: number
  goodsCount?: number
  followerCount?: number
  followingCount?: number
}

export function addFollow(userId: number) {
  return post<null>('/follow', { followeeId: userId })
}

export function removeFollow(userId: number) {
  return del<null>(`/follow/${userId}`)
}

export function getFollowers(userId: number, page = 1, size = 20) {
  return get<UserPublicVO[]>('/follow/followers', { userId, page, size } as Record<string, unknown>)
}

export function getFollowing(userId: number, page = 1, size = 20) {
  return get<UserPublicVO[]>('/follow/following', { userId, page, size } as Record<string, unknown>)
}
