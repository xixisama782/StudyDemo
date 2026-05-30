package com.example.gamecenter.exception;

import com.example.gamecenter.constant.ApiBizError;

/**
 * 业务层可显式抛出，由 {@link GlobalExceptionHandler} 转为统一 {@link com.example.gamecenter.utils.Result}。
 * 与 HTTP 语义对齐：400/401/403/404/409/500 等。
 * <p>优先使用 {@link #BusinessException(ApiBizError)} 与 {@link ApiBizError} 保持一致。</p>
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ApiBizError error) {
        super(error.getMessage());
        this.code = error.getCode();
    }

    public BusinessException(ApiBizError error, Throwable cause) {
        super(error.getMessage(), cause);
        this.code = error.getCode();
    }

    public int getCode() {
        return code;
    }
}
