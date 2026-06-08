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
  return '/api' + path
}
