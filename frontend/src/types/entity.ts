/** 用户 */
export interface User {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone: string
  role: 'user' | 'admin'
  status: 'active' | 'disabled'
  verified: boolean
  createdAt: string
  updatedAt: string
}

/** 用户统计 */
export interface UserStats {
  goodsCount: number
  soldCount: number
  favoriteCount: number
  followerCount: number
  followingCount: number
  reviewCount: number
}

/** 分类 */
export interface Category {
  id: number
  name: string
  icon: string
  parentId: number | null
  children?: Category[]
}

/** 商品 */
export interface Goods {
  id: number
  title: string
  description: string
  price: number
  originalPrice: number | null
  images: string[]
  condition: 'BRAND_NEW' | 'LIKE_NEW' | 'MINOR_WEAR' | 'VISIBLE_WEAR' | 'HEAVILY_USED'
  categoryId: number
  categoryName: string
  tradeType: 'SELL' | 'EXCHANGE'
  status: 'ACTIVE' | 'INACTIVE' | 'PENDING_REVIEW' | 'REJECTED' | 'TAKEN_DOWN'
  userId: number
  user: User
  favoriteCount: number
  viewCount: number
  isFavorite: boolean
  createdAt: string
  updatedAt: string
}

/** 商品发布表单 */
export interface GoodsPublishForm {
  title: string
  description: string
  price: number
  originalPrice: number | null
  images: string[]
  condition: Goods['condition']
  categoryId: number
  campus: string
  tradeType: 'SELL' | 'EXCHANGE'
}

/** 订单 */
export interface Order {
  id: number
  goodsId: number
  goodsTitle: string
  goodsCoverImage: string
  goodsDescription?: string
  buyerId: number
  buyerNickname: string
  buyerAvatar: string
  sellerId: number
  sellerNickname: string
  sellerAvatar: string
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  amount: number
  remark: string
  meetTime: string
  meetPlace: string
  createTime: string
  updateTime: string
}

/** 评价 */
export interface Review {
  id: number
  orderId: number
  userId: number
  user: User
  targetUserId: number
  rating: number
  content: string
  createdAt: string
}

/** 收藏 */
export interface Favorite {
  id: number
  userId: number
  goodsId: number
  goods: Goods
  createdAt: string
}

/** 关注 */
export interface Follow {
  id: number
  userId: number
  followUserId: number
  followUser: User
  createdAt: string
}

/** 会话 */
export interface Conversation {
  id: number
  goodsId: number
  goods: Goods
  participantIds: number[]
  participants: User[]
  lastMessage: Message | null
  unreadCount: number
  createdAt: string
  updatedAt: string
}

/** 消息 */
export interface Message {
  id: number
  conversationId: number
  senderId: number
  sender: User
  content: string
  type: 'text' | 'image' | 'system'
  createdAt: string
}

/** 通知 */
export interface Notification {
  id: number
  userId: number
  title: string
  content: string
  type: 'system' | 'order' | 'chat' | 'goods'
  relatedId: number | null
  read: boolean
  createdAt: string
}

/** 公告 */
export interface Announcement {
  id: number
  title: string
  content: string
  status: 'published' | 'draft'
  priority: 'low' | 'normal' | 'high'
  createdAt: string
  updatedAt: string
}

/** 举报 */
export interface Report {
  id: number
  reporterId: number
  reporter: User
  targetId: number
  targetType: 'goods' | 'user'
  reason: string
  description: string
  status: 'pending' | 'resolved' | 'dismissed'
  handleNote: string
  createdAt: string
  updatedAt: string
}

/** 管理后台统计 */
export interface DashboardStats {
  totalUsers: number
  totalGoods: number
  totalOrders: number
  todayActiveUsers: number
  pendingReviewGoods: number
  pendingReports: number
  todayNewUsers: number
  todayNewOrders: number
}

/** 搜索参数 */
export interface SearchParams {
  keyword?: string
  categoryId?: number
  condition?: string
  minPrice?: number
  maxPrice?: number
  sortBy?: 'latest' | 'price_asc' | 'price_desc' | 'popular'
  page?: number
  size?: number
}

/** 登录表单 */
export interface LoginForm {
  username: string
  password: string
}

/** 注册表单 */
export interface RegisterForm {
  username: string
  password: string
  confirmPassword: string
  nickname: string
}

/** 用户资料表单 */
export interface ProfileForm {
  nickname: string
  email: string
  phone: string
}

/** 修改密码表单 */
export interface PasswordForm {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}
