package com.example.gamecenter.service;

import com.example.gamecenter.mapper.FavoriteMapper;
import com.example.gamecenter.mapper.LeaderboardMapper;
import com.example.gamecenter.mapper.PlayHistoryMapper;
import com.example.gamecenter.service.impl.UserStatsServiceImpl;
import com.example.gamecenter.utils.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStatsServiceImplTest {

    @Mock
    private PlayHistoryMapper playHistoryMapper;

    @Mock
    private FavoriteMapper favoriteMapper;

    @Mock
    private LeaderboardMapper leaderboardMapper;

    @InjectMocks
    private UserStatsServiceImpl userStatsService;

    @Test
    void getUserStatistics_aggregatesDurationAndBestRank() {
        when(playHistoryMapper.countByUserId(1L)).thenReturn(3L);
        when(playHistoryMapper.sumDurationSecondsByUserId(1L)).thenReturn(1250L);
        when(favoriteMapper.countByUserId(1L)).thenReturn(2L);

        Map<String, Object> rankRow = new HashMap<>();
        rankRow.put("gameId", 10L);
        rankRow.put("gameName", "Snake");
        rankRow.put("rankPosition", 2);
        rankRow.put("score", 100L);
        rankRow.put("createdAt", "2026-01-01T00:00:00");
        when(leaderboardMapper.selectUserAllRanks(1L)).thenReturn(List.of(rankRow));

        Result<Object> res = userStatsService.getUserStatistics(1L);

        assertEquals(200, res.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getData();
        assertEquals(3L, ((Number) data.get("totalPlayCount")).longValue());
        assertEquals(1250L, ((Number) data.get("totalDurationSeconds")).longValue());
        assertEquals(2L, ((Number) data.get("favoriteCount")).longValue());
        assertEquals(2, ((Number) data.get("bestRank")).intValue());
        assertEquals(10L, ((Number) data.get("bestRankGameId")).longValue());
        assertEquals("Snake", data.get("bestRankGameName"));
    }

    @Test
    void getUserStatistics_nullCountsBecomeZero() {
        when(playHistoryMapper.countByUserId(1L)).thenReturn(null);
        when(playHistoryMapper.sumDurationSecondsByUserId(1L)).thenReturn(null);
        when(favoriteMapper.countByUserId(1L)).thenReturn(null);
        when(leaderboardMapper.selectUserAllRanks(1L)).thenReturn(List.of());

        Result<Object> res = userStatsService.getUserStatistics(1L);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) res.getData();
        assertEquals(0L, ((Number) data.get("totalPlayCount")).longValue());
        assertEquals(0L, ((Number) data.get("totalDurationSeconds")).longValue());
        assertEquals(0L, ((Number) data.get("favoriteCount")).longValue());
        assertNull(data.get("bestRank"));
    }
}
