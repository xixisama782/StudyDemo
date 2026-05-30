package com.example.gamecenter.service;

import com.example.gamecenter.utils.Result;

/** 当前用户游玩汇总统计与各游戏榜位。 */
public interface UserStatsService {
    Result<Object> getUserStatistics(Long userId);
    Result<Object> getUserLeaderboards(Long userId);
}
