/** 路由配置与登录守卫（用户端） */
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
import { useAuthStore } from '../store/auth'

const adminAppUrl = import.meta.env.VITE_ADMIN_URL || 'http://localhost:10110/login'

const redirectToAdminApp = () => {
  window.location.replace(adminAppUrl)
}

const routes: RouteRecordRaw[] = [
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

/** 路由守卫：未登录跳转登录页；旧 /admin 路径跳转独立管理端 */
router.beforeEach((to, _from, next) => {
  if (to.path === '/admin' || to.path.startsWith('/admin/')) {
    redirectToAdminApp()
    return
  }

  const authStore = useAuthStore()
  const isAuthenticated = authStore.isAuthenticated

  if (to.meta.requiresAuth) {
    if (!isAuthenticated) {
      next('/login')
    } else if (to.meta.role && to.meta.role !== authStore.role) {
      next('/app')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
