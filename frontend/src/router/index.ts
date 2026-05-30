/** 路由配置与登录/角色守卫 */
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import LeaderboardView from '../views/LeaderboardView.vue'
import UserAppView from '../views/UserAppView.vue'
import GamesView from '../views/GamesView.vue'
import FavoritesView from '../views/FavoritesView.vue'
import HistoryView from '../views/HistoryView.vue'
import ProfileView from '../views/ProfileView.vue'
import ProfileEditView from '../views/ProfileEditView.vue'
import AccountSettingsView from '../views/AccountSettingsView.vue'
import GenericGamePlayerView from '../views/GenericGamePlayerView.vue'
import GameLobbyView from '../views/GameLobbyView.vue'
import AdminLoginView from '../views/AdminLoginView.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import AdminDashboardView from '../views/AdminDashboardView.vue'
import AdminGameTypesView from '../views/AdminGameTypesView.vue'
import AdminGamesView from '../views/AdminGamesView.vue'
import AdminUsersView from '../views/AdminUsersView.vue'
import { useAuthStore } from '../store/auth'

const routes: RouteRecordRaw[] = [
  // 公开：用户登录 / 注册 / 排行榜
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView
  },
  {
    path: '/leaderboard',
    name: 'Leaderboard',
    component: LeaderboardView
  },
  // 管理端登录
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: AdminLoginView
  },
  // 管理端（需 admin 角色）
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
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
  },
  // 用户端主应用（需 user 角色）
  {
    path: '/app',
    name: 'UserApp',
    component: UserAppView,
    meta: { requiresAuth: true, role: 'user' },
    children: [
      {
        path: '',
        name: 'Games',
        component: GamesView
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: FavoritesView
      },
      {
        path: 'history',
        name: 'History',
        component: HistoryView
      },
      {
        path: 'profile',
        name: 'Profile',
        component: ProfileView
      },
      {
        path: 'profile/edit',
        name: 'ProfileEdit',
        component: ProfileEditView
      },
      {
        path: 'profile/security',
        name: 'AccountSettings',
        component: AccountSettingsView
      },
      {
        path: 'game/:id',
        name: 'GameLobby',
        component: GameLobbyView
      },
      {
        path: 'game/:id/play',
        name: 'GamePlay',
        component: GenericGamePlayerView
      }
    ]
  },
  {
    path: '/',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/** 路由守卫：未登录跳转登录页；角色与 meta.role 不匹配时重定向到对应首页 */
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  const isAuthenticated = authStore.isAuthenticated
  const userRole = authStore.role

  if (to.meta.requiresAuth) {
    if (!isAuthenticated) {
      if (to.path.startsWith('/admin')) {
        next('/admin/login')
      } else {
        next('/login')
      }
    } else if (to.meta.role && to.meta.role !== userRole) {
      if (userRole === 'admin') {
        next('/admin')
      } else {
        next('/app')
      }
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
