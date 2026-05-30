package com.example.gamecenter.controller;

import com.example.gamecenter.service.FavoriteService;
import com.example.gamecenter.service.GameSessionService;
import com.example.gamecenter.service.LeaderboardService;
import com.example.gamecenter.service.PlayHistoryService;
import com.example.gamecenter.utils.JwtUtils;
import com.example.gamecenter.utils.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ApiControllerContractTest {

    private static final long USER_ID = 7L;

    @Mock
    private FavoriteService favoriteService;

    @Mock
    private LeaderboardService leaderboardService;

    @Mock
    private GameSessionService gameSessionService;

    @Mock
    private PlayHistoryService playHistoryService;

    @Mock
    private JwtUtils jwtUtils;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private FavoriteController favoriteController;

    @InjectMocks
    private LeaderboardController leaderboardController;

    @InjectMocks
    private GameSessionController gameSessionController;

    @InjectMocks
    private PlayHistoryController playHistoryController;

    private MockMvc favoriteMvc;
    private MockMvc leaderboardMvc;
    private MockMvc sessionMvc;
    private MockMvc historyMvc;

    @BeforeEach
    void setUp() {
        favoriteMvc = MockMvcBuilders.standaloneSetup(favoriteController).build();
        leaderboardMvc = MockMvcBuilders.standaloneSetup(leaderboardController).build();
        sessionMvc = MockMvcBuilders.standaloneSetup(gameSessionController).build();
        historyMvc = MockMvcBuilders.standaloneSetup(playHistoryController).build();
    }

    @Test
    void batchCheckFavorites_usesDocumentedPathAndResponseShape() throws Exception {
        when(favoriteService.checkFavoritesBatch(eq(USER_ID), eq(List.of(1L, 2L))))
                .thenReturn(Result.success(Map.of("favoritedGameIds", List.of(2L))));

        favoriteMvc.perform(post("/api/users/me/favorites/batch-check")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameIds\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.favoritedGameIds[0]").value(2));
    }

    @Test
    void submitScore_requiresScoreParameter() throws Exception {
        leaderboardMvc.perform(post("/api/games/10/leaderboard")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("参数 score 为必填"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void submitScore_defaultsLeaderboardTypeToAllTime() throws Exception {
        when(leaderboardService.submitScore(USER_ID, 10L, 120L, "all_time"))
                .thenReturn(Result.success("Score submitted successfully",
                        Map.of("score", 120L, "rankPosition", 1, "isNewRecord", true)));

        leaderboardMvc.perform(post("/api/games/10/leaderboard")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":120}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.rankPosition").value(1))
                .andExpect(jsonPath("$.data.isNewRecord").value(true));

        verify(leaderboardService).submitScore(USER_ID, 10L, 120L, "all_time");
    }

    @Test
    void playHistoryRecord_requiresGameIdParameter() throws Exception {
        historyMvc.perform(post("/api/users/me/history")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("参数 gameId 为必填"));
    }

    @Test
    void beaconEnd_isIdempotentAndSkipsDuplicateWrites() throws Exception {
        when(jwtUtils.validateToken("token")).thenReturn(true);
        when(jwtUtils.getUserIdFromToken("token")).thenReturn(USER_ID);
        when(gameSessionService.endSession(USER_ID, 99L, 80L, null))
                .thenReturn(Result.success("Game session ended", sessionData(true)));

        sessionMvc.perform(post("/api/users/me/history/session/beacon/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"token\",\"sessionId\":99,\"score\":80}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Beacon session ended"))
                .andExpect(jsonPath("$.data.alreadyEnded").value(true));

        verify(leaderboardService, never()).submitScore(anyLong(), anyLong(), anyLong(), anyString());
        verify(playHistoryService, never()).recordPlay(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    void beaconEnd_recordsLeaderboardAndPlayOnceWhenSessionNewlyEnds() throws Exception {
        when(jwtUtils.validateToken("token")).thenReturn(true);
        when(jwtUtils.getUserIdFromToken("token")).thenReturn(USER_ID);
        when(gameSessionService.endSession(USER_ID, 99L, 80L, null))
                .thenReturn(Result.success("Game session ended", sessionData(false)));

        sessionMvc.perform(post("/api/users/me/history/session/beacon/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"token\",\"sessionId\":99,\"score\":80}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.alreadyEnded").value(false));

        verify(leaderboardService).submitScore(USER_ID, 10L, 80L, "all_time");
        verify(playHistoryService).recordPlay(USER_ID, 10L, null, 80L);
    }

    private Map<String, Object> sessionData(boolean alreadyEnded) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sessionId", 99L);
        data.put("gameId", 10L);
        data.put("durationSeconds", 30);
        data.put("alreadyEnded", alreadyEnded);
        return data;
    }
}
