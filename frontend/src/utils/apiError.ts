/** 解析后端 ApiResult 结构，区分 HTTP 错误与业务 code */
import type { AxiosError } from 'axios'

export interface ApiResultBody {
  code?: number | string
  message?: string
  data?: unknown
}

/**
 * 判断 Axios 响应体是否为业务成功（`body.code === 200`，兼容字符串 `"200"`）。
 */
export function isApiSuccess(res: { data?: ApiResultBody } | null | undefined): boolean {
  const code = res?.data?.code
  return code != null && Number(code) === 200
}

/**
 * 从 Axios **错误**响应中读取后端统一结构 `{ code, message, data }` 的 message。
 */
export function getApiErrorMessage(error: unknown, fallback = '请求失败') {
  const ax = error as AxiosError<ApiResultBody> | undefined
  const msg = ax?.response?.data?.message
  return msg != null && String(msg).trim() !== '' ? msg : fallback
}

/**
 * HTTP 成功但业务 `body.code !== 200` 时，取 `data.message`（与后端 Result 一致）。
 */
export function getApiBusinessMessage(
  res: { data?: ApiResultBody } | null | undefined,
  fallback = '操作失败'
) {
  const msg = res?.data?.message
  return msg != null && String(msg).trim() !== '' ? msg : fallback
}
