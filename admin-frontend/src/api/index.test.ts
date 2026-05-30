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
      post: vi.fn((url: string, data?: unknown) => {
        calls.push({ method: 'post', url, data })
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

describe('admin frontend api contract helpers', async () => {
  const { adminGameApi } = await import('./index')

  it('builds admin game filtered list params', async () => {
    calls.length = 0
    await adminGameApi.getGameList(2, 'snake', 3, 10)
    expect(calls[0]).toMatchObject({
      method: 'get',
      url: '/games/admin/list',
      config: { params: { typeId: 2, keyword: 'snake', page: 3, pageSize: 10 } }
    })
  })
})
