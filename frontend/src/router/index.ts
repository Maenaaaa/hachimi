import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/request'
import MainLayout from '@/layouts/MainLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import type { RouteRecordRaw } from 'vue-router'

function isLoggedIn(): boolean {
  return !!getToken()
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', name: 'Home', component: () => import('@/views/home/HomePage.vue') },
      { path: 'search', name: 'Search', component: () => import('@/views/search/SearchPage.vue') },
      { path: 'goods/:id', name: 'GoodsDetail', component: () => import('@/views/goods/GoodsDetailPage.vue') },
    ],
  },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: 'publish', name: 'Publish', component: () => import('@/views/publish/PublishGoodsPage.vue') },
      { path: 'chat', name: 'Chat', component: () => import('@/views/chat/ChatPage.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/user/ProfilePage.vue') },
      { path: 'my-goods', name: 'MyGoods', component: () => import('@/views/user/MyGoodsPage.vue') },
      { path: 'my-orders', name: 'MyOrders', component: () => import('@/views/user/MyOrdersPage.vue') },
      { path: 'settings', name: 'Settings', component: () => import('@/views/user/AccountSettingsPage.vue') },
      { path: 'notifications', name: 'Notifications', component: () => import('@/views/user/NotificationsPage.vue') },
      { path: 'review/:id', name: 'ReviewDetail', component: () => import('@/views/review/ReviewDetailPage.vue') },
      { path: 'goods/:id/edit', name: 'EditGoods', component: () => import('@/views/goods/EditGoodsPage.vue') },
    ],
  },
  {
    path: '/login',
    redirect: { path: '/', query: { login: 'true' } },
  },
  {
    path: '/register',
    redirect: { path: '/', query: { login: 'true' } },
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('@/views/admin/DashboardPage.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/UserManagementPage.vue') },
      { path: 'goods', name: 'AdminGoods', component: () => import('@/views/admin/GoodsManagementPage.vue') },
      { path: 'reports', name: 'AdminReports', component: () => import('@/views/admin/ReportManagementPage.vue') },
      { path: 'verifications', name: 'AdminVerifications', component: () => import('@/views/admin/VerificationManagementPage.vue') },
      { path: 'announcements', name: 'AdminAnnouncements', component: () => import('@/views/admin/AnnouncementManagementPage.vue') },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: MainLayout,
    children: [
      {
        path: '',
        component: () => import('@/views/NotFound.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth && !isLoggedIn()) {
    next({ path: '/', query: { login: 'true', redirect: to.fullPath } })
    return
  }

  const userStr = localStorage.getItem('current_user')
  const isAdmin = (() => {
    if (!userStr) return false
    try { return JSON.parse(userStr).role?.toUpperCase() === 'ADMIN' } catch { return false }
  })()

  if (to.meta.requiresAdmin) {
    if (!isAdmin) {
      next({ path: '/' })
      return
    }
  }

  // Admin users going to root get redirected to admin panel
  if (isAdmin && to.path === '/') {
    next({ path: '/admin' })
    return
  }

  next()
})

export default router
