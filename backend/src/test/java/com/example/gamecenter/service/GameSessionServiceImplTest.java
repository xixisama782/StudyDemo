package com.example.gamecenter.service;

import com.example.gamecenter.entity.GameSession;
import com.example.gamecenter.mapper.GameMapper;
import com.example.gamecenter.mapper.GameSessionMapper;
import com.example.gamecenter.mapper.PlayHistoryMapper;
import com.example.gamecenter.service.impl.GameSessionServiceImpl;
import com.example.gamecenter.utils.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameSessionServiceImplTest {

    @Mock
    private GameSessionMapper gameSessionMapper;

    @Mock
    private GameMapper gameMapper;

    @Mock
    private PlayHistoryMapper playHistoryMapper;

    @InjectMocks
    private GameSessionServiceImpl gameSessionService;

    @Test
    void endSession_alreadyEnded_isIdempotent_noPlayHistoryMerge() {
        GameSession session = new GameSession();
        session.setId(99L);
        session.setUserId(5L);
        session.setGameId(10L);
        session.setStatus("ended");
        session.setStartedAt(LocalDateTime.now().minusMinutes(5));
        session.setEndedAt(LocalDateTime.now().minusMinutes(1));
        session.setDurationSeconds(240);

        when(gameSessionMapper.selectById(99L)).thenReturn(session);

        Result<Object> res = gameSessionService.endSession(5L, 99L, 10L, null);

        assertEquals(200, res.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getData();
        assertEquals(true, data.get("alreadyEnded"));
        assertEquals(10L, ((Number) data.get("gameId")).longValue());
        verify(gameSessionMapper, never()).updateById(any(GameSession.class));
        verify(playHistoryMapper, never()).upsertMergePlayHistory(anyLong(), anyLong(), any(), anyInt(), anyLong(), any());
    }

    @Test
    void endSession_active_updatesAndMergesHistory() {
        LocalDateTime start = LocalDateTime.now().minusSeconds(30);
        GameSession session = new GameSession();
        session.setId(1L);
        session.setUserId(5L);
        session.setGameId(10L);
        session.setStatus("active");
        session.setStartedAt(start);

        when(gameSessionMapper.selectById(1L)).thenReturn(session);
        when(gameSessionMapper.updateById(any(GameSession.class))).thenReturn(1);

        Result<Object> res = gameSessionService.endSession(5L, 1L, 42L, null);

        assertEquals(200, res.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getData();
        assertNull(data.get("alreadyEnded"));
        verify(gameSessionMapper).updateById(any(GameSession.class));
        verify(playHistoryMapper).upsertMergePlayHistory(eq(5L), eq(10L), any(LocalDateTime.class), anyInt(), eq(42L), isNull());
    }
}
