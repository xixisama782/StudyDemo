package com.example.gamecenter.service;

import com.example.gamecenter.utils.Result;

/** 管理端运营统计概览与热门游戏。 */
public interface AdminStatisticsService {
    Result<Object> getOverview();

    Result<Object> getPopularGames(Integer limit);
}
