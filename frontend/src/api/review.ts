import { get, post } from '@/utils/request'
import type { Review } from '@/types/entity'

export function createReview(data: { orderId: number; rating: number; content: string }) {
  return post<Review>('/review', data)
}

export function getUserReviews(userId: number) {
  return get<Review[]>(`/review/user/${userId}`)
}
