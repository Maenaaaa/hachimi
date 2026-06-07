import { get } from '@/utils/request'
import type { Announcement } from '@/types/entity'

export function getAnnouncements() {
  return get<Announcement[]>('/announcement')
}
