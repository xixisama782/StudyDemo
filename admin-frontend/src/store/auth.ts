import { defineStore } from 'pinia'
import api from '../api'

export interface AdminInfo {
  id?: number
  username?: string
  email?: string | null
  displayName?: string | null
  avatarUrl?: string | null
  createdAt?: string
  updatedAt?: string
  [key: string]: unknown
}

export interface AuthPayload {
  token: string
  userId?: string
  admin?: AdminInfo | null
}

const USER_KEY = 'user'

function loadStoredAdmin(): AdminInfo | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AdminInfo
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

/** 管理端登录态：token 与管理员资料 */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: localStorage.getItem('userId') || '',
    admin: loadStoredAdmin()
  }),
  getters: {
    isAuthenticated: (state) => !!state.token
  },
  actions: {
    setAuth(data: AuthPayload) {
      this.token = data.token
      this.userId = String(data.userId ?? data.admin?.id ?? '')
      this.admin = data.admin ?? null

      localStorage.setItem('token', this.token)
      localStorage.setItem('userId', this.userId)
      if (this.admin) {
        localStorage.setItem(USER_KEY, JSON.stringify(this.admin))
      } else {
        localStorage.removeItem(USER_KEY)
      }
    },
    updateAdmin(admin: AdminInfo | null) {
      this.admin = admin ? { ...admin } : null
      if (this.admin) {
        localStorage.setItem(USER_KEY, JSON.stringify(this.admin))
      } else {
        localStorage.removeItem(USER_KEY)
      }
    },
    async logout() {
      try {
        await api.post('/admin/auth/logout')
      } catch (error) {
        console.error('Logout API error:', error)
      } finally {
        this.token = ''
        this.userId = ''
        this.admin = null

        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem(USER_KEY)
      }
    }
  }
})
