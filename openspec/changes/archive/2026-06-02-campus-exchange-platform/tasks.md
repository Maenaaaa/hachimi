## 1. Database Design & SQL

- [x] 1.1 Write ER diagram design document (entities, relationships, cardinality)
- [x] 1.2 Write complete SQL schema script (all 16+ tables with columns, types, defaults, comments)
- [x] 1.3 Write index creation script (primary keys, unique indexes, composite indexes)
- [x] 1.4 Write foreign key constraint script
- [x] 1.5 Write seed data script (categories, default admin user, sample data)

## 2. Backend Project Initialization

- [x] 2.1 Generate Spring Boot 3 project with dependencies (Web, Security, MyBatis Plus, MySQL, Redis, WebSocket, MinIO, Validation, Lombok)
- [x] 2.2 Configure application.yml (datasource, redis, minio, jwt secret, server port, file upload limits)
- [x] 2.3 Create unified package structure: common, config, controller, dto, entity, mapper, service, service.impl, vo, security, websocket, exception, utils, constant
- [x] 2.4 Implement unified response body `Result<T>` with code, message, data
- [x] 2.5 Implement global exception handler (`@RestControllerAdvice`) with business exception `BusinessException`
- [x] 2.6 Configure MyBatis Plus (pagination plugin, optimistic lock, auto-fill for create_time/update_time, logic delete)
- [x] 2.7 Configure CORS for frontend development

## 3. Backend Security & Auth

- [x] 3.1 Implement JWT utility (generate token, parse token, validate token, extract user info)
- [x] 3.2 Implement `JwtAuthenticationFilter` extending `OncePerRequestFilter`
- [x] 3.3 Configure `SecurityConfig` with permit paths, role-based access, stateless session, JWT filter registration
- [x] 3.4 Implement `UserDetailsService` loading user from database
- [x] 3.5 Implement login endpoint (`/api/auth/login`) returning JWT token pair
- [x] 3.6 Implement register endpoint (`/api/auth/register`) with password encryption (BCrypt)
- [x] 3.7 Implement token refresh endpoint (`/api/auth/refresh`)
- [x] 3.8 Implement logout endpoint (invalidate refresh token)
- [x] 3.9 Add `@CurrentUser` annotation to inject authenticated user into controller parameters

## 4. Backend Entity & Mapper Layer

- [x] 4.1 Create `User` entity with all fields
- [x] 4.2 Create `UserAuth` entity (verification records)
- [x] 4.3 Create `Category` entity
- [x] 4.4 Create `Goods` entity
- [x] 4.5 Create `GoodsImage` entity
- [x] 4.6 Create `GoodsView` entity
- [x] 4.7 Create `Favorite` entity
- [x] 4.8 Create `Follow` entity
- [x] 4.9 Create `Conversation` entity
- [x] 4.10 Create `Message` entity
- [x] 4.11 Create `Order` entity
- [x] 4.12 Create `OrderLog` entity
- [x] 4.13 Create `Review` entity
- [x] 4.14 Create `Report` entity
- [x] 4.15 Create `Notification` entity
- [x] 4.16 Create `Announcement` entity
- [x] 4.17 Create all Mapper interfaces extending `BaseMapper<T>` for each entity
- [x] 4.18 Create all Mapper XML files for complex queries

## 5. Backend DTO & VO Layer

- [x] 5.1 Create auth DTOs: LoginDTO, RegisterDTO, RefreshTokenDTO
- [x] 5.2 Create user DTOs: UpdateProfileDTO, ChangePasswordDTO, RealNameVerifyDTO
- [x] 5.3 Create goods DTOs: GoodsPublishDTO, GoodsUpdateDTO, GoodsSearchDTO
- [x] 5.4 Create order DTOs: CreateOrderDTO
- [x] 5.5 Create review DTOs: CreateReviewDTO
- [x] 5.6 Create report DTOs: CreateReportDTO, HandleReportDTO
- [x] 5.7 Create announcement DTOs: CreateAnnouncementDTO, UpdateAnnouncementDTO
- [x] 5.8 Create user VO: UserVO, UserProfileVO, UserPublicVO
- [x] 5.9 Create goods VO: GoodsVO, GoodsDetailVO, GoodsCardVO
- [x] 5.10 Create order VO: OrderVO, OrderDetailVO
- [x] 5.11 Create review VO: ReviewVO
- [x] 5.12 Create conversation VO: ConversationVO, MessageVO
- [x] 5.13 Create notification VO: NotificationVO
- [x] 5.14 Create report VO: ReportVO
- [x] 5.15 Create admin VO: DashboardVO, AdminUserVO, AdminGoodsVO

## 6. Backend Service & Controller — User Module

- [x] 6.1 Implement `UserService.getProfile()` — get current user profile
- [x] 6.2 Implement `UserService.getPublicProfile(userId)` — get other user public info
- [x] 6.3 Implement `UserService.updateProfile(dto)` — update nickname, school, phone, email
- [x] 6.4 Implement `UserService.changePassword(dto)` — verify old password, update new
- [x] 6.5 Implement `UserService.uploadAvatar(file)` — upload to MinIO, update avatar URL
- [x] 6.6 Implement `UserService.submitVerification(dto)` — submit real-name verification
- [x] 6.7 Implement `UserService.getVerificationStatus()` — check own verification status
- [x] 6.8 Create `UserController` with all user endpoints
- [x] 6.9 Add parameter validation annotations (`@Valid`, `@NotBlank`, etc.)

## 7. Backend Service & Controller — Goods Module

- [x] 7.1 Implement `CategoryService.listAll()` — get all categories
- [x] 7.2 Implement `CategoryService.getById(id)` — get single category
- [x] 7.3 Implement `GoodsService.publish(dto, userId)` — create goods with images
- [x] 7.4 Implement `GoodsService.update(id, dto, userId)` — update goods, reset review status
- [x] 7.5 Implement `GoodsService.delete(id, userId)` — soft delete with active order check
- [x] 7.6 Implement `GoodsService.toggleStatus(id, userId, status)` — on/off shelf
- [x] 7.7 Implement `GoodsService.getDetail(id)` — full goods detail with seller info
- [x] 7.8 Implement `GoodsService.getRecommendList(page, size)` — waterfall feed sorted by popularity
- [x] 7.9 Implement `GoodsService.getLatestList(page, size)` — newest goods with infinite scroll
- [x] 7.10 Implement `GoodsService.recordView(goodsId, userId)` — record or increment view
- [x] 7.11 Implement `GoodsService.getMyGoods(userId, status, page, size)` — user's own goods
- [x] 7.12 Create `CategoryController` and `GoodsController`

## 8. Backend Service & Controller — Search Module

- [x] 8.1 Implement `GoodsService.search(dto)` — keyword + filters + sort in one query
- [x] 8.2 Build dynamic MyBatis query with conditional WHERE clauses
- [x] 8.3 Implement condition options endpoint (GET `/api/goods/conditions` — all conditions, campuses)
- [x] 8.4 Create `SearchController`

## 9. Backend Service & Controller — Social Module

- [x] 9.1 Implement `FavoriteService.add(userId, goodsId)` — add favorite with duplicate/self checks
- [x] 9.2 Implement `FavoriteService.remove(userId, goodsId)` — remove favorite
- [x] 9.3 Implement `FavoriteService.getMyFavorites(userId, page, size)` — paginated favorites list
- [x] 9.4 Implement `FollowService.follow(followerId, followeeId)` — follow with checks
- [x] 9.5 Implement `FollowService.unfollow(followerId, followeeId)` — unfollow
- [x] 9.6 Implement `FollowService.getFollowers(userId, page, size)` — followers list
- [x] 9.7 Implement `FollowService.getFollowing(userId, page, size)` — following list
- [x] 9.8 Implement `SocialService.getStatus(userId, targetUserId, goodsId)` — check follow/favorite status
- [x] 9.9 Create `FavoriteController`, `FollowController`, and `/api/social/status` endpoint

## 10. Backend Service & Controller — Chat Module

- [x] 10.1 Implement `ConversationService.getOrCreate(goodsId, buyerId, sellerId)` — find or create conversation
- [x] 10.2 Implement `ConversationService.getList(userId)` — conversations sorted by latest message
- [x] 10.3 Implement `MessageService.sendMessage(conversationId, senderId, content, type)` — persist and return message
- [x] 10.4 Implement `MessageService.getMessages(conversationId, page, size)` — paginated history
- [x] 10.5 Implement `MessageService.markAsRead(conversationId, userId)` — mark all messages as read
- [x] 10.6 Implement `MessageService.getUnreadCount(userId)` — total unread count
- [x] 10.7 Configure WebSocket with STOMP: `WebSocketConfig` implementing `WebSocketMessageBrokerConfigurer`
- [x] 10.8 Implement `WebSocketAuthInterceptor` — validate JWT on CONNECT
- [x] 10.9 Implement `ChatController` with STOMP message mapping (`@MessageMapping`, `@SendTo`)
- [x] 10.10 Create REST controllers for conversation list, message history, unread count

## 11. Backend Service & Controller — Order Module

- [x] 11.1 Implement `OrderService.create(buyerId, dto)` — create order with self-goods check, availability check
- [x] 11.2 Implement `OrderService.confirm(orderId, sellerId)` — seller confirms, PENDING→IN_PROGRESS
- [x] 11.3 Implement `OrderService.cancel(orderId, buyerId)` — buyer cancels, PENDING→CANCELLED
- [x] 11.4 Implement `OrderService.complete(orderId, buyerId)` — buyer completes, IN_PROGRESS→COMPLETED
- [x] 11.5 Implement `OrderService.getBuyerOrders(userId, status, page, size)` — buyer's order list
- [x] 11.6 Implement `OrderService.getSellerOrders(userId, status, page, size)` — seller's order list
- [x] 11.7 Implement `OrderService.getDetail(orderId, userId)` — detail with participant check
- [x] 11.8 Implement order state machine validation in service layer
- [x] 11.9 Implement `OrderLogService.addLog(orderId, action, operatorId, remark)` — record each state change
- [x] 11.10 Create `OrderController` with all order endpoints

## 12. Backend Service & Controller — Review Module

- [x] 12.1 Implement `ReviewService.create(reviewerId, dto)` — create review with completed order check
- [x] 12.2 Implement `ReviewService.getUserReviews(userId, page, size)` — reviews received by a user
- [x] 12.3 Implement `ReviewService.getMyReceivedReviews(userId, page, size)` — my received reviews
- [x] 12.4 Implement credit score recalculation after each review (average of all ratings)
- [x] 12.5 Create `ReviewController`

## 13. Backend Service & Controller — Report Module

- [x] 13.1 Implement `ReportService.create(reporterId, dto)` — create report with duplicate check
- [x] 13.2 Implement `ReportService.getMyReports(userId, page, size)` — user's submitted reports
- [x] 13.3 Implement `ReportService.getPendingReports(page, size)` — admin: list reports
- [x] 13.4 Implement `ReportService.handle(reportId, handlerId, dto)` — admin: approve/reject with penalty
- [x] 13.5 Create `ReportController`

## 14. Backend Service & Controller — Notification Module

- [x] 14.1 Implement `NotificationService.create(userId, type, title, content, relatedId)` — create notification
- [x] 14.2 Implement `NotificationService.getList(userId, page, size)` — paginated list
- [x] 14.3 Implement `NotificationService.markAsRead(id, userId)` — mark single
- [x] 14.4 Implement `NotificationService.markAllAsRead(userId)` — mark all
- [x] 14.5 Implement `NotificationService.getUnreadCount(userId)` — unread count
- [x] 14.6 Implement `NotificationService.pushToUser(userId, notification)` — push via WebSocket
- [x] 14.7 Integrate notification creation into order flow, review flow, admin actions
- [x] 14.8 Create `NotificationController`

## 15. Backend Service & Controller — Admin Module

- [x] 15.1 Implement `AdminService.getDashboard()` — aggregate stats query
- [x] 15.2 Implement `AdminService.listUsers(keyword, page, size)` — searchable user list
- [x] 15.3 Implement `AdminService.getUserDetail(userId)` — full user info for admin
- [x] 15.4 Implement `AdminService.disableUser(userId)` / `enableUser(userId)` — toggle user status
- [x] 15.5 Implement `AdminService.approveGoods(goodsId)` / `rejectGoods(goodsId, reason)` — goods review
- [x] 15.6 Implement `AdminService.takeDownGoods(goodsId, reason)` — force take down
- [x] 15.7 Implement `AdminService.deleteGoods(goodsId)` — admin delete
- [x] 15.8 Implement `AdminService.listGoods(status, keyword, page, size)` — admin goods list
- [x] 15.9 Implement `AdminService.approveVerification(authId)` / `rejectVerification(authId, reason)`
- [x] 15.10 Implement `AnnouncementService` CRUD (create, update, delete, list)
- [x] 15.11 Create `AdminController` and `AnnouncementController`

## 16. Backend — File Storage & Redis Cache

- [x] 16.1 Configure MinIO client bean with endpoint, access key, secret key
- [x] 16.2 Implement `FileService.upload(file, bucket)` — upload to MinIO, return URL
- [x] 16.3 Implement `FileService.delete(objectKey, bucket)` — remove from MinIO
- [x] 16.4 Implement `FileService.getUrl(objectKey, bucket)` — generate access URL
- [x] 16.5 Create `FileController` with upload endpoint
- [x] 16.6 Configure Redis cache manager with Jackson serialization
- [x] 16.7 Add `@Cacheable` / `@CacheEvict` annotations on goods detail, hot goods list, user profile
- [x] 16.8 Implement cache eviction on goods update/delete and user profile update

## 17. Frontend Project Initialization

- [x] 17.1 Scaffold Vite + Vue 3 + TypeScript project
- [x] 17.2 Install and configure dependencies: vue-router, pinia, axios, naive-ui, unocss, @vueuse/core
- [x] 17.3 Configure UnoCSS with theme colors, shortcuts, rules
- [x] 17.4 Configure TypeScript with strict mode, path aliases (`@/` → `src/`)
- [x] 17.5 Configure ESLint + Prettier
- [x] 17.6 Create directory structure: api, assets, components, composables, constants, hooks, layouts, router, stores, styles, types, utils, views
- [x] 17.7 Create global styles (reset, CSS variables for theme colors, font family, base styles)

## 18. Frontend Core Infrastructure

- [x] 18.1 Implement Axios instance with request interceptor (attach JWT token) and response interceptor (unwrap data, handle 401 redirect, error toast)
- [x] 18.2 Implement API modules: auth.ts, user.ts, goods.ts, category.ts, search.ts, favorite.ts, follow.ts, chat.ts, order.ts, review.ts, report.ts, notification.ts, announcement.ts, admin.ts, upload.ts
- [x] 18.3 Configure Vue Router with routes for all user and admin pages, lazy loading
- [x] 18.4 Implement router guards: auth check (redirect to login if not authenticated), admin role check
- [x] 18.5 Create Pinia stores: useAuthStore (user, token, login, logout, fetchProfile), useGoodsStore, useChatStore, useNotificationStore
- [x] 18.6 Create MainLayout component (header with logo, search, message icon, publish button, user dropdown)
- [x] 18.7 Create AdminLayout component (sidebar + header + content area)
- [x] 18.8 Implement global error boundary and 404 page
- [x] 18.9 Create reusable composables: useInfiniteScroll, useDebounce, useImageLazyLoad

## 19. Frontend — Auth Pages

- [x] 19.1 Create Login page with form validation (username, password)
- [x] 19.2 Create Register page with form validation (username, password, confirm password, nickname)
- [x] 19.3 Implement login/register API calls and token storage in useAuthStore
- [x] 19.4 Handle login redirect (back to originally requested page)

## 20. Frontend — Home Page

- [x] 20.1 Create HomeHero component (campus-style banner carousel using naive-ui Carousel)
- [x] 20.2 Create CategoryNav component (icon grid navigation for 7 categories)
- [x] 20.3 Create GoodsCard component (image, title, price, condition tag, view count, favorite button)
- [x] 20.4 Create GoodsWaterfall component (CSS columns / grid-based masonry layout)
- [x] 20.5 Create RecommendedSection with infinite scroll using useInfiniteScroll composable
- [x] 20.6 Create LatestSection with tab switch and infinite scroll
- [x] 20.7 Assemble HomePage with all sections

## 21. Frontend — Search Page

- [x] 21.1 Create SearchBar component with debounced input (useDebounce composable)
- [x] 21.2 Create SearchFilter component (category select, condition select, price range inputs, sort select)
- [x] 21.3 Create SearchResult component (grid of GoodsCards)
- [x] 21.4 Assemble SearchPage with search bar, filters, results, pagination/infinite scroll
- [x] 21.5 Sync search params with URL query string for shareable searches

## 22. Frontend — Goods Detail Page

- [x] 22.1 Create GoodsGallery component (image carousel with thumbnail navigation)
- [x] 22.2 Create GoodsInfo component (title, price, original price, condition, campus, description, publish time)
- [x] 22.3 Create SellerInfo component (avatar, nickname, credit score, follow button)
- [x] 22.4 Create GoodsActions component (favorite toggle, contact seller → start chat, buy now → create order)
- [x] 22.5 Create RelatedGoods component (same category goods)
- [x] 22.6 Assemble GoodsDetailPage with left-right layout (gallery | info + seller + actions)

## 23. Frontend — Publish Goods Page

- [x] 23.1 Create PublishSteps component (naive-ui Steps with 4 steps)
- [x] 23.2 Create Step1BasicInfo component (title input, category select, original price, transfer price)
- [x] 23.3 Create Step2ImageUpload component (multi-image upload with naive-ui Upload, preview grid, delete button, drag reorder)
- [x] 23.4 Create Step3Description component (rich text / textarea for description, condition select, campus input)
- [x] 23.5 Create Step4Confirm component (preview card showing all info before submit)
- [x] 23.6 Implement form state management with Pinia (multi-step form data persistence)
- [x] 23.7 Implement submit logic (upload images first, then create goods with image URLs)

## 24. Frontend — Chat / Message Page

- [x] 24.1 Create ConversationList component (avatar, nickname, last message preview, unread badge, time)
- [x] 24.2 Create ChatWindow component (message bubbles — sent vs received styling, text messages, image messages)
- [x] 24.3 Create ChatInput component (text input, image upload button, send button)
- [x] 24.4 Implement WebSocket connection with STOMP client (`@stomp/stompjs` + SockJS)
- [x] 24.5 Implement message sending/receiving, auto-scroll to bottom, mark as read on open
- [x] 24.6 Implement real-time notification badge update in header
- [x] 24.7 Create EmptyChat component (placeholder when no conversation selected)

## 25. Frontend — My Profile Page

- [x] 25.1 Create ProfileHeader component (avatar upload, nickname, school, credit score display)
- [x] 25.2 Create MyGoodsPage (tabbed list: active, pending review, taken down — with edit/delete/toggle actions)
- [x] 25.3 Create MyOrdersPage (tabbed list by buyer/seller, status filter)
- [x] 25.4 Create MyFavoritesPage (grid of favorited goods with remove button)
- [x] 25.5 Create MyFollowingPage (list of followed users with unfollow)
- [x] 25.6 Create MyReviewsPage (received reviews list)
- [x] 25.7 Create AccountSettingsPage (edit profile form, change password form, real-name verification form)
- [x] 25.8 Create NotificationList component (infinite scroll list with read/unread styling, click to navigate)

## 26. Frontend — Admin Dashboard

- [x] 26.1 Create DashboardPage (stat cards: users, goods, orders, active today; charts for trends)
- [x] 26.2 Create UserManagementPage (searchable table, view detail drawer, disable/enable actions, verification list with approve/reject)
- [x] 26.3 Create GoodsManagementPage (filterable table by status, approve/reject/take-down/delete actions, detail modal)
- [x] 26.4 Create ReportManagementPage (filterable table by status/type, handle report dialog with approve/reject + note)
- [x] 26.5 Create AnnouncementManagementPage (CRUD table, create/edit modal with title + content)

## 27. Integration & Polish

- [x] 27.1 End-to-end flow testing: register → login → publish goods → browse → search → favorite → chat → create order → confirm → complete → review
- [x] 27.2 Admin flow testing: login as admin → dashboard → review goods → manage users → handle reports → publish announcement
- [x] 27.3 Responsive design audit (desktop 1920px, tablet 768px, mobile 375px)
- [x] 27.4 Loading states: skeleton screens for cards/lists, loading spinners for actions
- [x] 27.5 Empty states: illustrational empty state components for all list pages
- [x] 27.6 Error states: network error retry, 404/500 error pages
- [x] 27.7 Image lazy loading implementation
- [x] 27.8 Route transition animations (page enter/leave)
- [x] 27.9 Final code review: remove console.log, ensure TypeScript strict compliance, verify ESLint passes
