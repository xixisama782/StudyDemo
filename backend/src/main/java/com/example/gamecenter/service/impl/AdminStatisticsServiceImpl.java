package com.example.gamecenter.service.impl;

import com.example.gamecenter.mapper.AdminStatisticsMapper;
import com.example.gamecenter.service.AdminStatisticsService;
import com.example.gamecenter.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 管理端统计：用户数、游戏数、游玩量与活跃用户。 */
@Service
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    private final AdminStatisticsMapper adminStatisticsMapper;

    @Autowired
    public AdminStatisticsServiceImpl(AdminStatisticsMapper adminStatisticsMapper) {
        this.adminStatisticsMapper = adminStatisticsMapper;
    }

    @Override
    public Result<Object> getOverview() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", safeLong(adminStatisticsMapper.countTotalUsers()));
        data.put("totalGames", safeLong(adminStatisticsMapper.countTotalGames()));
        data.put("totalPlays", safeLong(adminStatisticsMapper.sumTotalPlays()));
        data.put("activeUsersToday", safeLong(adminStatisticsMapper.countActiveUsersToday()));
        data.put("activeUsersWeek", safeLong(adminStatisticsMapper.countActiveUsersWeek()));
        data.put("activeUsersMonth", safeLong(adminStatisticsMapper.countActiveUsersMonth()));
        return Result.success(data);
    }

    @Override
    public Result<Object> getPopularGames(Integer limit) {
        if (limit == null || limit < 1) {
            limit = 10;
        }
        if (limit > 100) {
            limit = 100;
        }
        List<Map<String, Object>> list = adminStatisticsMapper.selectPopularGames(limit);
        return Result.success(list);
    }

    private static long safeLong(Long v) {
        return v == null ? 0L : v;
    }
}
