import { get, put, post, upload } from '@/utils/request'
import type { User, UserStats, ProfileForm, PasswordForm } from '@/types/entity'

export function getUserProfile() {
  return get<User>('/user/profile')
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

export function verifyUser(data: { realName: string; idNumber: string }) {
  return post<null>('/user/verify', data)
}

export function getUserStats(userId: number) {
  return get<UserStats>(`/user/stats/${userId}`)
}
