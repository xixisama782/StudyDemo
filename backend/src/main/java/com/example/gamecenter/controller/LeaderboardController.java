package com.example.gamecenter.controller;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.constant.ApiConstants;
import com.example.gamecenter.service.LeaderboardService;
import com.example.gamecenter.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/** 游戏排行榜查询、提交分数与当前用户排名。 */
@RestController
@RequestMapping(ApiConstants.Games.BASE)
public class LeaderboardController {

    @Autowired
    private LeaderboardService leaderboardService;

    @GetMapping(ApiConstants.Games.LEADERBOARD)
    public Result<Object> getLeaderboard(
            @PathVariable Long id,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return leaderboardService.getLeaderboard(id, type, limit);
    }

    @PostMapping(ApiConstants.Games.LEADERBOARD)
    public Result<Object> submitScore(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        Long score = body.get("score") != null ? Long.valueOf(body.get("score").toString()) : null;
        if (score == null) {
            return Result.error(
                    ApiBizError.PARAM_SCORE_REQUIRED.getCode(),
                    ApiBizError.PARAM_SCORE_REQUIRED.getMessage());
        }

        String type = body.get("type") != null ? body.get("type").toString() : "all_time";

        return leaderboardService.submitScore(userId, id, score, type);
    }

    @GetMapping(ApiConstants.Games.LEADERBOARD_ME)
    public Result<Object> getUserRank(
            @PathVariable Long id,
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        return leaderboardService.getUserRank(userId, id, type);
    }
}
