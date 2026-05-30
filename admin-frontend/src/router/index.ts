/** 管理端路由与登录守卫 */
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AdminLoginView from '../views/AdminLoginView.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminDashboardView from '../views/AdminDashboardView.vue'
import AdminGameTypesView from '../views/AdminGameTypesView.vue'
import AdminGamesView from '../views/AdminGamesView.vue'
import AdminUsersView from '../views/AdminUsersView.vue'
import { useAuthStore } from '../store/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'AdminLogin',
    component: AdminLoginView
  },
  {
    path: '/',
    component: AdminLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: AdminDashboardView
      },
      {
        path: 'games',
        name: 'AdminGames',
        component: AdminGamesView
      },
      {
        path: 'game-types',
        name: 'AdminGameTypes',
        component: AdminGameTypesView
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: AdminUsersView
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
