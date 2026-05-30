package com.example.gamecenter.config;

import com.example.gamecenter.controller.GameController;
import com.example.gamecenter.mapper.AdminMapper;
import com.example.gamecenter.mapper.AdminStatisticsMapper;
import com.example.gamecenter.mapper.FavoriteMapper;
import com.example.gamecenter.mapper.GameMapper;
import com.example.gamecenter.mapper.GameSessionMapper;
import com.example.gamecenter.mapper.GameTypeMapper;
import com.example.gamecenter.mapper.LeaderboardMapper;
import com.example.gamecenter.mapper.PlayHistoryMapper;
import com.example.gamecenter.mapper.UserMapper;
import com.example.gamecenter.service.GameService;
import com.example.gamecenter.service.PlayHistoryService;
import com.example.gamecenter.utils.JwtUtils;
import com.example.gamecenter.utils.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class SecurityConfigContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private GameService gameService;

    @MockBean
    private PlayHistoryService playHistoryService;

    @MockBean
    private AdminMapper adminMapper;

    @MockBean
    private AdminStatisticsMapper adminStatisticsMapper;

    @MockBean
    private FavoriteMapper favoriteMapper;

    @MockBean
    private GameMapper gameMapper;

    @MockBean
    private GameSessionMapper gameSessionMapper;

    @MockBean
    private GameTypeMapper gameTypeMapper;

    @MockBean
    private LeaderboardMapper leaderboardMapper;

    @MockBean
    private PlayHistoryMapper playHistoryMapper;

    @MockBean
    private UserMapper userMapper;

    @Test
    void publicGameList_allowsAnonymousAccess() throws Exception {
        when(gameService.getGameList(null, null, 1, 10))
                .thenReturn(Result.success(Map.of("total", 0, "page", 1, "pageSize", 10, "list", List.of())));

        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    void protectedAdminGameList_withoutTokenReturns401Json() throws Exception {
        mockMvc.perform(get("/api/games/admin/list"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未登录或无权访问"));
    }

    @Test
    void adminGameList_withUserTokenReturnsBusiness403() throws Exception {
        mockUserToken("user-token", 7L, "user");

        mockMvc.perform(get("/api/games/admin/list")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("需要管理员权限"));
    }

    @Test
    void adminGameList_withAdminTokenPassesToController() throws Exception {
        mockUserToken("admin-token", 1L, "admin");
        when(gameService.getAdminGameList(isNull(), isNull(), eq(1), eq(10)))
                .thenReturn(Result.success(Map.of("total", 0, "page", 1, "pageSize", 10, "list", List.of())));

        mockMvc.perform(get("/api/games/admin/list")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    private void mockUserToken(String token, Long userId, String role) {
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(token)).thenReturn(role + "-account");
        when(jwtUtils.getRoleFromToken(token)).thenReturn(role);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(userId);
    }
}
