export function formatPrice(price: number | null | undefined): string {
  if (price == null) return '¥0.00'
  return '¥' + Number(price).toFixed(2)
}

export function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return minutes + '分钟前'
  if (hours < 24) return hours + '小时前'
  if (days < 7) return days + '天前'

  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export function formatDateTime(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}:${s}`
}

export function debounce<T extends (...args: unknown[]) => unknown>(
  fn: T,
  delay: number,
): (...args: Parameters<T>) => void {
  let timer: ReturnType<typeof setTimeout> | null = null
  return (...args: Parameters<T>) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}

export function getImageUrl(path: string | undefined | null): string {
  if (!path) return '/default-image.svg'
  const minioMatch = path.match(/http:\/\/[^/]+\/([^/]+)\/([^?]+)/)
  if (minioMatch) {
    return `/api/file/${minioMatch[1]}/${minioMatch[2]}`
  }
  if (path.startsWith('http')) return path
  return '/api/file/' + path
}

export type AvatarSize = 'original' | 'thumb_256' | 'thumb_128' | 'thumb_64'

// 用于清除缓存的时间戳
let avatarCacheBuster = Date.now()

export function updateAvatarCache() {
  avatarCacheBuster = Date.now()
}

export function getAvatarUrl(path: string | undefined | null, size: AvatarSize = 'original'): string {
  if (!path) return '/default-avatar.svg'
  
  // 处理完整URL（旧数据兼容）
  if (path.startsWith('http')) {
    const match = path.match(/\/avatars\/(.+)$/)
    if (match) {
      path = 'avatars/' + match[1]
    } else {
      return path
    }
  }
  
  // 处理相对路径 avatars/xxx/original.png
  if (path.includes('avatars/')) {
    // 提取 userId
    const userMatch = path.match(/avatars\/(\d+)\//)
    if (userMatch) {
      const userId = userMatch[1]
      if (size === 'original') {
        return `/api/file/avatars/${userId}/original.png?t=${avatarCacheBuster}`
      }
      return `/api/file/avatars/${userId}/${size}.png?t=${avatarCacheBuster}`
    }
  }
  
  // 如果路径不包含 avatars/，可能是旧格式，直接返回
  if (path.startsWith('/')) return path
  
  return '/default-avatar.svg'
}
