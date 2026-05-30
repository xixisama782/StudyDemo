package com.example.gamecenter.controller;

import com.example.gamecenter.constant.ApiBizError;
import com.example.gamecenter.constant.ApiConstants;
import com.example.gamecenter.service.UserStatsService;
import com.example.gamecenter.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/** 当前用户游玩统计与各游戏排行榜成绩汇总。 */
@RestController
@RequestMapping(ApiConstants.UserStats.BASE)
public class UserStatsController {

    @Autowired
    private UserStatsService userStatsService;

    @GetMapping("/statistics")
    public Result<Object> getStatistics(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        return userStatsService.getUserStatistics(userId);
    }

    @GetMapping("/leaderboards")
    public Result<Object> getLeaderboards(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(
                    ApiBizError.AUTH_UNAUTHORIZED.getCode(),
                    ApiBizError.AUTH_UNAUTHORIZED.getMessage());
        }

        return userStatsService.getUserLeaderboards(userId);
    }
}
