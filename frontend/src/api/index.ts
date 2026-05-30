/** HTTP 客户端与按业务域划分的 API 封装 */
import axios, { type AxiosInstance } from 'axios'
import { useAuthStore } from '../store/auth'

/** 后端统一响应结构 */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api'

const api: AxiosInstance = axios.create({
  baseURL: apiBaseUrl,
  timeout: 30000
})

/** 发验证码接口可能等待 SMTP，超时略长 */
const SEND_CODE_TIMEOUT_MS = 30000

// 请求拦截：附加 Bearer Token
api.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截：401 时清除登录态
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      void authStore.logout()
    }
    return Promise.reject(error)
  }
)

export default api

// ── 用户端游戏 ──
export const gameApi = {
  getGameList(typeId: number | null | undefined, keyword: string | null | undefined, page = 1, pageSize = 10) {
    const params: Record<string, string | number> = { page, pageSize }
    if (typeId) params.typeId = typeId
    if (keyword) params.keyword = keyword
    return api.get<ApiResult<{ list: unknown[]; total?: number }>>('/games', { params })
  },

  getGameById(id: number | string) {
    return api.get<ApiResult<Record<string, unknown>>>(`/games/${id}`)
  }
}

// ── 游戏类型（用户端只读）──
export const gameTypeApi = {
  getGameTypes() {
    return api.get<ApiResult<unknown[]>>('/game-types')
  }
}

// ── 收藏 ──
export const favoriteApi = {
  getFavorites(page = 1, pageSize = 10) {
    return api.get<ApiResult<{ list: unknown[]; total?: number }>>('/users/me/favorites', { params: { page, pageSize } })
  },

  addFavorite(gameId: number) {
    return api.post<ApiResult<unknown>>('/users/me/favorites', { gameId })
  },

  removeFavorite(gameId: number) {
    return api.delete<ApiResult<unknown>>(`/users/me/favorites/${gameId}`)
  },

  checkFavorite(gameId: number) {
    return api.get<ApiResult<unknown>>(`/users/me/favorites/${gameId}/check`)
  },

  checkFavoritesBatch(gameIds: number[]) {
    return api.post<ApiResult<{ favoritedGameIds?: number[] }>>('/users/me/favorites/batch-check', { gameIds })
  }
}

// ── 游玩历史与会话 ──
export const historyApi = {
  getHistory(gameId: number | null = null, page = 1, pageSize = 10) {
    const params: Record<string, number> = { page, pageSize }
    if (gameId) params.gameId = gameId
    return api.get<ApiResult<{ list: unknown[]; total?: number }>>('/users/me/history', { params })
  },

  recordHistory(data: unknown) {
    return api.post<ApiResult<unknown>>('/users/me/history', data)
  },

  startGameSession(gameId: number) {
    return api.post<ApiResult<{ sessionId: number; startedAt?: string }>>('/users/me/history/session/start', { gameId })
  },

  endGameSession(sessionId: number, data: Record<string, unknown> = {}) {
    return api.put<ApiResult<unknown>>(`/users/me/history/session/${sessionId}/end`, data)
  },

  getCurrentSession() {
    return api.get<ApiResult<unknown>>('/users/me/history/session/current')
  }
}

// ── 游玩记录上报 ──
export const playApi = {
  recordPlay(gameId: number, data: unknown) {
    return api.post<ApiResult<unknown>>(`/games/${gameId}/play`, data)
  }
}

// ── 排行榜 ──
export const leaderboardApi = {
  getLeaderboard(gameId: number, type = 'all_time', page = 1, limit = 10) {
    const params = { type, page, limit }
    return api.get<ApiResult<unknown[]>>(`/games/${gameId}/leaderboard`, { params })
  },

  submitScore(gameId: number, data: unknown) {
    return api.post<ApiResult<unknown>>(`/games/${gameId}/leaderboard`, data)
  },

  getMyRank(gameId: number, type = 'all_time') {
    const params = { type }
    return api.get<ApiResult<Record<string, unknown> | null>>(`/games/${gameId}/leaderboard/me`, { params })
  }
}

// ── 认证（邮箱验证码等）──
export const authApi = {
  sendEmailCode(email: string, purpose: 'register' | 'change_password') {
    return api.post<ApiResult<unknown>>(
      '/auth/send-code',
      { email, purpose },
      { timeout: SEND_CODE_TIMEOUT_MS }
    )
  }
}

// ── 当前用户资料与安全 ──
export const userApi = {
  getMe() {
    return api.get<ApiResult<Record<string, unknown>>>('/users/me')
  },

  sendPasswordChangeCode() {
    return api.post<ApiResult<unknown>>('/users/me/password/send-code', null, {
      timeout: SEND_CODE_TIMEOUT_MS
    })
  },

  uploadAvatar(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post<ApiResult<{ avatarUrl?: string }>>('/users/me/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  updateProfile(data: unknown) {
    return api.put<ApiResult<Record<string, unknown>>>('/users/me', data)
  },

  changePassword(data: unknown) {
    return api.put<ApiResult<unknown>>('/users/me/password', data)
  }
}

// ── 用户侧统计与排行预览 ──
export const userStatsApi = {
  getStatistics() {
    return api.get<ApiResult<Record<string, unknown>>>('/users/me/statistics')
  },

  getLeaderboards() {
    return api.get<ApiResult<unknown[]>>('/users/me/leaderboards')
  }
}
