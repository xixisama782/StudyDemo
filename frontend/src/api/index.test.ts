import { describe, expect, it, vi } from 'vitest'

const calls: Array<{
  method: string
  url: string
  data?: unknown
  config?: { params?: Record<string, unknown> }
}> = []

vi.mock('../store/auth', () => ({
  useAuthStore: () => ({
    token: 'test-token',
    logout: vi.fn()
  })
}))

vi.mock('axios', () => ({
  default: {
    create: () => ({
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() }
      },
      get: vi.fn((url: string, config?: { params?: Record<string, unknown> }) => {
        calls.push({ method: 'get', url, config })
        return Promise.resolve({ data: { code: 200 } })
      }),
      post: vi.fn((url: string, data?: unknown, config?: { params?: Record<string, unknown> }) => {
        calls.push({ method: 'post', url, data, config })
        return Promise.resolve({ data: { code: 200 } })
      }),
      put: vi.fn((url: string, data?: unknown) => {
        calls.push({ method: 'put', url, data })
        return Promise.resolve({ data: { code: 200 } })
      }),
      delete: vi.fn((url: string) => {
        calls.push({ method: 'delete', url })
        return Promise.resolve({ data: { code: 200 } })
      })
    })
  }
}))

describe('frontend api contract helpers', async () => {
  const {
    favoriteApi,
    historyApi,
    leaderboardApi,
    playApi
  } = await import('./index')

  it('builds batch favorite check request', async () => {
    calls.length = 0
    await favoriteApi.checkFavoritesBatch([1, 2])
    expect(calls[0]).toMatchObject({
      method: 'post',
      url: '/users/me/favorites/batch-check',
      data: { gameIds: [1, 2] }
    })
  })

  it('builds game session and play reporting requests', async () => {
    calls.length = 0
    await historyApi.startGameSession(10)
    await historyApi.endGameSession(99, { score: 80 })
    await playApi.recordPlay(10, { score: 80 })

    expect(calls.map((c) => c.url)).toEqual([
      '/users/me/history/session/start',
      '/users/me/history/session/99/end',
      '/games/10/play'
    ])
  })

  it('builds leaderboard requests with documented params', async () => {
    calls.length = 0
    await leaderboardApi.getLeaderboard(10, 'weekly', 1, 20)
    await leaderboardApi.submitScore(10, { score: 100 })
    await leaderboardApi.getMyRank(10, 'weekly')

    expect(calls[0]).toMatchObject({
      method: 'get',
      url: '/games/10/leaderboard',
      config: { params: { type: 'weekly', page: 1, limit: 20 } }
    })
    expect(calls[1]).toMatchObject({
      method: 'post',
      url: '/games/10/leaderboard',
      data: { score: 100 }
    })
    expect(calls[2]).toMatchObject({
      method: 'get',
      url: '/games/10/leaderboard/me',
      config: { params: { type: 'weekly' } }
    })
  })

})
