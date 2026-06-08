import axios, { type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { TOKEN_KEY, REFRESH_KEY } from '@/constants'
import type { ApiResponse } from '@/types/api'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

function getToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

function getRefreshToken(): string | null {
  try {
    return localStorage.getItem(REFRESH_KEY)
  } catch {
    return null
  }
}

function setTokens(access: string, refresh: string): void {
  try {
    localStorage.setItem(TOKEN_KEY, access)
    localStorage.setItem(REFRESH_KEY, refresh)
  } catch {
    // ignore
  }
}

function clearTokens(): void {
  try {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(REFRESH_KEY)
    localStorage.removeItem('current_user')
  } catch {
    // ignore
  }
}

let isRefreshing = false
let refreshSubscribers: Array<(token: string) => void> = []

function onRefreshed(token: string): void {
  refreshSubscribers.forEach((cb) => cb(token))
  refreshSubscribers = []
}

function addRefreshSubscriber(cb: (token: string) => void): void {
  refreshSubscribers.push(cb)
}

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { data } = response
    if (data.code === 0 || data.code === 200) {
      return response
    }
    if (data.code === 401) {
      // Don't intercept 401 for login/register — pass error to caller
      const url = response.config.url || ''
      if (url.includes('/auth/login') || url.includes('/auth/register')) {
        return Promise.reject(new Error(data.message || '用户名或密码错误'))
      }
      const originalRequest = response.config as InternalAxiosRequestConfig & { _retry?: boolean }
      if (!originalRequest._retry) {
        originalRequest._retry = true
        const refreshToken = getRefreshToken()
        if (!refreshToken) {
          clearTokens()
          window.location.href = '/login'
          return Promise.reject(new Error(data.message || '未登录'))
        }
        if (!isRefreshing) {
          isRefreshing = true
          return axios
            .post<ApiResponse<{ accessToken: string; refreshToken: string }>>('/api/auth/refresh', {
              refreshToken,
            })
            .then((res) => {
              const d = res.data.data
              setTokens(d.accessToken, d.refreshToken)
              isRefreshing = false
              onRefreshed(d.accessToken)
              if (originalRequest.headers) {
                originalRequest.headers.Authorization = `Bearer ${d.accessToken}`
              }
              return request(originalRequest)
            })
            .catch(() => {
              clearTokens()
              isRefreshing = false
              refreshSubscribers = []
              window.location.href = '/login'
              return Promise.reject(new Error('登录已过期'))
            })
        }
        return new Promise((resolve) => {
          addRefreshSubscriber((token: string) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`
            }
            resolve(request(originalRequest))
          })
        })
      }
    }
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      const url = error.config?.url || ''
      if (url.includes('/auth/login') || url.includes('/auth/register')) {
        return Promise.reject(new Error('用户名或密码错误'))
      }
      clearTokens()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export function get<T>(url: string, params?: Record<string, unknown>): Promise<ApiResponse<T>> {
  return request.get<ApiResponse<T>>(url, { params }).then((res) => res.data)
}

export function post<T>(url: string, data?: unknown): Promise<ApiResponse<T>> {
  return request.post<ApiResponse<T>>(url, data).then((res) => res.data)
}

export function put<T>(url: string, data?: unknown): Promise<ApiResponse<T>> {
  return request.put<ApiResponse<T>>(url, data).then((res) => res.data)
}

export function del<T>(url: string, params?: Record<string, unknown>): Promise<ApiResponse<T>> {
  return request.delete<ApiResponse<T>>(url, { params }).then((res) => res.data)
}

export function upload<T>(url: string, formData: FormData): Promise<ApiResponse<T>> {
  return request
    .post<ApiResponse<T>>(url, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      transformRequest: [(data: FormData) => data],
    })
    .then((res) => res.data)
}

export { clearTokens, setTokens, getToken }
export default request
