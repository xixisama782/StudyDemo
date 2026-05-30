package com.example.gamecenter.constant;

/**
 * 全局异常处理器、校验失败等<strong>非业务枚举</strong>场景的英文短文案。
 * <p>业务规则错误（如「用户不存在」「用户名已存在」）应使用 {@link ApiBizError} + {@link com.example.gamecenter.exception.BusinessException}
 * 或 Controller 内 {@link com.example.gamecenter.utils.Result#error(int, String)} 与枚举码一致。</p>
 */
public final class ApiErrorMessages {

    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    public static final String INVALID_REQUEST_BODY = "Invalid request body";
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String FILE_TOO_LARGE = "File too large";
    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String FORBIDDEN = "Forbidden";

    private ApiErrorMessages() {
    }
}
