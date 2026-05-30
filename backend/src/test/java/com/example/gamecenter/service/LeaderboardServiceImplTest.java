package com.example.gamecenter.service;

import com.example.gamecenter.entity.Leaderboard;
import com.example.gamecenter.mapper.LeaderboardMapper;
import com.example.gamecenter.service.impl.LeaderboardServiceImpl;
import com.example.gamecenter.utils.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceImplTest {

    @Mock
    private LeaderboardMapper leaderboardMapper;

    @InjectMocks
    private LeaderboardServiceImpl leaderboardService;

    private final Long gameId = 10L;
    private final Long userId = 5L;
    private final String type = "all_time";

    private Leaderboard row(long score) {
        Leaderboard lb = new Leaderboard();
        lb.setId(1L);
        lb.setGameId(gameId);
        lb.setUserId(userId);
        lb.setScore(score);
        lb.setType(type);
        lb.setCreatedAt(LocalDateTime.now());
        return lb;
    }

    @Test
    void submitScore_insertsNew_andUsesEffectiveScoreForRank() {
        Leaderboard afterInsert = row(100L);
        when(leaderboardMapper.selectByGameUserType(gameId, userId, type))
                .thenReturn(null)
                .thenReturn(afterInsert);
        when(leaderboardMapper.countHigherScores(gameId, type, 100L)).thenReturn(0L);

        Result<Object> res = leaderboardService.submitScore(userId, gameId, 100L, type);

        assertEquals(200, res.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getData();
        assertEquals(100L, ((Number) data.get("score")).longValue());
        assertEquals(1, ((Number) data.get("rankPosition")).intValue());
        assertEquals(true, data.get("isNewRecord"));
        verify(leaderboardMapper).insert(any(Leaderboard.class));
        verify(leaderboardMapper).countHigherScores(gameId, type, 100L);
    }

    @Test
    void submitScore_lowerScore_usesStoredScoreForRank_notSubmitted() {
        Leaderboard existing = row(100L);
        when(leaderboardMapper.selectByGameUserType(gameId, userId, type))
                .thenReturn(existing)
                .thenReturn(existing);
        when(leaderboardMapper.countHigherScores(gameId, type, 100L)).thenReturn(2L);

        Result<Object> res = leaderboardService.submitScore(userId, gameId, 30L, type);

        assertEquals(200, res.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getData();
        assertEquals(100L, ((Number) data.get("score")).longValue());
        assertEquals(3, ((Number) data.get("rankPosition")).intValue());
        assertEquals(false, data.get("isNewRecord"));
        verify(leaderboardMapper, never()).updateById(any(Leaderboard.class));
        verify(leaderboardMapper).countHigherScores(gameId, type, 100L);
        verify(leaderboardMapper, never()).countHigherScores(eq(gameId), eq(type), eq(30L));
    }

    @Test
    void submitScore_improvesScore_updatesAndRecalculatesRank() {
        Leaderboard before = row(50L);
        Leaderboard after = row(200L);
        when(leaderboardMapper.selectByGameUserType(gameId, userId, type))
                .thenReturn(before)
                .thenReturn(after);
        when(leaderboardMapper.countHigherScores(gameId, type, 200L)).thenReturn(0L);

        Result<Object> res = leaderboardService.submitScore(userId, gameId, 200L, type);

        assertEquals(200, res.getCode());
        verify(leaderboardMapper).updateById(any(Leaderboard.class));
        verify(leaderboardMapper).countHigherScores(gameId, type, 200L);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getData();
        assertEquals(true, data.get("isNewRecord"));
        assertEquals(1, ((Number) data.get("rankPosition")).intValue());
    }

    @Test
    void getLeaderboard_resolvesAvatarFallback() {
        Map<String, Object> row = new HashMap<>();
        row.put("avatarurl", "/uploads/x.jpg");
        when(leaderboardMapper.selectLeaderboard(gameId, type, 10))
                .thenReturn(List.of(row));

        Result<Object> res = leaderboardService.getLeaderboard(gameId, type, 10);

        assertEquals(200, res.getCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) res.getData();
        assertEquals(1, list.size());
        assertTrue(list.get(0).containsKey("avatarUrl"));
    }
}
