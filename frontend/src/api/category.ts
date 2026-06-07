import { get } from '@/utils/request'
import type { Category } from '@/types/entity'

export function getCategories() {
  return get<Category[]>('/category')
}
