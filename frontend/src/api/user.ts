import { get, put, post, upload } from '@/utils/request'
import type { User, ProfileForm, PasswordForm } from '@/types/entity'

export interface UserPublicVO {
  id: number
  nickname: string
  avatar: string
  school: string
  creditScore: number
  realName?: string
  authTitle?: string
  goodsCount?: number
  followerCount?: number
  followingCount?: number
}

export function getUserProfile() {
  return get<User>('/user/profile')
}

export function getUserPublicProfile(userId: number) {
  return get<UserPublicVO>(`/user/profile/${userId}`)
}

export function updateUserProfile(data: ProfileForm) {
  return put<User>('/user/profile', data)
}

export function changePassword(data: PasswordForm) {
  return put<null>('/user/password', data)
}

export function uploadAvatar(formData: FormData) {
  return upload<string>('/user/avatar', formData)
}

export function verifyUser(formData: FormData) {
  return upload<null>('/user/verify', formData)
}
