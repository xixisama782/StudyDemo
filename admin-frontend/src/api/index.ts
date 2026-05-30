/** 管理端 HTTP 客户端与 API 封装 */
import axios, { type AxiosInstance } from 'axios'
import { useAuthStore } from '../store/auth'

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

api.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      void authStore.logout()
    }
    return Promise.reject(error)
  }
)

export default api

export const adminGameApi = {
  getGameList(typeId: number | null | undefined, keyword: string | null | undefined, page = 1, pageSize = 10) {
    const params: Record<string, string | number> = { page, pageSize }
    if (typeId) params.typeId = typeId
    if (keyword) params.keyword = keyword
    return api.get<ApiResult<{ list: unknown[]; total?: number }>>('/games/admin/list', { params })
  },

  createGame(data: unknown) {
    return api.post<ApiResult<unknown>>('/admin/games', data)
  },

  updateGame(id: number, data: unknown) {
    return api.put<ApiResult<unknown>>(`/admin/games/${id}`, data)
  },

  deleteGame(id: number) {
    return api.delete<ApiResult<unknown>>(`/admin/games/${id}`)
  }
}

export const gameTypeApi = {
  getGameTypes() {
    return api.get<ApiResult<unknown[]>>('/game-types')
  },

  createGameType(data: unknown) {
    return api.post<ApiResult<unknown>>('/admin/game-types', data)
  },

  updateGameType(id: number, data: unknown) {
    return api.put<ApiResult<unknown>>(`/admin/game-types/${id}`, data)
  },

  deleteGameType(id: number) {
    return api.delete<ApiResult<unknown>>(`/admin/game-types/${id}`)
  }
}

export const adminStatsApi = {
  getOverview() {
    return api.get<ApiResult<Record<string, unknown>>>('/admin/statistics/overview')
  },

  getPopularGames(limit = 10) {
    return api.get<ApiResult<unknown[]>>('/admin/statistics/popular-games', {
      params: { limit }
    })
  }
}

export const adminUserApi = {
  getUsers(keyword: string | undefined, status: string | undefined, page = 1, pageSize = 10) {
    const params: Record<string, string | number> = { page, pageSize }
    if (keyword) params.keyword = keyword
    if (status) params.status = status
    return api.get<ApiResult<{ list: unknown[]; total?: number }>>('/admin/users', { params })
  },

  getUserById(id: number) {
    return api.get<ApiResult<Record<string, unknown>>>(`/admin/users/${id}`)
  },

  updateUserStatus(id: number, status: string) {
    return api.put<ApiResult<unknown>>(`/admin/users/${id}/status`, { status })
  },

  resetPassword(id: number, newPassword: string) {
    return api.put<ApiResult<unknown>>(`/admin/users/${id}/password`, { newPassword })
  }
}
