package com.example.gamecenter.exception;

import com.example.gamecenter.constant.ApiErrorMessages;
import com.example.gamecenter.utils.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 将未在 Controller 内显式处理的异常转为 {@link Result}。
 * <ul>
 *   <li><b>业务语义</b>：{@link BusinessException}（推荐由 Service 使用 {@link com.example.gamecenter.constant.ApiBizError} 构造）。</li>
 *   <li><b>框架/校验</b>：Bean Validation、参数缺失、请求体不可读等，文案见 {@link ApiErrorMessages} 或异常自带信息。</li>
 *   <li><b>Spring Security</b>：未认证 / 无权限走 {@link com.example.gamecenter.config.RestAuthenticationEntryPoint} /
 *       {@link com.example.gamecenter.config.RestAccessDeniedHandler}，与 {@link com.example.gamecenter.constant.ApiBizError#AUTH_UNAUTHORIZED} /
 *       {@link com.example.gamecenter.constant.ApiBizError#ACCESS_FORBIDDEN} 对齐，不经过本类。</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException ex) {
        return Result.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        FieldError fe = ex.getBindingResult().getFieldError();
        String msg = fe != null && fe.getDefaultMessage() != null
                ? fe.getDefaultMessage()
                : ApiErrorMessages.VALIDATION_FAILED;
        return Result.error(400, msg);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException ex) {
        FieldError fe = ex.getBindingResult().getFieldError();
        String msg = fe != null && fe.getDefaultMessage() != null
                ? fe.getDefaultMessage()
                : ApiErrorMessages.VALIDATION_FAILED;
        return Result.error(400, msg);
    }

    @ExceptionHandler(DataAccessException.class)
    public Result<Void> handleDataAccess(DataAccessException ex) {
        log.error("Data access error: {}", summarize(ex));
        log.debug("Data access stacktrace", ex);
        return Result.error(500, ApiErrorMessages.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse(ApiErrorMessages.VALIDATION_FAILED);
        return Result.error(400, msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException ex) {
        return Result.error(400, ApiErrorMessages.INVALID_REQUEST_BODY);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException ex) {
        return Result.error(400, "Parameter '" + ex.getParameterName() + "' is required");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return Result.error(400, "Invalid parameter value");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return Result.error(400, ApiErrorMessages.FILE_TOO_LARGE);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.debug("IllegalArgumentException: {}", ex.getMessage());
        return Result.error(400, ex.getMessage() != null ? ex.getMessage() : "Bad request");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnexpected(Exception ex) {
        log.error("Unhandled exception: {}", summarize(ex));
        log.debug("Unhandled exception stacktrace", ex);
        return Result.error(500, ApiErrorMessages.INTERNAL_SERVER_ERROR);
    }

    private String summarize(Exception ex) {
        String message = ex.getMessage();
        return ex.getClass().getSimpleName() + (message != null && !message.isBlank() ? ": " + message : "");
    }
}
