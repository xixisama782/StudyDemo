package com.example.gamecenter.controller;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.constant.ApiConstants;
import com.example.gamecenter.service.GameSessionService;
import com.example.gamecenter.service.LeaderboardService;
import com.example.gamecenter.service.PlayHistoryService;
import com.example.gamecenter.utils.JwtUtils;
import com.example.gamecenter.utils.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 游戏会话：开始、结束、当前会话与 sendBeacon 兜底结束。 */
@RestController
@RequestMapping(ApiConstants.GameSession.BASE)
public class GameSessionController {

    @Autowired
    private GameSessionService gameSessionService;

    @Autowired
    private LeaderboardService leaderboardService;

    @Autowired
    private PlayHistoryService playHistoryService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(ApiConstants.GameSession.START)
    public Result<Object> startSession(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        Long gameId = body.get("gameId") != null ?
            Long.valueOf(body.get("gameId").toString()) : null;
        if (gameId == null) {
            return Result.error(
                    ApiBizError.PARAM_GAME_ID_REQUIRED.getCode(),
                    ApiBizError.PARAM_GAME_ID_REQUIRED.getMessage());
        }

        return gameSessionService.startSession(userId, gameId);
    }

    @PutMapping(ApiConstants.GameSession.END)
    public Result<Object> endSession(
            @PathVariable Long sessionId,
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        Long score = body != null && body.get("score") != null ?
            Long.valueOf(body.get("score").toString()) : null;
        String meta = body != null && body.get("meta") != null ?
            body.get("meta").toString() : null;

        return gameSessionService.endSession(userId, sessionId, score, meta);
    }

    @GetMapping(ApiConstants.GameSession.CURRENT)
    public Result<Object> getCurrentSession(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        return gameSessionService.getCurrentSession(userId);
    }

    /**
     * 页面关闭时 sendBeacon 兜底：请求体为 JSON，含 sessionId、score、token（JWT）。
     * 不走 Authorization 头，故单独放行并由 token 校验用户身份。
     */
    @PostMapping(ApiConstants.GameSession.BEACON_END)
    public Result<Object> beaconEnd(@RequestBody String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String token = root.hasNonNull("token") ? root.get("token").asText() : null;
            if (token == null || !jwtUtils.validateToken(token)) {
                return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
            }
            Long userId = jwtUtils.getUserIdFromToken(token);

            if (!root.hasNonNull("sessionId")) {
                return Result.error(
                        ApiBizError.PARAM_SESSION_ID_REQUIRED.getCode(),
                        ApiBizError.PARAM_SESSION_ID_REQUIRED.getMessage());
            }
            Long sessionId = root.get("sessionId").asLong();
            long score = root.hasNonNull("score") ? root.get("score").asLong() : 0L;

            Result<Object> endResult = gameSessionService.endSession(userId, sessionId, score, null);
            if (endResult.getCode() != 200) {
                return endResult;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) endResult.getData();
            boolean alreadyEnded = Boolean.TRUE.equals(data.get("alreadyEnded"));
            if (alreadyEnded) {
                return Result.success("Beacon session ended", data);
            }

            Object gidObj = data.get("gameId");
            Long gameId = gidObj instanceof Number ? ((Number) gidObj).longValue()
                : Long.parseLong(gidObj.toString());

            if (score > 0) {
                leaderboardService.submitScore(userId, gameId, score, "all_time");
            }
            playHistoryService.recordPlay(userId, gameId, null, score);

            return Result.success("Beacon session ended", data);
        } catch (Exception e) {
            return Result.error(
                    ApiBizError.REQUEST_BODY_INVALID.getCode(),
                    ApiBizError.REQUEST_BODY_INVALID.getMessage());
        }
    }
}
