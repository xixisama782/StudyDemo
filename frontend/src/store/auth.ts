import { defineStore } from 'pinia'
import api from '../api'

/** 与后端用户 / 管理员 DTO 对齐的宽松类型 */
export interface UserInfo {
  id?: number
  username?: string
  email?: string | null
  displayName?: string | null
  avatarUrl?: string | null
  createdAt?: string
  updatedAt?: string
  [key: string]: unknown
}

export type AdminInfo = UserInfo

export interface AuthPayload {
  token: string
  userId?: string
  role?: 'user' | 'admin'
  user?: UserInfo | null
  admin?: AdminInfo | null
}

const USER_KEY = 'user'

/** 从 localStorage 恢复用户/管理员资料，解析失败则清除 */
function loadStoredUser(): UserInfo | AdminInfo | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as UserInfo | AdminInfo
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

/** 登录态：token、角色与用户信息，持久化到 localStorage */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: localStorage.getItem('userId') || '',
    role: (localStorage.getItem('role') || '') as '' | 'user' | 'admin',
    user: loadStoredUser() as UserInfo | AdminInfo | null
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.role === 'admin',
    isUser: (state) => state.role === 'user'
  },
  actions: {
    setAuth(data: AuthPayload) {
      this.token = data.token
      this.userId = String(data.userId ?? data.admin?.id ?? data.user?.id ?? '')
      this.role = (data.role ?? (data.admin ? 'admin' : 'user')) as 'user' | 'admin'
      this.user = (data.user ?? data.admin) ?? null

      localStorage.setItem('token', this.token)
      localStorage.setItem('userId', this.userId)
      localStorage.setItem('role', this.role)
      if (this.user) {
        localStorage.setItem(USER_KEY, JSON.stringify(this.user))
      } else {
        localStorage.removeItem(USER_KEY)
      }
    },
    updateUser(user: UserInfo | AdminInfo | null) {
      this.user = user ? { ...user } : null
      if (this.user) {
        localStorage.setItem(USER_KEY, JSON.stringify(this.user))
      } else {
        localStorage.removeItem(USER_KEY)
      }
    },
    /** 调用对应登出接口后清空本地状态（接口失败仍清本地） */
    async logout() {
      try {
        if (this.role === 'admin') {
          await api.post('/admin/auth/logout')
        } else {
          await api.post('/auth/logout')
        }
      } catch (error) {
        console.error('Logout API error:', error)
      } finally {
        this.token = ''
        this.userId = ''
        this.role = ''
        this.user = null

        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem('role')
        localStorage.removeItem(USER_KEY)
      }
    }
  }
})
