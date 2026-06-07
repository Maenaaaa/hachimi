import { post } from '@/utils/request'
import type { LoginForm, RegisterForm } from '@/types/entity'

export function login(data: LoginForm) {
  return post<{ accessToken: string; refreshToken: string; user: import('@/types/entity').User }>('/auth/login', data)
}

export function register(data: RegisterForm) {
  return post<{ accessToken: string; refreshToken: string }>('/auth/register', data)
}

export function refreshToken(refreshToken: string) {
  return post<{ accessToken: string; refreshToken: string }>('/auth/refresh', { refreshToken })
}
