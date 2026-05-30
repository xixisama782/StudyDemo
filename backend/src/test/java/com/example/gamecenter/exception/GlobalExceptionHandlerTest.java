package com.example.gamecenter.exception;

import com.example.gamecenter.constant.ApiErrorMessages;
import com.example.gamecenter.utils.Result;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusiness_returnsCodeAndMessage() {
        Result<Void> r = handler.handleBusiness(new BusinessException(404, "Not found"));
        assertEquals(404, r.getCode());
        assertEquals("Not found", r.getMessage());
        assertNull(r.getData());
    }

    @Test
    void handleUnexpected_returns500WithoutDetail() {
        Result<Void> r = handler.handleUnexpected(new RuntimeException("secret"));
        assertEquals(500, r.getCode());
        assertEquals(ApiErrorMessages.INTERNAL_SERVER_ERROR, r.getMessage());
    }

    @Test
    void handleNotReadable_returns400() {
        Result<Void> r = handler.handleNotReadable(mock(HttpMessageNotReadableException.class));
        assertEquals(400, r.getCode());
        assertEquals(ApiErrorMessages.INVALID_REQUEST_BODY, r.getMessage());
    }
}
