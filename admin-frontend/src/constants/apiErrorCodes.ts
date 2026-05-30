/**
 * 与后端 `ApiBizError` 中 `code` 字段对应（仅列常用值）。
 */
export const ApiErrorCodes = {
  OK: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  SERVER_ERROR: 500
} as const

export type ApiErrorCodeKey = keyof typeof ApiErrorCodes
