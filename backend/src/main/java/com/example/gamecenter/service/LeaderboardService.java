package com.example.gamecenter.service;

import com.example.gamecenter.utils.Result;

import java.util.Map;

/** 游戏排行榜查询、提交与个人排名。 */
public interface LeaderboardService {
    Result<Object> getLeaderboard(Long gameId, String type, Integer limit);
    Result<Object> submitScore(Long userId, Long gameId, Long score, String type);
    Result<Object> getUserRank(Long userId, Long gameId, String type);
}
