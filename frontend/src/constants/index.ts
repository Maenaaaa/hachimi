export const TOKEN_KEY = 'access_token'
export const REFRESH_KEY = 'refresh_token'
export const USER_KEY = 'current_user'

export const GOODS_CONDITIONS: Record<string, string> = {
  BRAND_NEW: '全新',
  LIKE_NEW: '几乎全新',
  MINOR_WEAR: '轻微使用',
  VISIBLE_WEAR: '明显使用',
  HEAVILY_USED: '使用较重',
}

export const GOODS_STATUS: Record<string, string> = {
  ACTIVE: '在售',
  INACTIVE: '已下架',
  PENDING_REVIEW: '待审核',
  REJECTED: '未通过',
  TAKEN_DOWN: '强制下架',
}

export const ORDER_STATUS: Record<string, string> = {
  PENDING: '待确认',
  IN_PROGRESS: '交易中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

export const NOTIFICATION_TYPES: Record<string, string> = {
  SYSTEM: '系统通知',
  ORDER: '订单通知',
  REVIEW: '审核通知',
}

export const REPORT_STATUS: Record<string, string> = {
  PENDING: '待处理',
  APPROVED: '已处理',
  REJECTED: '已驳回',
}
